package uk.ac.man.cs.eventlite.dao;
import java.util.List;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();
		
	public Venue save(Venue v);
	
	public Venue updateLatLonIn(Venue v);
	
	public Venue findOne(long id);
	
	public void deleteById(long id);

	public Iterable<Venue> findAllByName(String regex);
	
	public List<Event> getThreeUpcomingEventsForVenue(Long venueId) ;
	
	public List<Event> getEventsForVenue(Long venueId);
	
}
