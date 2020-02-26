package uk.ac.man.cs.eventlite.dao;

import org.springframework.beans.factory.annotation.Autowired;
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
		return eventRepository.findAll(sortRule.and(Sort.by(Sort.Direction.ASC, "time")));
	}
	
	@Override
	public Iterable<Event> findPast() {
		Sort sortRule = Sort.by(Sort.Direction.ASC, "date");
		return eventRepository.findAll(sortRule.and(Sort.by(Sort.Direction.ASC, "time")));
	}
	
	@Override
	public Iterable<Event> findPresent() {
		Sort sortRule = Sort.by(Sort.Direction.ASC, "date");
		return eventRepository.findAll(sortRule.and(Sort.by(Sort.Direction.ASC, "time")));
	}
	
	@Override
	public Event save(Event e) {		
		return eventRepository.save(e);
	}
	
	@Override
	public Event findOne(long id) {		
		return eventRepository.findById(id).orElse(null);
	}
}
