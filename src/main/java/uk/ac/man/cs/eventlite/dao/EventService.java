package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.List;

import twitter4j.Status;
import twitter4j.TwitterException;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public Event save(Event e);
	
	public Event findOne(long id);

	public void deleteById(long id);

	public Iterable<Event> findPast();
	
	public Iterable<Event> findFuture();
	
	public Iterable<Event> threeUpcomingEvents();

	public Iterable<Event> findAllByName(String regex);
	
	public Iterable<Venue> threeMostUsedVenues();
	
	public String createTweet(String tweet) throws TwitterException;

	public List<Status> getLastFiveStatusesFromTimeline() throws TwitterException;

	public boolean isEventPast(Event event);
}
