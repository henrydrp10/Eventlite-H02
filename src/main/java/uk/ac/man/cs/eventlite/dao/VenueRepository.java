package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueRepository extends CrudRepository<Venue, Long>{
	
	@Query(value = "SELECT * FROM venues WHERE UPPER(name) REGEXP ?1 ORDER BY name ASC", nativeQuery = true)
	public Iterable<Venue> findAllByName(String regex);
}
