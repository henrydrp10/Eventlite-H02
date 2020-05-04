package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")

public class VenuesControllerApiIntegrationTest  extends AbstractTransactionalJUnit4SpringContextTests {

	private HttpEntity<String> httpEntity;

	@Autowired
	private TestRestTemplate template;
    
	@LocalServerPort
	private int port;

	private String baseUrl;
	
	private final TestRestTemplate evil = new TestRestTemplate("Bad", "Person");
	
	@BeforeEach
	public void setup() {
		this.baseUrl = "http://localhost:" + port + "/api/events";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		httpEntity = new HttpEntity<String>(headers);
	}

	@Test
	public void testGetAllVenues() {
		ResponseEntity<String> response = template.exchange("/api/venues", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	
	@Test
	public void deleteVenueBadAuth() {
		HttpHeaders deleteHeaders = new HttpHeaders();
		deleteHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		deleteHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<String> deleteEntity = new HttpEntity<String>("{ \"name\": \"Venue, %s!\" }", deleteHeaders);
		
		ResponseEntity<String> response = evil.exchange(baseUrl, HttpMethod.DELETE, deleteEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
		//assertThat(4, equalTo(countRowsInTable("events")));
	}

	@Test
	public void postVenueBadAuth() {
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		postHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<String> postEntity = new HttpEntity<String>("{ \"name\": \"Test Venue New\" }", postHeaders);

		ResponseEntity<String> response = evil.exchange(baseUrl, HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
		//assertThat(4, equalTo(countRowsInTable("events")));
	}
}