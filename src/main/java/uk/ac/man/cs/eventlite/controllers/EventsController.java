package uk.ac.man.cs.eventlite.controllers;

import java.time.Clock;
import java.time.LocalDate;

//import utils.EventsFormBuilder;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import twitter4j.TwitterException;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })

public class EventsController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;
	
	String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoiZXZlbnRsaXRlaDAyIiwiYSI6ImNrOG44NjNrNTBrZGMzbW9jbGRqc3kxbXQifQ.H2MJkZCOBTT-X9_noMmreA";

	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) throws TwitterException {

		model.addAttribute("events", eventService.findAll());
		model.addAttribute("venues", venueService.findAll());
		
		model.addAttribute("eventsp", eventService.findPast());
		model.addAttribute("eventsf", eventService.findFuture());
		
		model.addAttribute("lastFiveStatuses", eventService.getLastFiveStatusesFromTimeline());

		return "events/index";
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String showEventDetails(@PathVariable("id") long id, Model model) {

		Event event = eventService.findOne(id);
		if(event != null)
		{
			
			model.addAttribute("event", event);
			model.addAttribute("lat", event.getVenue().getLatitude());
			model.addAttribute("lon", event.getVenue().getLongitude());
			return "events/event_details";
			
		}
		else
		{
			return "redirect:/events";
		}
		
		
	}
	
	
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
	public String deleteById(@PathVariable("id") long id) {
        
		eventService.deleteById(id);
		
		return "redirect:/events";
	}

	@RequestMapping(value = "/byName", method = RequestMethod.GET)
	public String getEventsByName(Model model, @RequestParam String search) {
		
		model.addAttribute("events", eventService.findAllByName(search));
		return "events/byName";


	}
	
	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public String newEvent(Model model) {
		if (!model.containsAttribute("event")) {
			model.addAttribute("event", new Event());
		}
		
		if (!model.containsAttribute("venueList")) {
			model.addAttribute("venueList", venueService.findAll());
		}

		return "events/new";
	}
	
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createEvent(@RequestBody @Valid @ModelAttribute Event event, 
			BindingResult errors, Model model, RedirectAttributes redirectAttrs) {

		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			model.addAttribute("venueList", venueService.findAll());
			
			return "events/new";
		}

		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "New event added.");	
		
		return "redirect:/events";
	}
	
	
	@GetMapping("/updateEvent/{id}")
    public String getEventToUpdate(Model model, @PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
	 
	 	Event event = eventService.findOne(id);
	 	
	 	if(event==null) {
	 		redirectAttrs.addFlashAttribute("error_message", "event not found");
	 	}
	 	
		model.addAttribute("event", event);
		model.addAttribute("venueList", venueService.findAll());
		
        return "events/updateEvent";
    }
	
	@Autowired
    public Clock clock;

	@RequestMapping(value="/update/{id}", method= RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	//public String putEvent(@PathVariable("id") Long id, Model model, Event event, BindingResult errors) {
	public String putEvent(@RequestBody @Valid @ModelAttribute Event event,  BindingResult errors, Model model, @PathVariable("id") long id, RedirectAttributes redirectAttrs ) {
		Event newEvent = eventService.findOne(id);	
// || event.getName()=="" || eventService.isEventPast(event)  
		if (errors.hasErrors() 	)
		{
			model.addAttribute("event", newEvent);
			model.addAttribute("venueList", venueService.findAll());
			return "events/updateEvent";
		}

		newEvent.setName(event.getName());
		newEvent.setDate(event.getDate());
		newEvent.setTime(event.getTime());
		newEvent.setVenue(event.getVenue());
		newEvent.setSummary(event.getSummary());
		newEvent.setDescription(event.getDescription());
		
		eventService.save(newEvent);

		return "redirect:/events";
	}
	
	@RequestMapping(value="/tweet/{id}", method= RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String updateStatusOnTwitter(@PathVariable("id") Long id, String tweet, RedirectAttributes redirectAttrs) {
		
		try {
			eventService.createTweet(tweet);
			redirectAttrs.addFlashAttribute("tweetString",tweet);

		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "redirect:/events/{id}";
	}
}
