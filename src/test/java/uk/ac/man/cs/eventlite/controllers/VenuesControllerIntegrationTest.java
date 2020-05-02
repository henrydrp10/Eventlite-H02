package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	@LocalServerPort
	private int port;
	
	private String baseUrl;

	private HttpEntity<String> httpEntity;

	@Autowired
	private TestRestTemplate template;
	
    private String loginUrl ;
	
	// We need cookies for Web log in.
	// Initialize this each time we need it to ensure it's clean.
	private TestRestTemplate stateful;

	@BeforeEach
	public void setup() {
		HttpHeaders headers = new HttpHeaders();
		
		this.baseUrl = "http://localhost:" + port + "/venues";
		this.loginUrl = "http://localhost:" + port + "/sign-in";

		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		httpEntity = new HttpEntity<String>(headers);
	}
	
	@Test
	public void testGetAllVenues() {
		ResponseEntity<String> response = template.exchange("/venues", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	
	@Test
	public void testShowVenueDetailPage() {
		ResponseEntity<String> response = template.exchange(baseUrl+ "/1", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	
	@Test
	public void testShowUpdateEventPage() {
//		template = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);
//		
//		// Set up headers for GETting and POSTing.
//		HttpHeaders getHeaders = new HttpHeaders();
//		HttpHeaders postHeaders = new HttpHeaders();
//		
//		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
//		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
//		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//		
//		//Login and get cookie session
//		String cookie = integrationLogin(template, getHeaders, postHeaders);
//		
//		// Set the session cookie and GET the new greeting form so we can read
//		// the new CSRF token.
//		getHeaders.set("Cookie", cookie);
//		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);

		
		ResponseEntity<String> response = template.exchange(baseUrl + "/updateVenue/1", HttpMethod.GET, httpEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	

	
	@Test
	public void testShowCreateVenuePage() {
		
		ResponseEntity<String> response = template.exchange("/venues/new", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	
	
	
	@Test
	public void testCreateVenue() {
		stateful = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);
		
		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		HttpHeaders postHeaders = new HttpHeaders();
		
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		//Login and get cookie session
		String cookie = integrationLogin(stateful, getHeaders, postHeaders);
		
		// Set the session cookie and GET the new greeting form so we can read
		// the new CSRF token.
		getHeaders.set("Cookie", cookie);
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> formResponse = stateful.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());

		MultiValueMap<String, String> venue = new LinkedMultiValueMap<String, String>();

		venue.add("_csrf", csrfToken);
		venue.add("name", "test name");
		venue.add("roadName", "Ossory St");
		venue.add("postCode", "M144BX");
		venue.add("capacity", "2000");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(venue, postHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));		
	}

	
	@Test
	public void testCreateVenueNoCSRF() {
		stateful = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);
		
		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		HttpHeaders postHeaders = new HttpHeaders();
		
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		//Login and get cookie session
		String cookie = integrationLogin(stateful, getHeaders, postHeaders);
		
		// Set the session cookie and GET the new greeting form so we can read
		// the new CSRF token.
		getHeaders.set("Cookie", cookie);
		
		MultiValueMap<String, String> venue = new LinkedMultiValueMap<String, String>();
		venue.add("name", "test name");
		venue.add("roadName", "test description");
		venue.add("postCode", "test summary");
		venue.add("capacity", "2000");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(venue, postHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));		
	}
	
	@Test
	public void testCreateVenueNoLogin() {
		
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> venue = new LinkedMultiValueMap<String, String>();
		venue.add("name", "test name");
		venue.add("roadName", "test description");
		venue.add("postCode", "test summary");
		venue.add("capacity", "2000");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(venue,
				postHeaders);

		ResponseEntity<String> response = template.exchange(baseUrl + "/update/1", HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));

	}

	
	@Test
	public void testUpdateVenue() {
		stateful = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);
		
		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		HttpHeaders postHeaders = new HttpHeaders();
		
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		//Login and get cookie session
		String cookie = integrationLogin(stateful, getHeaders, postHeaders);
		
		// Set the session cookie and GET the new event form so we can read
		// the new CSRF token.
		getHeaders.set("Cookie", cookie);
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> loginResponse = stateful.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(loginResponse.getBody());

		MultiValueMap<String, String> venue = new LinkedMultiValueMap<String, String>();
		venue.add("_csrf", csrfToken);
		venue.add("name", "test name");
		venue.add("roadName", "test description");
		venue.add("postCode", "test summary");
		venue.add("capacity", "2000");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(venue, postHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl + "/update/1", HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));

		
	}
	
	@Test
	public void testUpdateEventNoCSRF() {
		stateful = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);
		
		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		HttpHeaders postHeaders = new HttpHeaders();
		
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		//Login and get cookie session
		String cookie = integrationLogin(stateful, getHeaders, postHeaders);
		
		// Set the session cookie and GET the new event form so we can read
		// the new CSRF token.
		getHeaders.set("Cookie", cookie);

		MultiValueMap<String, String> venue = new LinkedMultiValueMap<String, String>();
		venue.add("name", "test name");
		venue.add("roadName", "test description");
		venue.add("postCode", "test summary");
		venue.add("capacity", "2000");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(venue, postHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl + "/update/1", HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));

		
	}
	
	public void testUpdateEventNoLogin() {
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> venue = new LinkedMultiValueMap<String, String>();
		venue.add("name", "test name");
		venue.add("roadName", "test description");
		venue.add("postCode", "test summary");
		venue.add("capacity", "2000");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(venue,
				postHeaders);

		ResponseEntity<String> response = template.exchange( baseUrl +"/update/1", HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
	}
	
	@Test
	public void testDeleteEventNoLogin() {
		
		HttpHeaders getHeaders = new HttpHeaders();		
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		MultiValueMap<String, String> venue = new LinkedMultiValueMap<String, String>();
		venue.add("name", "test name");
		venue.add("roadName", "test description");
		venue.add("postCode", "test summary");
		venue.add("capacity", "2000");
		HttpEntity<MultiValueMap<String, String>> deleteEntity = new HttpEntity<MultiValueMap<String, String>>(venue, getHeaders);

		ResponseEntity<String> response = template.exchange(baseUrl + "/delete/1", HttpMethod.DELETE, deleteEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
		
	}
	
	@Test
	public void testDeleteVenue() {
		stateful = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);
		
		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		HttpHeaders postHeaders = new HttpHeaders();
		
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		//Login and get cookie session
		String cookie = integrationLogin(stateful, getHeaders, postHeaders);
		
		// Set the session cookie and GET the new event form so we can read
		// the new CSRF token.
		
		getHeaders.set("Cookie", cookie);
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> loginResponse = stateful.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(loginResponse.getBody());

		MultiValueMap<String, String> venue = new LinkedMultiValueMap<String, String>();
		venue.add("_csrf", csrfToken);
		venue.add("name", "test name");
		venue.add("roadName", "test description");
		venue.add("postCode", "test summary");
		venue.add("capacity", "2000");
		HttpEntity<MultiValueMap<String, String>> deleteEntity = new HttpEntity<MultiValueMap<String, String>>(venue, getHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl +"/delete/1", HttpMethod.DELETE, deleteEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));

		
	}
	
	@Test
	public void testDeleteVenueNoCSRF() {
		stateful = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);
		
		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		HttpHeaders postHeaders = new HttpHeaders();
		
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		//Login and get cookie session
		String cookie = integrationLogin(stateful, getHeaders, postHeaders);
		
		// Set the session cookie and GET the new event form so we can read
		// the new CSRF token.
		getHeaders.set("Cookie", cookie);

		MultiValueMap<String, String> venue = new LinkedMultiValueMap<String, String>();
		venue.add("name", "test name");
		venue.add("roadName", "test description");
		venue.add("postCode", "test summary");
		venue.add("capacity", "2000");
		HttpEntity<MultiValueMap<String, String>> deleteEntity = new HttpEntity<MultiValueMap<String, String>>(venue, getHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl + "/delete/1", HttpMethod.DELETE, deleteEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));

		
	}
	
	
	public static String getCsrfToken(String body)
	{
		Pattern pattern = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
		Matcher matcher = pattern.matcher(body);
		assertThat(matcher.matches(), equalTo(true));
		return matcher.group(1);
	}
	
	public  String integrationLogin(TestRestTemplate t, HttpHeaders getHeaders, HttpHeaders postHeaders)
	{
		
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> formResponse = t.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie").split(";")[0];
		HttpEntity<MultiValueMap<String, String>> postEntity;
		postHeaders.set("Cookie", cookie);
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Organiser");
		login.add("password", "Organiser");
		
		// Log in.
		postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				postHeaders);
		ResponseEntity<String> loginResponse = t.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
		
		return cookie;
	}

}

