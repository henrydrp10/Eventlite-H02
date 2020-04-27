package uk.ac.man.cs.eventlite.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class VenueServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private VenueService venueService;

	// This class is here as a starter for testing any custom methods within the
	// VenueService. Note: It is currently @Ignored!
	
	@BeforeEach
	public void setup() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
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
