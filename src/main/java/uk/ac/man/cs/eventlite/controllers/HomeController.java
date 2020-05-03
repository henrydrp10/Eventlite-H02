package uk.ac.man.cs.eventlite.controllers;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;

@Controller
@RequestMapping(value = "/", produces = { MediaType.TEXT_HTML_VALUE })
public class HomeController {

	@Autowired
	private EventService eventService;
	
	private boolean hasRole(String role)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		Set<String> roles = authentication.getAuthorities().stream()
		     .map(r -> r.getAuthority()).collect(Collectors.toSet());
		
		return roles.contains("ROLE_"+role);
		
	}

	
	@RequestMapping(method = RequestMethod.GET)
	public String getThreeUpcomingEvents(Model model) {
		model.addAttribute("isAdmin", hasRole(Security.ADMIN_ROLE));
		model.addAttribute("eventsnext", eventService.threeUpcomingEvents());
		model.addAttribute("venuesnext", eventService.threeMostUsedVenues());


		return "../static/index";
	}

}
