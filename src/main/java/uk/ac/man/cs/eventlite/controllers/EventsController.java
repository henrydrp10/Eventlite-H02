package uk.ac.man.cs.eventlite.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
//import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) {

		model.addAttribute("events", eventService.findAll());
		model.addAttribute("venues", venueService.findAll());

		return "events/index";
	}
	

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String showEventDetails(@PathVariable("id") long id, Model model) {

		Event event = eventService.findOne(id);
		model.addAttribute("event", event);

		return "events/event_details";
	}
	
	@RequestMapping(value = "/byName", method = RequestMethod.GET)
	public String getEventsByName(Model model, @RequestParam String search) {
		
		Event query = new Event();
		query.setName(search);
		ExampleMatcher matcher = ExampleMatcher.matchingAny()
							.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
		Example<Event> eventExample = Example.of(query, matcher);
		
		model.addAttribute("events", eventService.findAll(eventExample));
		return "events/byName";
		
	}
	
	
	@GetMapping("/updateEvent/{id}")
    public String getEventToUpdate(Model model, @PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
	 
	 	Event event = eventService.findOne(id);
	 	if(event==null)
	 	{
	 		redirectAttrs.addFlashAttribute("error_message", "event not found");
	 	}
		model.addAttribute("event", event);
		model.addAttribute("venueList", venueService.findAll());
		
        return "events/updateEvent";
    }

	@RequestMapping(value="/update/{id}", method= RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String putEvent(@PathVariable("id") Long id, Event event) {
		 
		Event newEvent = eventService.findOne(id);
		newEvent.setName(event.getName());
		newEvent.setDate(event.getDate());
		newEvent.setTime(event.getTime());
		newEvent.setVenue(event.getVenue());
		newEvent.setSummary(event.getSummary());
		newEvent.setDescription(event.getDescription());
		
		eventService.save(newEvent);
		
		return "redirect:/events";
	}
}
