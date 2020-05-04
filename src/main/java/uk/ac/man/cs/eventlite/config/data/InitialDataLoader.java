package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import uk.ac.man.cs.eventlite.dao.EventService;
//import uk.ac.man.cs.eventlite.dao.VenueRepository;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Component
@Profile({ "default", "test" })
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		if (eventService.count() > 0) {
			log.info("Database already populated. Skipping data initialization.");
			return;
		}
		
		// Build and save initial models here.
		
		//Please don't do requests for geolocation here.
		
		// Create test venues
		Venue venue1 = new Venue();
		venue1.setName("Test Venue 1");
		venue1.setCapacity(1000);
		venue1.setRoadName("King St");
		venue1.setPostCode("M2 1NL");
		venue1.setLatitude(53.481380);
		venue1.setLongitude(-2.246870);
		venueService.save(venue1);
		
		Venue venue2 = new Venue();
		venue2.setName("Test Venue 2");
		venue2.setCapacity(2000);
		venue2.setRoadName("Oxford Rd");
		venue2.setPostCode("M13 9GP");
		venue2.setLatitude(53.465820);
		venue2.setLongitude(-2.233390);
		venueService.save(venue2);
		
		Venue venue3 = new Venue();
		venue3.setName("Test Venue 3");
		venue3.setCapacity(3000);
		venue3.setRoadName("Portsmouth St");
		venue3.setPostCode("M13 9GB");
		venue3.setLatitude(53.463550);
		venue3.setLongitude(-2.229170);
		venueService.save(venue3);
		
		// Create test events
		Event event1 = new Event();
		event1.setName("Test Event 1");
		event1.setDate(LocalDate.parse("2020-02-13"));
		event1.setTime(LocalTime.parse("12:00"));

		event1.setDescription("Test description for Test Event 1");
		event1.setVenue(venue1);
		event1.setSummary("Test summary for Test Event 1");
		eventService.save(event1);
		
		Event event2 = new Event();
		event2.setName("Test Event 2");
		event2.setDate(LocalDate.parse("2020-02-14"));
		event2.setTime(LocalTime.parse("08:00"));
	
		event2.setDescription("Test description for Test Event 2");
		event2.setVenue(venue2);
		event2.setSummary("Test summary for Test Event 2");
		eventService.save(event2);
		
		Event event3 = new Event();
		event3.setName("Test Event 3");
		event3.setDate(LocalDate.parse("2022-02-13"));
		event3.setTime(LocalTime.parse("08:00"));

		event3.setDescription("Test description for Test Event 3");
		event3.setVenue(venue1);
		event3.setSummary("Test summary for Test Event 3");
		eventService.save(event3);
		
		Event event4 = new Event();
		event4.setName("Test Event 4");
		event4.setDate(LocalDate.parse("2022-02-14"));
		event4.setTime(LocalTime.parse("09:00"));

		event4.setDescription("Test description for Test Event 4");
		event4.setVenue(venue2);
		event4.setSummary("Test summary for Test Event 4");
		eventService.save(event4);
		
		Event event5 = new Event();
		event5.setName("Test Event 5");
		event5.setDate(LocalDate.parse("2022-02-14"));
		event5.setTime(LocalTime.parse("09:00"));

		event5.setDescription("Test description for Test Event 4");
		event5.setVenue(venue2);
		event5.setSummary("Test summary for Test Event 4");
		eventService.save(event5);
		
		Event event6 = new Event();
		event6.setName("Test Event 6");
		event6.setDate(LocalDate.parse("2022-02-14"));
		event6.setTime(LocalTime.parse("09:00"));

		event6.setDescription("Test description for Test Event 6");
		event6.setVenue(venue2);
		event6.setSummary("Test summary for Test Event 6");
		eventService.save(event6);
	}
}
