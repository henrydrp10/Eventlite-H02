package uk.ac.man.cs.eventlite.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import twitter4j.Status;
import twitter4j.TwitterException;
import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class VenueServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	@InjectMocks
	private EventServiceImpl eventServiceImpl;
	
	@Autowired
	@InjectMocks
	private VenueServiceImpl venueServiceImpl;
	
	//Mock the clock
	@Mock
	private Clock clock;

	//field that will contain the fixed clock
	private Clock fixedClock;
	
	@Autowired
	private EventRepository eventRepository;

	private static List<Event> events;
	
	@BeforeEach
	//This will set up a fixed clock in order to test the functionality
	public void initMocks() {
	    MockitoAnnotations.initMocks(this);

	    events = eventRepository.findAll();
	    if(events != null && events.size() > 0)
	    {
	    	//This can be made into different tests, each with a different clock.
	    	LocalDate date = events.get(events.size()/2).getDate();
	    	LocalTime time = events.get(events.size()/2).getTime().plusMinutes(2);
	    	fixedClock = Clock.fixed(Instant.parse(date.toString() + "T" + time.toString() + ":00.00Z"),
	    							 ZoneId.systemDefault());
	    }
	    else
	    	fixedClock = Clock.fixed(Instant.parse("2020-02-14T7:00:00.00Z"), ZoneId.systemDefault());
	    doReturn(fixedClock.instant()).when(clock).instant();
	    doReturn(fixedClock.getZone()).when(clock).getZone();
	}
	
	//Test search venue:
	@Test
	public void testGetVenuesByName() {
		Iterable<Venue> allVenues = venueServiceImpl.findAll();
		assertThat(((Collection<Venue>) allVenues).size(), is(3));
		
		// Case where the term is not complete (whole term match implementation)
		Iterable<Venue> venueList = venueServiceImpl.findAllByName("Venu");
		assertThat(((Collection<Venue>) venueList).size(), is(0));
		
		// Case where the term is complete, ignoring case (should return all)
		venueList = venueServiceImpl.findAllByName("VENUE");
		assertThat(((Collection<Venue>) venueList).size(), 
		   equalTo(((Collection<Venue>) allVenues).size()));
		for(Venue venue : venueList) {
			assertThat(allVenues, hasItem(venue));
		}
		
		// Case where looking for a specific venue (should return 1).
		venueList = venueServiceImpl.findAllByName("Test Venue 3");
		assertThat(((Collection<Venue>) venueList).size(), is(1));
		for(Venue venue : venueList) {
			assertThat(venue.getName(), equalTo("Test Venue 3"));
		}
	}
	
	// getEventsPerVenue Testing
	  // normal case
	@Test
	public void testEventsPerVenueNormalCase()
	{
	    Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueServiceImpl.save(v);
		
		Event e1 = new Event();
		e1.setId(1);
		e1.setName("Event");
		e1.setDate(LocalDate.now().plusDays(4));
		e1.setTime(LocalTime.now());
		e1.setVenue(v);
		eventServiceImpl.save(e1);
		
		Event e2 = new Event();
		e2.setId(1);
		e2.setName("Event");
		e2.setDate(LocalDate.now().plusDays(3));
		e2.setTime(LocalTime.now());
		e2.setVenue(v);
		eventServiceImpl.save(e2);
		
		Event e3 = new Event();
		e3.setId(1);
		e3.setName("Event");
		e3.setDate(LocalDate.now().plusDays(2));
		e3.setTime(LocalTime.now());
		e3.setVenue(v);
		eventServiceImpl.save(e3);
		
		Event e4 = new Event();
		e4.setId(1);
		e4.setName("Event");
		e4.setDate(LocalDate.now().plusDays(1));
		e4.setTime(LocalTime.now());
		e4.setVenue(v);
		eventServiceImpl.save(e4);
		
		Event e5 = new Event();
		e5.setId(1);
		e5.setName("Event");
		e5.setDate(LocalDate.now().minusDays(4));
		e5.setTime(LocalTime.now());
		e5.setVenue(v);
		eventServiceImpl.save(e5);
		
		List<Event> listEvents =  (List<Event>)venueServiceImpl.getEventsForVenue(v.getId());
		assertTrue(listEvents.size()==5);
		
	
	}
	//the case where there are no events per venue
	@Test
	public void testNoEventsPerVenueNormalCase()
	{
	    Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueServiceImpl.save(v);

		
		List<Event> listEvents =  (List<Event>)venueServiceImpl.getEventsForVenue(v.getId());
		assertTrue(listEvents.size()==0);
	
	}
	
	// getThreeUpcomingEventsPerVenue
	 //the case where there is three or more upcoming events for that venue
	@Test
	public void testFourUpcomingEventsButThreeEarliestAreReturned()
	{
	    Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueServiceImpl.save(v);
		
		Event e1 = new Event();
		e1.setId(1);
		e1.setName("Event");
		e1.setDate(LocalDate.now().plusDays(4));
		e1.setTime(LocalTime.now());
		e1.setVenue(v);
		eventServiceImpl.save(e1);
		
		Event e2 = new Event();
		e2.setId(1);
		e2.setName("Event");
		e2.setDate(LocalDate.now().plusDays(3));
		e2.setTime(LocalTime.now());
		e2.setVenue(v);
		eventServiceImpl.save(e2);
		
		Event e3 = new Event();
		e3.setId(1);
		e3.setName("Event");
		e3.setDate(LocalDate.now().plusDays(2));
		e3.setTime(LocalTime.now());
		e3.setVenue(v);
		eventServiceImpl.save(e3);
		
		Event e4 = new Event();
		e4.setId(1);
		e4.setName("Event");
		e4.setDate(LocalDate.now().plusDays(1));
		e4.setTime(LocalTime.now());
		e4.setVenue(v);
		eventServiceImpl.save(e4);
		
		Event e5 = new Event();
		e5.setId(1);
		e5.setName("Event");
		e5.setDate(LocalDate.now().minusDays(4));
		e5.setTime(LocalTime.now());
		e5.setVenue(v);
		eventServiceImpl.save(e5);
		
		List<Event> list3UpcomingEvents =  (List<Event>)venueServiceImpl.getThreeUpcomingEventsForVenue(v.getId());
		assertTrue(list3UpcomingEvents.size()==3);
		assertFalse(list3UpcomingEvents.contains(e5));
		List<Event> upcomingEvents = (List<Event>)eventServiceImpl.findFuture();
		int i = 0;
		for(Event e : upcomingEvents)
		{
			if(e.getVenue() == v)
			{
				if(i<3)
				{
					assertTrue(list3UpcomingEvents.contains(e));
					i++;
				}
				else
				{
					assertFalse(list3UpcomingEvents.contains(e));
				}
			}
	
		}
				
	}
	 //the case where there is less than 3 events for that venue
	
	@Test
	public void testLessThan3UpcomingEvents()
	{
	    Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueServiceImpl.save(v);
		
		Event e1 = new Event();
		e1.setId(1);
		e1.setName("Event");
		e1.setDate(LocalDate.now().plusDays(4));
		e1.setTime(LocalTime.now());
		e1.setVenue(v);
		eventServiceImpl.save(e1);
		
		Event e2 = new Event();
		e2.setId(1);
		e2.setName("Event");
		e2.setDate(LocalDate.now().plusDays(3));
		e2.setTime(LocalTime.now());
		e2.setVenue(v);
		eventServiceImpl.save(e2);
		
		Event e3 = new Event();
		e3.setId(1);
		e3.setName("Event");
		e3.setDate(LocalDate.now().minusDays(2));
		e3.setTime(LocalTime.now());
		e3.setVenue(v);
		eventServiceImpl.save(e3);
		
		
		List<Event> list3UpcomingEvents =  (List<Event>)venueServiceImpl.getThreeUpcomingEventsForVenue(v.getId());
		assertTrue(list3UpcomingEvents.size()<=3);
		assertFalse(list3UpcomingEvents.contains(e3));
		List<Event> upcomingEvents = (List<Event>)eventServiceImpl.findFuture();
		int i = 0;
		for(Event e : upcomingEvents)
		{
			if(e.getVenue() == v)
			{
				if(i<list3UpcomingEvents.size())
				{
					assertTrue(list3UpcomingEvents.contains(e));
					i++;
				}
				else
				{
					assertFalse(list3UpcomingEvents.contains(e));
				}
				
			}
			
	
		}
				
	}
	
	
	
	
	// updateLatLon
	
	   //The case where venue given is null
	/*
	 * This is a test for no results
	@Test
	public void updateLatLonInNullVenue()
	{
		Venue v = venueServiceImpl.updateLatLonIn(null);
		assertTrue(v.getLatitude() == 1000 );
		assertTrue(v.getLongitude() == 1000 );
	}
	*/ 
	   // the case when an actual venue is given and the status should be okay
	@Test
	public void updateLatLonInNormalVenue()
	{
		
		Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		v.setRoadName("Ossory Street 39");
		v.setPostCode("M144BX");
		venueServiceImpl.save(v);
		
		v = venueServiceImpl.updateLatLonIn(v);
	    assertTrue(v.getLatitude() == 53.455988);
	    assertTrue(v.getLongitude() == -2.230808);
	}
	    
	
}
