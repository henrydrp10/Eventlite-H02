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

import uk.ac.man.cs.eventlite.config.Hateoas;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

	@Autowired
	private VenueService venueService;	
	
	@RequestMapping(method = RequestMethod.GET)
	public Resources<Resource<Venue>> getAllVenues() {

		return venueToResource(venueService.findAll());
	}
	
	@GetMapping(value = "/{venueId}")
	public Resource<Venue> showVenue(@PathVariable final Long venueId) {
		
		if(venueService.findOne(venueId)==null) return null;
		else return venueToResource(venueId); 
			
	}
	
	@GetMapping(value = "/{venueId}/events")
	public Resources<Resource<Event>> getEventsForVenue(@PathVariable final Long venueId) {
		
		List<Event> events = venueService.getEventsForVenue(venueId);
	    List<Resource<Event>> resources = new ArrayList<Resource<Event>>();
	    
	    for (final Event event : events) {
	    	
	        resources.add(eventToResource(event));
	        
	    }
	  
	    Link link = linkTo(methodOn(VenuesControllerApi.class)
	      .getEventsForVenue(venueId)).withSelfRel();
	    Resources<Resource<Event>> result = new Resources<Resource<Event>>(resources, link);
	    return result;
	} 
	
	@GetMapping(value = "/{venueId}/next3events")
	public Resources<Resource<Event>> getThreeNextEventsForVenue(@PathVariable final Long venueId) {
		
		List<Event> events = venueService.getThreeUpcomingEventsForVenue(venueId);
	    List<Resource<Event>> resources = new ArrayList<Resource<Event>>();
	    
	    for (final Event event : events) {
	    	
	        resources.add(eventToResource(event));
	        
	    }
	  
	    Link link = linkTo(methodOn(VenuesControllerApi.class)
	      .getThreeNextEventsForVenue(venueId)).withSelfRel();
	    Resources<Resource<Event>> result = new Resources<Resource<Event>>(resources, link);
	    return result;
	}
	
	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public ResponseEntity<?> newVenue() {
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createVenue(@RequestBody @Valid Venue venue, BindingResult result) {

		if (result.hasErrors()) {
			return ResponseEntity.unprocessableEntity().build();
		}

		venueService.save(venue);
		URI location = linkTo(VenuesControllerApi.class).slash(venue.getId()).toUri();

		return ResponseEntity.created(location).build();
	}
	
	@RequestMapping(value = "/byName", method = RequestMethod.GET)
	public Resources<Resource<Venue>> getVenuesByName(@RequestParam String search) {

		return venueToResource(venueService.findAllByName(search));
	} 

	private Resource<Venue> venueToResource(Venue venue) {
		Link selfLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).withSelfRel();

		return new Resource<Venue>(venue, selfLink);
	}
	
	private Resource<Venue> venueToResource(Long venueId) {
		
		Link selfLink = linkTo(VenuesControllerApi.class).slash(venueId).withSelfRel();
		
		Link venueLink = linkTo(methodOn(VenuesControllerApi.class)
				  .showVenue(venueId)).withRel("venue");
		
		//Links that only show up in venue/id page should be added here
		Link next3eventsLink = linkTo(methodOn(VenuesControllerApi.class)
				  .getThreeNextEventsForVenue(venueId)).withRel("next3events");
		
		//Link events = ...
		Link eventsForVenue = linkTo(methodOn(VenuesControllerApi.class)
				  .getEventsForVenue(venueId)).withRel("events");

		return new Resource<Venue>(venueService.findOne(venueId), selfLink, venueLink, next3eventsLink, eventsForVenue);	
		}

	private Resources<Resource<Venue>> venueToResource(Iterable<Venue> venues) {
		Link selfLink = linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel();
		Link profileLink = linkTo(Hateoas.class).slash("api").slash("profile").slash("venues").withRel("profile");

		List<Resource<Venue>> resources = new ArrayList<Resource<Venue>>();
		for (Venue venue : venues) {
			resources.add(venueToResource(venue));
		}

		return new Resources<Resource<Venue>>(resources, selfLink, profileLink);
	}
	
	private Resource<Event> eventToResource(Event event) {
		Link selfLink = linkTo(EventsControllerApi.class).slash(event.getId()).withSelfRel();

		return new Resource<Event>(event, selfLink);
	}
}
