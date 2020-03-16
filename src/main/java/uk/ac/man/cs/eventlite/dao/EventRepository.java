package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventRepository extends JpaRepository<Event, Long>{

	@Query(value = "SELECT * FROM events WHERE UPPER(name) REGEXP ?1 ORDER BY date ASC, name ASC", nativeQuery = true)
	public Iterable<Event> findAllByName(String regex);
	
}
