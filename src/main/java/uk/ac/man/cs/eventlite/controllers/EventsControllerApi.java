package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;

@RestController
@RequestMapping(value = "/api/events", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class EventsControllerApi {

	@Autowired
	private EventService eventService;

	@RequestMapping(method = RequestMethod.GET)
	public Resources<Resource<Event>> getAllEvents() {

		return eventToResource(eventService.findAll());
	}
	
	@RequestMapping(value = "/byName", method = RequestMethod.GET)
	public Resources<Resource<Event>> getEventsByName(@RequestParam String search) {

		return eventToResource(eventService.findAllByName(search));
	}
	
	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public ResponseEntity<?> newEvent() {
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
	}
	

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createEvent(@RequestBody @Valid Event event, BindingResult result) {

		if (result.hasErrors()) {
			return ResponseEntity.unprocessableEntity().build();
		}

		eventService.save(event);
		URI location = linkTo(EventsControllerApi.class).slash(event.getId()).toUri();

		return ResponseEntity.created(location).build();
	}
	
	@GetMapping(value = "/{eventId}")
	public Resource<Event> showEvent(@PathVariable final Long eventId) {
		
		if(eventService.findOne(eventId)==null) 
			return null;
		else 
			return eventToResource(eventId);   		        
	}
	
	private Resource<Event> eventToResource(Long eventId) {
		Link selfLink = linkTo(EventsControllerApi.class).slash(eventId).withSelfRel();
		Link eventLink = linkTo(methodOn(EventsControllerApi.class).showEvent(eventId)).withRel("event");
		Link venueLink = linkTo(EventsControllerApi.class).slash(eventId).slash("venue").withRel("venue");

		return new Resource<Event>(eventService.findOne(eventId), selfLink,eventLink, venueLink);
	}


	private Resource<Event> eventToResource(Event event) {
		Link selfLink = linkTo(EventsControllerApi.class).slash(event.getId()).withSelfRel();
		Link venueLink = linkTo(EventsControllerApi.class).slash(event.getId()).slash("venue").withRel("venue");

		return new Resource<Event>(event, selfLink, venueLink);
	}

	private Resources<Resource<Event>> eventToResource(Iterable<Event> events) {
		Link selfLink = linkTo(methodOn(EventsControllerApi.class).getAllEvents()).withSelfRel();

		List<Resource<Event>> resources = new ArrayList<Resource<Event>>();
		for (Event event : events) {
			resources.add(eventToResource(event));
		}

		return new Resources<Resource<Event>>(resources, selfLink);
	}
}
