package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private HttpEntity<String> httpEntity;

	@Autowired
	private TestRestTemplate template;
	
	@Autowired
	private EventService eventService;

	@BeforeEach
	public void setup() {
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
	public void testGetEventsByName() {
		Iterable<Event> allEvents = eventService.findAll();
		assertThat(((Collection<Event>) allEvents).size(), is(4));
		
		// Case where the term is not complete (whole term match implementation)
		Iterable<Event> eventList = eventService.findAllByName("Even");
		assertThat(((Collection<Event>) eventList).size(), is(0));
		
		// Case where the term is complete, ignoring case (should return all)
		eventList = eventService.findAllByName("EVENT");
		assertThat(((Collection<Event>) eventList).size(), 
		   equalTo(((Collection<Event>) allEvents).size()));
		for(Event event : eventList) {
			assertThat(allEvents, hasItem(event));
		}
		
		// Case where looking for a specific event (should return 1).
		eventList = eventService.findAllByName("Test Event 3");
		assertThat(((Collection<Event>) eventList).size(), is(1));
		for(Event event : eventList) {
			assertThat(event.getName(), equalTo("Test Event 3"));
		}
	}

}
