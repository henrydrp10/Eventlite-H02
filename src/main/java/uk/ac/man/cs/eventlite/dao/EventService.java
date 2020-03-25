package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public Event save(Event e);
	
	public Event findOne(long id);

	public void deleteById(long id);

	public Iterable<Event> findPast();
	
	public Iterable<Event> findFuture();

	public Iterable<Event> findAllByName(String regex);
}
