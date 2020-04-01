package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import uk.ac.man.cs.eventlite.dao.EventService;

@Controller
@RequestMapping(value = "/", produces = { MediaType.TEXT_HTML_VALUE })
public class HomeController {

	@Autowired
	private EventService eventService;

	
	@RequestMapping(method = RequestMethod.GET)
	public String getThreeUpcomingEvents(Model model) {
		
		model.addAttribute("eventsnext", eventService.threeUpcomingEvents());
		model.addAttribute("venuesnext", eventService.threeMostUsedVenues());


		return "../static/index";
	}

}
