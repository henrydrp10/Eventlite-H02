package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.domain.Example;

import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();
	
	public Venue save(Venue v);
	
	public Iterable<Venue> findAllByName(String regex);
}
