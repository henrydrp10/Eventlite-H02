package uk.ac.man.cs.eventlite.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {

	@Autowired
	private VenueRepository venueRepository;

	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAll();
	}
	
	@Override
	public void deleteById(long id) {
		venueRepository.deleteById(id);
	}
	
	
	@Override
	public Venue save(Venue v) {
		return venueRepository.save(v);
	}
	
	@Override
	public Venue findOne(long id) {		
		return venueRepository.findById(id).orElse(null);
	}

	public Iterable<Venue> findAllByName (String search) {
		String regex = "\\b" + search.toUpperCase() + "\\b";
		return venueRepository.findAllByName(regex);
	}

}
