package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/profile", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class ProfileControllerApi {

	@RequestMapping(method = RequestMethod.GET)
	public Resource<String> getIndex()
	{
		Link selfLink = linkTo(ProfileControllerApi.class).withSelfRel();
		return new Resource<String>("profile", selfLink);
	}
	
}
