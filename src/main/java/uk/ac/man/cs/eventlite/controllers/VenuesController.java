package uk.ac.man.cs.eventlite.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })

public class VenuesController {

	@Autowired
	private VenueService venueService;
	@Autowired
	private EventService eventService;
	
	@RequestMapping(method = RequestMethod.GET)
	public String getAllVenues(Model model) {
		
		model.addAttribute("venues", venueService.findAll());
		
		return "events/index";
	}
	
	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public String newVenue(Model model) {
		if (!model.containsAttribute("venue")) {
			model.addAttribute("venue", new Venue());
		}

		return "venues/new";
	}
	
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createVenue(@RequestBody @Valid @ModelAttribute Venue venue, 
			BindingResult errors, Model model, RedirectAttributes redirectAttrs) {

		if (errors.hasErrors()) {
			model.addAttribute("venue", venue);
			
			return "venues/new";
		}

		venueService.save(venue);
		redirectAttrs.addFlashAttribute("ok_message", "New venue added.");	
		
		return "redirect:/events";
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String showVenueDetails(@PathVariable("id") long id, Model model) {

		Venue venue = venueService.findOne(id);
		List<Event> upcomingEventsInThisVenue = new ArrayList<Event>();
		Iterable<Event> events = eventService.findFuture();
		for(Event event : events)
		{
			if( event.getVenue() == venue)
			{
				upcomingEventsInThisVenue.add(event);
			}
			
		}	
		model.addAttribute("venue", venue);
		model.addAttribute("eventsf", upcomingEventsInThisVenue);
		
		

		return "venues/venue_details";
	}
	
}
