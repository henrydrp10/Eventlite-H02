package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@RestController
@RequestMapping(value = "/api", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class HomeControllerApi {

	@Autowired
	private EventService eventService;
	
	@RequestMapping(method = RequestMethod.GET)
	public Resource<String> body()
	{
		Link selfLink = linkTo(HomeControllerApi.class).withSelfRel();
		Link venueLink = linkTo(VenuesControllerApi.class).withRel("venues");
		Link eventLink = linkTo(EventsControllerApi.class).withRel("events");
		//update this if the profileController will be made in the future.
		Link profileLink = new Link("http://localhost:8080/api/profile").withRel("profile");
		
		//put the links relative to the controller in the future.
		Link threeUpcomingEventsLink = new Link("http://localhost:8080/api/getThreeUpcomingEvents").withRel("Three Upcoming Events");
		Link threeMostUsedVenues = new Link("http://localhost:8080/api/getThreeMostUsedVenues").withRel("Three Most Used Venues");
		return new Resource<String>("Home Page", venueLink, eventLink, profileLink, selfLink, threeUpcomingEventsLink, threeMostUsedVenues);
	}
	
	@RequestMapping(value = "/getThreeUpcomingEvents", method = RequestMethod.GET)
	public Resources<Resource<Event>> getThreeUpcomingEvents() {
		return eventToResource(eventService.threeUpcomingEvents());
	}

	private Resource<Event> eventToResource(Event event) {
		Link selfLink = linkTo(EventsControllerApi.class).slash(event.getId()).withSelfRel();

		return new Resource<Event>(event, selfLink);
	}
	
	private Resources<Resource<Event>> eventToResource(Iterable<Event> events) {
		Link selfLink = linkTo(methodOn(EventsControllerApi.class).getAllEvents()).withSelfRel();

		List<Resource<Event>> resources = new ArrayList<Resource<Event>>();
		for (Event event : events) {
			resources.add(eventToResource(event));
		}

		return new Resources<Resource<Event>>(resources, selfLink);
	}
	
	@RequestMapping(value = "/getThreeMostUsedVenues", method = RequestMethod.GET)
	public Resources<Resource<Venue>> getThreeMostUsedVenues() {

		return venueToResource(eventService.threeMostUsedVenues());
	}

	private Resource<Venue> venueToResource(Venue venue) {
		Link selfLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).withSelfRel();

		return new Resource<Venue>(venue, selfLink);
	}

	private Resources<Resource<Venue>> venueToResource(Iterable<Venue> venues) {
		Link selfLink = linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel();

		List<Resource<Venue>> resources = new ArrayList<Resource<Venue>>();
		for (Venue venue : venues) {
			resources.add(venueToResource(venue));
		}

		return new Resources<Resource<Venue>>(resources, selfLink);
	}
	
}
