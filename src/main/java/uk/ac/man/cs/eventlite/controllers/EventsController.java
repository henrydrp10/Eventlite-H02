package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestParam;

import uk.ac.man.cs.eventlite.dao.EventService;
//import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	// @Autowired
	// private VenueService venueService;

	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) {

		model.addAttribute("events", eventService.findAll());
		// model.addAttribute("venues", venueService.findAll());

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
							.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase();
		Example<Event> eventExample = Example.of(query, matcher);
		
		model.addAttribute("events", eventService.findAll(eventExample));
		return "events/byName";
	}
}
