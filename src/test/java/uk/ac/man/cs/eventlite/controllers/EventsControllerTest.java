package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import org.mockito.ArgumentCaptor;
import uk.ac.man.cs.eventlite.config.Security;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerTest {
	
	private final static String BAD_ROLE = "USER";

	private MockMvc mvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@Mock
	private EventService eventService;

	@Mock
	private VenueService venueService;

	@InjectMocks
	private EventsController eventsController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(eventsController).apply(springSecurity(springSecurityFilterChain))
				.build();
	}

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event> emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		// verify(venueService).findAll();
		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event> singletonList(event));
		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		// verify(venueService).findAll();
		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}

	
	@Test
	public void getEvent() throws Exception {
	    Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueService.save(v);
		
		Event e = new Event();
		e.setId(1);
		e.setName("Event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		e.setVenue(v);
		long id = e.getId();
		when(eventService.findOne(id)).thenReturn(e);

		mvc.perform(MockMvcRequestBuilders.get("/events/"+id).accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
		.andExpect(view().name("events/event_details")).andExpect(handler().methodName("showEventDetails"));
		verify(eventService).findOne(id);
	}
	
	@Test
	public void getEventWhenEventDoesNotExist() throws Exception {

		mvc.perform(MockMvcRequestBuilders.get("/events/1000").accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
		.andExpect(view().name("redirect:/events")).andExpect(handler().methodName("showEventDetails"));
		verify(eventService).findOne(1000);
	}
	
	

	public void getNewEventNoAuth() throws Exception {		
		mvc.perform(MockMvcRequestBuilders.post("/events")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "abcdefghij").accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isFound()).andExpect(header().string("Location", endsWith("/sign-in")));
	}

	@Test
	public void getNewEvent() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/events/new").with(user("Rob").roles(Security.ADMIN_ROLE))
				.accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk()).andExpect(view().name("events/new"))
		.andExpect(handler().methodName("newEvent"));
	}

	@Test
	public void postEventNoAuth() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Event New").accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
		.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(eventService, never()).save(event);
	}

	@Test
	public void postEventBadRole() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/events").with(user("Rob").roles(BAD_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "Test Event 1")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isForbidden());

		verify(eventService, never()).save(event);
	}

	@Test
	public void postEventNoCsrf() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "Test Event 1")
				.accept(MediaType.TEXT_HTML)).andExpect(status().isForbidden());

		verify(eventService, never()).save(event);
	}

	@Test
	public void postEvent() throws Exception {
		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);

		mvc.perform(MockMvcRequestBuilders.post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Event New")
				.param("date", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
				.param("venue", venue.getName())
				.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/events")).andExpect(model().hasNoErrors())
		.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeExists("ok_message"));

		verify(eventService).save(arg.capture());
		assertThat("Test Event New", equalTo(arg.getValue().getName()));
	}


	@Test
	public void postLongEvent() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyzabcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz").accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isOk()).andExpect(view().name("events/new"))
		.andExpect(model().attributeHasFieldErrors("event", "name"))
		.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
	}

	@Test
	public void postEmptyEvent() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "").accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
		.andExpect(view().name("events/new"))
		.andExpect(model().attributeHasFieldErrors("event", "name"))
		.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
	}
	
	@WithMockUser(username = "Mustafa", password = "Mustafa", roles= {"ADMINISTRATOR"})
	public void deleteEventByName() throws Exception {
		when(eventService.findOne(1)).thenReturn(event);
	
		mvc.perform(MockMvcRequestBuilders.delete("/events/delete/1").accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/events"))
		.andExpect(handler().methodName("deleteById"));
	
		verify(eventService).deleteById(1);
	}
 
	@Test
	@WithMockUser(username = "Mustafa", password = "Mustafa", roles= {"USER"})
	public void deleteEventByNameUnauthorisedUser() throws Exception {
		when(eventService.findOne(1)).thenReturn(event);
	
		mvc.perform(MockMvcRequestBuilders.delete("/events/delete/1").accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isForbidden());
	
		verify(eventService, never()).deleteById(1);
	}
	
	@Test
	@WithMockUser(username = "Mustafa", password = "Mustafa", roles= {"ADMINISTRATOR"})
	public void deleteEventByNameNoCsrf() throws Exception {
		when(eventService.findOne(1)).thenReturn(event);
	
		mvc.perform(MockMvcRequestBuilders.delete("/events/delete/1").accept(MediaType.TEXT_HTML))
		.andExpect(status().isForbidden());
	
		verify(eventService, never()).deleteById(1);
	}
	
	@Test
	public void putEvent() throws Exception {
		
	    Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueService.save(v);
		
		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		
		when(eventService.findOne(1)).thenReturn(event);

		mvc.perform(MockMvcRequestBuilders.post("/events/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Event New")
				.param("date", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
				.param("venue", venue.getName())
				.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/events")).andExpect(model().hasNoErrors())
		.andExpect(handler().methodName("putEvent"));

		verify(eventService).save(arg.capture());
	}
	
	
	@Test
	public void putEventWithNoName() throws Exception {

		when(eventService.findOne(1)).thenReturn(event);
		mvc.perform(MockMvcRequestBuilders.post("/events/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "")
				.param("date", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
				.param("venue", venue.getName())
				.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("events/updateEvent")).andExpect(model().hasNoErrors())
		.andExpect(handler().methodName("putEvent"));

		verify(eventService, never()).save(event);
	}

	/*
	 * A suggestion for testing an invalid parameter
	@Test
	public void putEventWithOldDate() throws Exception {

		when(eventService.findOne(1)).thenReturn(event);
		mvc.perform(MockMvcRequestBuilders.post("/events/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Event")
				.param("date", "2019-07-09")
				.param("venue", venue.getName())
				.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("events/updateEvent")).andExpect(model().hasNoErrors())
		.andExpect(handler().methodName("putEvent"));

		verify(eventService, never()).save(event);
	}
	*/
	
	@Test
	@WithMockUser(username = "Mustafa", password = "Mustafa", roles= {"USER"})
	public void putEventUnauthorisedUser() throws Exception {
		when(eventService.findOne(1)).thenReturn(event);
		
		mvc.perform(MockMvcRequestBuilders.post("/events/update/{id}", 1)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Event New")
				.param("date", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
				.param("venue", venue.getName())
				.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isForbidden());
	
		verify(eventService, never()).save(event);
	}
	
	@Test
	@WithMockUser(username = "Mustafa", password = "Mustafa", roles= {"ADMINISTRATOR"})
	public void putVenueNoCsrf() throws Exception {
		when(eventService.findOne(1)).thenReturn(event);
		
		mvc.perform(MockMvcRequestBuilders.post("/events/update/{id}", 1)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Event New")
				.param("date", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
				.param("venue", venue.getName())
				.accept(MediaType.TEXT_HTML))
		.andExpect(status().isForbidden());

		
		verify(eventService, never()).save(event);
	}

	
	@Test
	public void postTweet() throws Exception {
		
		Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueService.save(v);
		
		
		Event e = new Event();
		e.setId(1);
		e.setName("Event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		e.setVenue(v);
		long id = e.getId();
		
		int randomNum = 1 + (int)(Math.random() * 100);
		
		String tweet = Integer.toString(randomNum);
		
		when(eventService.findOne(id)).thenReturn(e);

		mvc.perform(MockMvcRequestBuilders.post("/events/tweet/"+id).with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("tweet", tweet)
				.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/events/{id}")).andExpect(model().hasNoErrors())
		.andExpect(handler().methodName("updateStatusOnTwitter")).andExpect(flash().attributeExists("tweetString"));

		verify(eventService).createTweet(tweet);
	}
	
	@Test
	public void postEmptyTweet() throws Exception {
		
		Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueService.save(v);
		
		
		Event e = new Event();
		e.setId(1);
		e.setName("Event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		e.setVenue(v);
		long id = e.getId();
		
		String tweet = null;
		
		when(eventService.findOne(id)).thenReturn(e);

		mvc.perform(MockMvcRequestBuilders.post("/events/tweet/"+id).with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("tweet", tweet)
				.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/events/{id}")).andExpect(model().hasNoErrors())
		.andExpect(handler().methodName("updateStatusOnTwitter"));

		verify(eventService).createTweet(tweet);
	}
	
}
