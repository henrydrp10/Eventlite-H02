package uk.ac.man.cs.eventlite.dao;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class EventServiceImpl implements EventService {
	
	@Bean
	public Clock clock() {
	    return Clock.systemDefaultZone();
	}
	
	@Autowired
    public Clock clock;
	
	@Autowired
	private EventRepository eventRepository;

	@Override
	public long count() {		
		return eventRepository.count();
	}

	@Override
	public Iterable<Event> findAll() {
		Sort sortRule = Sort.by(Sort.Direction.ASC, "date");
		return eventRepository.findAll(sortRule.and(Sort.by(Sort.Direction.ASC, "name")));
	}
	
	@Override
	public Iterable<Event> findPast() {
		Sort sortRule = Sort.by(Sort.Direction.ASC, "date");
		Iterable<Event> events = eventRepository.findAll(sortRule.and(Sort.by(Sort.Direction.DESC, "name")));
		List<Event> pastEvents = new ArrayList<Event>();
		LocalDate currentTimeStamp = LocalDate.now(clock);
		
		for(Event event : events)
		{
			LocalDate eventDate = event.getDate();
			if(currentTimeStamp.isAfter(eventDate))
			{
				pastEvents.add(event);
			}
			else if(currentTimeStamp.getDayOfYear() != eventDate.getDayOfYear() 
					|| currentTimeStamp.getYear() != eventDate.getYear())
				break;
		}	
		
		Collections.reverse(pastEvents);
		
		return pastEvents;
	}
	
	@Override
	public Iterable<Event> findFuture() {
		Sort sortRule = Sort.by(Sort.Direction.DESC, "date");
		Iterable<Event> events = eventRepository.findAll(sortRule.and(Sort.by(Sort.Direction.DESC, "name")));
		List<Event> futureEvents = new ArrayList<Event>();
		LocalDate currentTimeStamp = LocalDate.now(clock);
		
		for(Event event : events)
		{
			LocalDate eventDate = event.getDate();
			if(!currentTimeStamp.isAfter(eventDate))
			{
				futureEvents.add(event);
			}
			else if(currentTimeStamp.getDayOfYear() != eventDate.getDayOfYear() 
					|| currentTimeStamp.getYear() != eventDate.getYear())
				break;	
		}	
		
		Collections.reverse(futureEvents);
		
		return futureEvents;
	}
	
	@Override
	public Iterable<Event> threeUpcomingEvents() {
		Iterable<Event> futureEvents = findFuture();
		while (((List<Event>) futureEvents).size() > 3) {
			int lastIndex = ((List<Event>) futureEvents).size() - 1;
			((List<Event>) futureEvents).remove(lastIndex);
		}
		return futureEvents;
	}

	public Iterable<Venue> threeMostUsedVenues(){
		Iterable<Event> events = eventRepository.findAll(Sort.by(Sort.Direction.DESC, "name"));
		
		// All the venues with how many times they're used by events
		Map<Venue, Integer> allVenues = new HashMap<Venue, Integer>();

		//Get all Venues and count how many times they're used by events
		for(Event event : events)
		{
			if(!allVenues.containsKey(event.getVenue()))
			{
				allVenues.put(event.getVenue(), 0);
			}
			else
			{
				allVenues.replace(event.getVenue(), allVenues.get(event.getVenue()) + 1);
			}
		}
		
		// List of the three venues with the most events
		List<Venue> topThreeUsedVenues = new ArrayList<Venue>();
		
		// Finds the three venues with the most events
		for(int i = 0; i < 3; i++)
		{
			Venue mostUsedVenue = Collections.max(allVenues.entrySet(), Map.Entry.comparingByValue()).getKey();
			topThreeUsedVenues.add(mostUsedVenue);
			allVenues.remove(mostUsedVenue);
		}

		return topThreeUsedVenues;
	}

	@Override
	public Event save(Event e) {		
		return eventRepository.save(e);
	}
	
	@Override
	public Event findOne(long id) {		
		return eventRepository.findById(id).orElse(null);
	}

	@Override
	public Iterable<Event> findAllByName (String search) {
		String regex = "\\b" + search.toUpperCase() + "\\b";
		return eventRepository.findAllByName(regex);
	}
	
	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}
}
