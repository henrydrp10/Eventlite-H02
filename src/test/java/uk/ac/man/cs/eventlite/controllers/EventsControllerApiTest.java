package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.man.cs.eventlite.testutil.MessageConverterUtil.getMessageConverters;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import javax.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerApiTest {
	
	private final static String BAD_ROLE = "USER";

	private MockMvc mvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@Mock
	private VenueService venueService;
	
	@Mock
	private Event event;
	
	@Mock
	private EventService eventService;

	@InjectMocks
	private EventsControllerApi eventsController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(eventsController).apply(springSecurity(springSecurityFilterChain))
				.setMessageConverters(getMessageConverters()).build();
	}

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event> emptyList());

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")));

		verify(eventService).findAll();
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		
		Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueService.save(v);
		
		Event e = new Event();
		e.setId(0);
		e.setName("Event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		e.setVenue(v);
		
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(1)));

		verify(eventService).findAll();
	}
	
	@Test
	public void getNewEvent() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/api/events/new").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isNotAcceptable()).andExpect(handler().methodName("newEvent"));
	}

	@Test
	public void postGreetingNoAuth() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/api/events").contentType(MediaType.APPLICATION_JSON)
				.content("{ \"name\": \"Test Event New\" }").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnauthorized());

		verify(eventService, never()).save(event);
	}

	@Test
	public void postEventBadAuth() throws Exception {
		mvc.perform(
				MockMvcRequestBuilders.post("/api/events").with(anonymous())
				.contentType(MediaType.APPLICATION_JSON).content("{ \"name\": \"Test Event New\" }")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

		verify(eventService, never()).save(event);
	}

	@Test
	public void postEventBadRole() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/api/events").with(user("Rob").roles(BAD_ROLE))
				.contentType(MediaType.APPLICATION_JSON).content("{ \"name\": \"Test Event New\" }")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

		verify(eventService, never()).save(event);
	}

	/*
	@Test
	public void postBadEvent() throws Exception {
		mvc.perform(
				MockMvcRequestBuilders.post("/api/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"template\": \"<Something bad>\" }").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity()).andExpect(content().string(""))
		.andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}
	*/

	@Test
	public void postLongEvent() throws Exception {
		mvc.perform(
				MockMvcRequestBuilders.post("/api/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"template\": \"abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyzabcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz\" }").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity()).andExpect(content().string(""))
		.andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}

	@Test
	public void postEmptyEvent() throws Exception {
		mvc.perform(
				MockMvcRequestBuilders.post("/api/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"template\": \"\" }").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity()).andExpect(content().string(""))
		.andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}
}