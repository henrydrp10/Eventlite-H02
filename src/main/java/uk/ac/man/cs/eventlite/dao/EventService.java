package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.domain.Example;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public Event save(Event e);
	
	public Event findOne(long id);
	
	public Iterable<Event> findAll(Example<Event> example);

	public void deleteById(long id);

}
