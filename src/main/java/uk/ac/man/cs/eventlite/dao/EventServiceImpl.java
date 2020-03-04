package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {
	
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
		LocalDate currentTimeStamp = LocalDate.now();
		
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
		LocalDate currentTimeStamp = LocalDate.now();
		
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
	public Event save(Event e) {		
		return eventRepository.save(e);
	}
	
	@Override
	public Event findOne(long id) {		
		return eventRepository.findById(id).orElse(null);
	}
	
	@Override
	public Iterable<Event> findAll(Example<Event> example) {
		Sort sortRule = Sort.by(Sort.Direction.ASC, "date");
		return eventRepository.findAll(example, sortRule.and(Sort.by(Sort.Direction.ASC, "name")));
	}
	
	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}
}
