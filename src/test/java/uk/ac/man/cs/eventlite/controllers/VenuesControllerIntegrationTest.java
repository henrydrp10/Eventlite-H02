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
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private HttpEntity<String> httpEntity;

	@Autowired
	private TestRestTemplate template;
	
	@Autowired
	private VenueService venueService;

	@BeforeEach
	public void setup() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		httpEntity = new HttpEntity<String>(headers);
	}

	
	@Test
	public void testGetVenuesByName() {
		Iterable<Venue> allVenues = venueService.findAll();
		assertThat(((Collection<Venue>) allVenues).size(), is(3));
		
		// Case where the term is not complete (whole term match implementation)
		Iterable<Venue> venueList = venueService.findAllByName("Venu");
		assertThat(((Collection<Venue>) venueList).size(), is(0));
		
		// Case where the term is complete, ignoring case (should return all)
		venueList = venueService.findAllByName("VENUE");
		assertThat(((Collection<Venue>) venueList).size(), 
		   equalTo(((Collection<Venue>) allVenues).size()));
		for(Venue venue : venueList) {
			assertThat(allVenues, hasItem(venue));
		}
		
		// Case where looking for a specific venue (should return 1).
		venueList = venueService.findAllByName("Test Venue 3");
		assertThat(((Collection<Venue>) venueList).size(), is(1));
		for(Venue venue : venueList) {
			assertThat(venue.getName(), equalTo("Test Venue 3"));
		}
	}

}
