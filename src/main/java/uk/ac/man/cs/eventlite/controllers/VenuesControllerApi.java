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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.dao.VenueService;
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
