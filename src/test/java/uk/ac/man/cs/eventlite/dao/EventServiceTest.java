package uk.ac.man.cs.eventlite.dao;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	//@InjectMocks
	//private EventService eventService;
	
	@Autowired
	@InjectMocks
	private EventServiceImpl eventServiceImpl;
	
	//Mock the clock
	@Mock
	private Clock clock;

	//field that will contain the fixed clock
	private Clock fixedClock;
	
	@Autowired
	private EventRepository eventRepository;

	private static List<Event> events;
	private static List<Event> futureEvents;
	private static List<Event> pastEvents;
	
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
	
	//Testing twitter list
	@Test
	public void testAtMost5TweetsReturn()
	{
		try {
			List<Status> tweets = eventServiceImpl.getLastFiveStatusesFromTimeline();
			assertTrue(tweets.size() <= 5 && tweets.size() >= 0, "The tweeter feed must have at most 5 tweets");
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//testing the findFuture() method.
	@Test
	public void testFutureEvents()
	{
		futureEvents = (List<Event>)eventServiceImpl.findFuture();		
		if(futureEvents.size() > 0)
		{
			Event previous = futureEvents.get(0);
			LocalDate previousEventDate = previous.getDate();
			LocalDate currentTimeStamp = LocalDate.now(fixedClock);
			assertTrue(!currentTimeStamp.isAfter(previousEventDate));
			for(int index = 1; index < futureEvents.size(); index++)
			{
				assertTrue(!currentTimeStamp.isAfter(futureEvents.get(index).getDate()));
				if(currentTimeStamp.getDayOfYear() == futureEvents.get(index).getDate().getDayOfYear()
				   && currentTimeStamp.getYear() == futureEvents.get(index).getDate().getYear())
					assertTrue(futureEvents.get(index).getName().compareTo(previous.getName()) >= 0);
				
				previous = futureEvents.get(index);
				previousEventDate = previous.getDate();
			}
		}
	}
	
	//testing the findPast() method.
	@Test
	public void testPastEvents()
	{
		pastEvents = (List<Event>)eventServiceImpl.findPast();
		if(pastEvents.size() > 0)
		{
			Event previous = pastEvents.get(0);
			LocalDate previousEventDate = previous.getDate();
			LocalDate currentTimeStamp = LocalDate.now(fixedClock);
			assertTrue(currentTimeStamp.isAfter(previousEventDate), currentTimeStamp + " " + previousEventDate);
			for(int index = 1; index < pastEvents.size(); index++)
			{
				assertTrue(currentTimeStamp.isAfter(pastEvents.get(index).getDate()));
				if(currentTimeStamp.getDayOfYear() == pastEvents.get(index).getDate().getDayOfYear()
				   && currentTimeStamp.getYear() == pastEvents.get(index).getDate().getYear())
					assertTrue(pastEvents.get(index).getName().compareTo(previous.getName()) >= 0);
				
				previous = pastEvents.get(index);
				previousEventDate = previous.getDate();
			}
		}
	}
	
	//Checks if there was a missing event.
	@AfterAll
	public static void testEventIntegrity()
	{
		List<Event> eventsUnion = futureEvents;
		eventsUnion.addAll(pastEvents);
		assertTrue(eventsUnion.size() == events.size());
	}
}
