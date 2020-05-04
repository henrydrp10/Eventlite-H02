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
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private HttpEntity<String> httpEntity;
	
	@Autowired
	private TestRestTemplate template;
	
	@Autowired
	private VenueService venueService;
	
	@Autowired
	private EventService eventService;
	
	@LocalServerPort
	private int port;

	private String baseUrl;
	private String loginUrl;
	
	// We need cookies for Web log in.
	// Initialize this each time we need it to ensure it's clean.
	private TestRestTemplate stateful;

	@BeforeEach
	public void setup() {
		this.baseUrl = "http://localhost:" + port + "/events";
		this.loginUrl = "http://localhost:" + port + "/sign-in";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		httpEntity = new HttpEntity<String>(headers);
	}
	
	@Test
	public void testGetAllEvents() {
		ResponseEntity<String> response = template.exchange("/events", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	
	@Test
	public void testShowEventDetailPage() {
		ResponseEntity<String> response = template.exchange("/events/1", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
	}
	
	@Test
	public void testSearchEvent() {
		ResponseEntity<String> response = template.exchange("/events/byName?search=Test", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	
	@Test
	public void testShowUpdateEventPage() {
		//long id = eventService.findAll().iterator().next().getId();
		ResponseEntity<String> response = template.exchange(baseUrl+ "/updateEvent/5", HttpMethod.GET, httpEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	

	
	@Test
	public void testShowCreateEventPage() {
		
		ResponseEntity<String> response = template.exchange("/events/new", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	
	
	//normal data
	@Test
	public void testCreateEventSensibleData() {
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

		MultiValueMap<String, String> event = new LinkedMultiValueMap<String, String>();
		long id = venueService.findAll().iterator().next().getId();
		event.add("_csrf", csrfToken);
		event.add("name", "test name");
		event.add("venue.id", id + "");
		event.add("description", "test description");
		event.add("summary", "test summary");
		event.add("date", "2022-06-13");
		event.add("time", "20:00");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(event, postHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));	
	}
	 
	//Bad Data
	@Test
	public void testCreateEventBadData() {
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

		MultiValueMap<String, String> event = new LinkedMultiValueMap<String, String>();
		long id = venueService.findAll().iterator().next().getId();
		event.add("_csrf", csrfToken);
		event.add("name", "");
		event.add("venue.id", id + "");
		event.add("description", "test description");
		event.add("summary", "test summary");
		event.add("date", "2022-06-13");
		event.add("time", "20:00");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(event, postHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));		
	}
	
	//No Data
	@Test
	public void testCreateEventNoData() {
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

		MultiValueMap<String, String> event = new LinkedMultiValueMap<String, String>();
		event.add("_csrf", csrfToken);
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(event, postHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	
	
	@Test
	public void testCreateEventNoCSRF() {
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

		MultiValueMap<String, String> event = new LinkedMultiValueMap<String, String>();
		long id = venueService.findAll().iterator().next().getId();
		event.add("name", "test name");
		event.add("venue.id", id + "");
		event.add("description", "test description");
		event.add("summary", "test summary");
		event.add("date", "2022-06-13");
		event.add("time", "20:00");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(event, postHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));		
	}
	
	@Test
	public void testCreateEventNoLogin() {
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> event = new LinkedMultiValueMap<String, String>();
		long id = venueService.findAll().iterator().next().getId();
		event.add("name", "test name");
		event.add("venue.id", id + "");
		event.add("description", "test description");
		event.add("summary", "test summary");
		event.add("date", "2022-06-13");
		event.add("time", "20:00");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(event,
				postHeaders);

		ResponseEntity<String> response = template.exchange(baseUrl , HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
	}

	//Sensible Data
	@Test
	public void testUpdateEventSensibleData() {
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

		MultiValueMap<String, String> event = new LinkedMultiValueMap<String, String>();
		long id = venueService.findAll().iterator().next().getId();
		long idE = eventService.findAll().iterator().next().getId();
		event.add("_csrf", csrfToken);
		event.add("name", "update test name");
		event.add("id", idE+"");
		event.add("venue.id", id + "");
		event.add("description", "update test description");
		event.add("summary", "test description");
		event.add("date", "2022-06-13");
		event.add("time", "20:00");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(event, postHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl+ "/update/" + idE, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));

		
	}
	//Bad Data
	@Test
	public void testUpdateEventBadData() {
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

		MultiValueMap<String, String> event = new LinkedMultiValueMap<String, String>();
		long id = venueService.findAll().iterator().next().getId();
		long idE = eventService.findAll().iterator().next().getId();
		event.add("_csrf", csrfToken);
		event.add("name", "");
		event.add("venue.id", id + "");
		event.add("id", idE+"");
		event.add("description", "update test description");
		event.add("summary", "test description");
		event.add("date", "2022-06-13");
		event.add("time", "20:00");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(event, postHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl + "/update/" + idE, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

		
	}
	//No data
	@Test
	public void testUpdateEventNoData() {
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

		MultiValueMap<String, String> event = new LinkedMultiValueMap<String, String>();
		long idE = eventService.findAll().iterator().next().getId();
		event.add("_csrf", csrfToken);
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(event, postHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl + "/update/" + idE, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

		
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

		MultiValueMap<String, String> event = new LinkedMultiValueMap<String, String>();
		long id = venueService.findAll().iterator().next().getId();
		//event.add("_csrf", csrfToken);
		event.add("name", "update test name");
		event.add("venue.id", id + "");
		event.add("description", "update test description");
		event.add("summary", "test description");
		event.add("date", "2022-06-13");
		event.add("time", "20:00");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(event, postHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl + "/update/1", HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));

		
	}
	
	public void testUpdateEventNoLogin() {
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> event = new LinkedMultiValueMap<String, String>();
		long id = venueService.findAll().iterator().next().getId();
		event.add("name", "test name");
		event.add("venue.id", id + "");
		event.add("description", "test description");
		event.add("summary", "test summary");
		event.add("date", "2022-06-13");
		event.add("time", "20:00");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(event,
				postHeaders);

		ResponseEntity<String> response = template.exchange(baseUrl + "/update/1", HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
	}
	
	@Test
	public void testDeleteEventNoLogin() {
		
		HttpHeaders getHeaders = new HttpHeaders();		
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		MultiValueMap<String, String> event = new LinkedMultiValueMap<String, String>();
		long id = venueService.findAll().iterator().next().getId();
		event.add("name", "test name");
		event.add("venue.id", id + "");
		event.add("description", "test description");
		event.add("summary", "test summary");
		event.add("date", "2022-06-13");
		event.add("time", "20:00");
		HttpEntity<MultiValueMap<String, String>> deleteEntity = new HttpEntity<MultiValueMap<String, String>>(event, getHeaders);

		ResponseEntity<String> response = template.exchange(baseUrl + "/delete/1", HttpMethod.DELETE, deleteEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
		
	}
	
	@Test
	public void testDeleteEvent() {
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

		MultiValueMap<String, String> event = new LinkedMultiValueMap<String, String>();
		long id = venueService.findAll().iterator().next().getId();
		long idE = eventService.findAll().iterator().next().getId();
		event.add("_csrf", csrfToken);
		event.add("name", "update test name");
		event.add("venue.id", id + "");
		event.add("id", idE + "");
		event.add("description", "update test description");
		event.add("summary", "test description");
		event.add("date", "2022-06-13");
		event.add("time", "20:00");
		HttpEntity<MultiValueMap<String, String>> deleteEntity = new HttpEntity<MultiValueMap<String, String>>(event, getHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl + "/delete/" + idE, HttpMethod.DELETE, deleteEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));

		
	}
	
	@Test
	public void testDeleteEventNoCSRF() {
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

		MultiValueMap<String, String> event = new LinkedMultiValueMap<String, String>();
		long id = venueService.findAll().iterator().next().getId();
		event.add("name", "update test name");
		event.add("venue.id", id + "");
		event.add("description", "update test description");
		event.add("summary", "test description");
		event.add("date", "2022-06-13");
		event.add("time", "20:00");
		HttpEntity<MultiValueMap<String, String>> deleteEntity = new HttpEntity<MultiValueMap<String, String>>(event, getHeaders);
		ResponseEntity<String> response = stateful.exchange(baseUrl + "/delete/1", HttpMethod.DELETE, deleteEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));

		
	}
	
	//Sensible DATA
	@Test
	public void testPostTweet() {
		template = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);
		
		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		HttpHeaders postHeaders = new HttpHeaders();
		
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		//Login and get cookie session
		String cookie = integrationLogin(template, getHeaders, postHeaders);
		
		// Set the session cookie and GET the new event form so we can read
		// the new CSRF token.
		getHeaders.set("Cookie", cookie);
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> loginResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(loginResponse.getBody());
        
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("tweet", "test tweet");

		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
		ResponseEntity<String> response = template.exchange(baseUrl + "/tweet/1", HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));

		
	}
	
	//Sensible DATA
	@Test
	public void testPostTweetNoData() {
		
		template = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);
		
		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		HttpHeaders postHeaders = new HttpHeaders();
		
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		//Login and get cookie session
		String cookie = integrationLogin(template, getHeaders, postHeaders);
		
		// Set the session cookie and GET the new event form so we can read
		// the new CSRF token.
		getHeaders.set("Cookie", cookie);
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> loginResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(loginResponse.getBody());
        
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("tweet", "");

		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
		ResponseEntity<String> response = template.exchange(baseUrl + "/tweet/1", HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));

		
	}
	
	@Test
	public void testPostTweetNoCSRF() {
		template = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);
		
		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		HttpHeaders postHeaders = new HttpHeaders();
		
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		//Login and get cookie session
		String cookie = integrationLogin(template, getHeaders, postHeaders);
		
		// Set the session cookie and GET the new event form so we can read
		// the new CSRF token.
		getHeaders.set("Cookie", cookie);
        
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("tweet", "test tweet");

		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
		ResponseEntity<String> response = template.exchange(baseUrl + "/update/1", HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));

		
	}
	
	@Test
	public void testPostTweetNoLogin() {
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("tweet", "test tweet");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form,
				postHeaders);

		ResponseEntity<String> response = template.exchange(baseUrl + "/update/1", HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
	}
	
	
	public static String getCsrfToken(String body)
	{
		Pattern pattern = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
		Matcher matcher = pattern.matcher(body);
		assertThat(matcher.matches(), equalTo(true));
		return matcher.group(1);
	}
	
	public String integrationLogin(TestRestTemplate t, HttpHeaders getHeaders, HttpHeaders postHeaders)
	{
		
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> formResponse = t.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie").split(";")[0];
		HttpEntity<MultiValueMap<String, String>> postEntity;
		postHeaders.set("Cookie", cookie);
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Mustafa");
		login.add("password", "Mustafa");
		
		// Log in.
		postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				postHeaders);
		ResponseEntity<String> loginResponse = t.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
		
		return cookie;
	}
}
