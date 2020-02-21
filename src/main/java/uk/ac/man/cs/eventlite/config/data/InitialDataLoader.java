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
import uk.ac.man.cs.eventlite.dao.VenueRepository;
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
		
		// Create test venues
		Venue venue1 = new Venue();
		venue1.setName("Test Venue 1");
		venue1.setCapacity(1000);
		venueService.save(venue1);
		
		Venue venue2 = new Venue();
		venue2.setName("Test Venue 2");
		venue2.setCapacity(2000);
		venueService.save(venue2);
		
		// Create test events
		Event event1 = new Event();
		event1.setName("Test Event 1");
		event1.setDate(LocalDate.parse("2020-02-13"));
		event1.setTime(LocalTime.parse("12:00"));

		event1.setDescription("A joana e linda");
		event1.setVenue(venue1);
		eventService.save(event1);
		
		Event event2 = new Event();
		event2.setName("Test Event 2");
		event2.setDate(LocalDate.parse("2020-02-14"));
		event2.setTime(LocalTime.parse("08:00"));
	
		event2.setDescription("A joana e linda");
		event2.setVenue(venue2);
		eventService.save(event2);
		
		Event event3 = new Event();
		event3.setName("Test Event 3");
		event3.setDate(LocalDate.parse("2020-02-13"));
		event3.setTime(LocalTime.parse("08:00"));

		event3.setDescription("A joana e linda");
		event3.setVenue(venue1);
		eventService.save(event3);
		
		Event event4 = new Event();
		event4.setName("Test Event 4");
		event4.setDate(LocalDate.parse("2020-02-14"));
		event4.setTime(LocalTime.parse("09:00"));

		event4.setDescription("A joana e linda");
		event4.setVenue(venue2);
		eventService.save(event4);
	}
}
