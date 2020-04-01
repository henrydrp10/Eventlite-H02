package uk.ac.man.cs.eventlite.dao;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	
    @Autowired
    private VenueService venueService;

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
		int[] venueTimes = new int[((int)venueService.count())];
		for(int i = 0; i < venueService.count(); i++) {
			venueTimes[i] = 0;
		}
		
		List<Venue> venueList = new ArrayList<Venue>();
		List<Venue> mostUsedVenues = new ArrayList<Venue>();

		//Get all Venues and count how many times they're used by events
		for(Event event : events)
		{
			Venue v = event.getVenue();
			if(!venueList.contains(v)) {
				venueList.add(v);
			}
			venueTimes[venueList.indexOf(v)]++;
		}
		
		System.out.println(venueList);
		System.out.println(venueTimes.length);
		
		
		int maxIndex;
		for(int a = 0; a < venueList.size() && a < 3; a++) {
			maxIndex = 0;
			for(int i = 0; i < venueTimes.length; i++) {
				if(venueTimes[i] > venueTimes[maxIndex]) {
					maxIndex = i;
				}
			}
			if(venueTimes[maxIndex] != -1) {
				mostUsedVenues.add(venueList.get(maxIndex));
			}
			venueTimes[maxIndex] = -1;
		}
		
		return mostUsedVenues;
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
