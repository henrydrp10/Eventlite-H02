package uk.ac.man.cs.eventlite.controllers;


import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;


import org.springframework.web.bind.annotation.RequestMapping;



@Controller
@RequestMapping(value = "/error", produces = { MediaType.TEXT_HTML_VALUE })
public class ErrorsController implements ErrorController{

	
	@RequestMapping("/error")
	public String handleError() {

		return "/error/error";
	}
	
	@Override
	public String getErrorPath()
	{
		return "/error";
	}
	

	
	

}
