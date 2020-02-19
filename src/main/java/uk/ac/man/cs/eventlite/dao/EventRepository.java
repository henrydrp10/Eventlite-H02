package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventRepository extends JpaRepository<Event, Long>{
	
}
