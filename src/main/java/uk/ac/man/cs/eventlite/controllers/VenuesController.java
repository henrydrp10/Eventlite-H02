package uk.ac.man.cs.eventlite.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventRepository;
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
	private EventRepository eventRepository;
	
	@Autowired
	private EventService eventService;
	
	String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoiZXZlbnRsaXRlaDAyIiwiYSI6ImNrOG44NjNrNTBrZGMzbW9jbGRqc3kxbXQifQ.H2MJkZCOBTT-X9_noMmreA";
	
	@RequestMapping(method = RequestMethod.GET)
	public String getAllVenues(Model model) {
		
		model.addAttribute("venuelist", venueService.findAll());
		
		return "venues/index";
	} 
	
	@RequestMapping(value = "/byName", method = RequestMethod.GET)
	public String getVenuesByName(Model model, @RequestParam String search) {
		
		model.addAttribute("venues", venueService.findAllByName(search));
		return "venues/byName";
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
		
		venue = venueService.updateLatLonIn(venue);  
        
		venueService.save(venue);
		redirectAttrs.addFlashAttribute("ok_message", "New venue added.");	
		
		return "redirect:/venues";
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String showVenueDetails(@PathVariable("id") long id, Model model) {

		Venue venue = venueService.findOne(id);
		
		if (venue != null) {
			
			List<Event> upcomingEventsInThisVenue = new ArrayList<Event>();
			Iterable<Event> events = eventService.findFuture();
			
			for(Event event : events) {
				if( event.getVenue() == venue) {
					upcomingEventsInThisVenue.add(event);
				}
			}	
			
			model.addAttribute("venue", venue);
			model.addAttribute("eventsf", upcomingEventsInThisVenue);
			
			

			return "venues/venue_details";
		}
		else {
			return "redirect:/venues"; 
		}
		
	}
	

	 @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
	 public String deleteById(@PathVariable("id") long id) {
         
		 boolean venueBelongsToEvent = false;
		 
		 Iterable<Event> events = eventRepository.findAll();
		 
		 for (Event event : events) {
				Venue v = event.getVenue();
				
				if (v.getId() == id)
					venueBelongsToEvent = true;
		 }
		 
		 if(!venueBelongsToEvent) {
		    venueService.deleteById(id);
		 }
		 
		 return "redirect:/venues"; 
	} 	

	@GetMapping("/updateVenue/{id}")
    public String getVenueToUpdate(Model model, @PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
	 
	 	Venue venue = venueService.findOne(id);
	 	
	 	if (venue == null) {
	 		redirectAttrs.addFlashAttribute("error_message", "venue not found");
	 	}
	 	
		model.addAttribute("venue", venue);
		
		redirectAttrs.addFlashAttribute("ok_message", "Venue updated.");	
		return "venues/updateVenue";
    }

	@RequestMapping(value="/update/{id}", method= RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String putVenue(@RequestBody @Valid @ModelAttribute Venue venue,
			BindingResult errors, Model model, @PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		
		Venue newVenue = venueService.findOne(id);	
		
		if (errors.hasErrors() ) {
			model.addAttribute("venue", venue);
			
			return "venues/updateVenue";
		}
		
		newVenue.setName(venue.getName());
		newVenue.setPostCode(venue.getPostCode());
		newVenue.setRoadName(venue.getRoadName());
		newVenue.setCapacity(venue.getCapacity());;
		
		venueService.save(venueService.updateLatLonIn(newVenue));

		return "redirect:/venues";
	}
}
