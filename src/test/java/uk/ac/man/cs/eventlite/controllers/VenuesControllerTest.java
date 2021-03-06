package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
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

import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import uk.ac.man.cs.eventlite.config.Security;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerTest {
	
	private final static String BAD_ROLE = "USER";

	private MockMvc mvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@Mock
	private Venue venue;
	
	@Mock
	private Iterable<Venue> venues;

	@Mock
	private VenueService venueService;
	
	@Mock
	private EventService eventService;

	@InjectMocks
	private VenuesController venuesController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(venuesController).apply(springSecurity(springSecurityFilterChain))
				.build();
	}
	
	@Test
	public void getIndexWhenNoVenues() throws Exception {
		
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
	}

	@Test
	public void getIndexWithVenues() throws Exception {
		
		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
	}

	public void getNewVenueNoAuth() throws Exception {		
		mvc.perform(MockMvcRequestBuilders.post("/venues")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "abcdefghij").accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isFound()).andExpect(header().string("Location", endsWith("/sign-in")));
	}

	@Test
	public void getNewVenue() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/venues/new").with(user("Rob").roles(Security.ADMIN_ROLE))
				.accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk()).andExpect(view().name("venues/new"))
		.andExpect(handler().methodName("newVenue"));
	}
	
	public void getUpdateVenueViewNoAuth() throws Exception {
		Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueService.save(v);
		long id = v.getId();
		when(venueService.findOne(id)).thenReturn(v);

		mvc.perform(MockMvcRequestBuilders.post("/venues")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "abcdefghij").accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isFound()).andExpect(header().string("Location", endsWith("/sign-in")));
	}

	@Test
	public void getUpdateVenueView() throws Exception {
		Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueService.save(v);
		long id = v.getId();
		when(venueService.findOne(id)).thenReturn(v);
		mvc.perform(MockMvcRequestBuilders.get("/venues/updateVenue/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
				.accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk()).andExpect(view().name("venues/updateVenue"))
		.andExpect(handler().methodName("getVenueToUpdate"));
	}

	@Test
	public void postVenueNoAuth() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/venues").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Venue New").accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
		.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(venueService, never()).save(venue);
	}

	@Test
	public void postVenueBadRole() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/venues").with(user("Rob").roles(BAD_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "Test Venue 1")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isForbidden());

		verify(venueService, never()).save(venue);
	}

	@Test
	public void postVenueNoCsrf() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "Test Venue 1")
				.accept(MediaType.TEXT_HTML)).andExpect(status().isForbidden());

		verify(venueService, never()).save(venue);
	}

	
	@Test
	public void postVenue() throws Exception {
		ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);

		mvc.perform(MockMvcRequestBuilders.post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Venue New")
				.param("roadName", "Oxford Rd")
				.param("postCode", "M13 9GP")
				.param("capacity", "5000")
				.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/venues")).andExpect(model().hasNoErrors())
		.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeExists("ok_message"));

		verify(venueService).save(arg.capture());
		verify(venueService).updateLatLonIn(arg.capture());
	}
	

	@Test
	public void postLongVenue() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyzabcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz").accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isOk()).andExpect(view().name("venues/new"))
		.andExpect(model().attributeHasFieldErrors("venue", "name"))
		.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
	}

	@Test
	public void postEmptyVenue() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "").accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
		.andExpect(view().name("venues/new"))
		.andExpect(model().attributeHasFieldErrors("venue", "name"))
		.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void putVenue() throws Exception {
		ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);
		
		Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueService.save(v);
		long id = v.getId();
		when(venueService.findOne(id)).thenReturn(v);

		mvc.perform(MockMvcRequestBuilders.post("/venues/update/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Venue New")
				.param("roadName", "Oxford Rd")
				.param("postCode", "M13 9GP")
				.param("capacity", "5000")
				.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/venues")).andExpect(model().hasNoErrors())
		.andExpect(handler().methodName("putVenue"));

		verify(venueService, atLeast(1)).save(arg.capture());
		verify(venueService).updateLatLonIn(arg.capture());
	}
	
	
	@Test
	public void putVenueWithNoName() throws Exception {
		Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueService.save(v);
		long id = v.getId();
		when(venueService.findOne(id)).thenReturn(v);
		mvc.perform(MockMvcRequestBuilders.post("/venues/update/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "")
				.param("roadName", "Oxford Rd")
				.param("postCode", "M13 9GP")
				.param("capacity", "5000")
				.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("venues/updateVenue")).andExpect(model().hasErrors())
		.andExpect(handler().methodName("putVenue"));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void putVenueWithNoRoadName() throws Exception {
		Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueService.save(v);
		long id = v.getId();
		when(venueService.findOne(id)).thenReturn(v);
		mvc.perform(MockMvcRequestBuilders.post("/venues/update/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test")
				.param("roadName", "")
				.param("postCode", "M13 9GP")
				.param("capacity", "5000")
				.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("venues/updateVenue")).andExpect(model().hasErrors())
		.andExpect(handler().methodName("putVenue"));

		verify(venueService, never()).save(venue);
	}
	

	@Test
	@WithMockUser(username = "Mustafa", password = "Mustafa", roles= {"USER"})
	public void putVenueUnauthorisedUser() throws Exception {
		when(venueService.findOne(1)).thenReturn(venue);
		
		mvc.perform(MockMvcRequestBuilders.post("/venues/update/{id}", 1)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Venue New")
				.param("roadName", "Oxford Rd")
				.param("postCode", "M13 9GP")
				.param("capacity", "5000")
				.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isForbidden());
	
		verify(venueService, never()).save(venue);
	}
	
	@Test
	@WithMockUser(username = "Mustafa", password = "Mustafa", roles= {"ADMINISTRATOR"})
	public void putVenueNoCsrf() throws Exception {
		when(venueService.findOne(1)).thenReturn(venue);
		
		mvc.perform(MockMvcRequestBuilders.post("/venues/update/{id}", 1)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Venue New")
				.param("roadName", "Oxford Rd")
				.param("postCode", "M13 9GP")
				.param("capacity", "5000")
				.accept(MediaType.TEXT_HTML))
		.andExpect(status().isForbidden());

		
		verify(venueService, never()).save(venue);
	}
	

	@Test
	public void putEmptyVenue() throws Exception {
		Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueService.save(v);
		long id = v.getId();
		when(venueService.findOne(id)).thenReturn(v);
		mvc.perform(MockMvcRequestBuilders.post("/venues/update/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "").accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
		.andExpect(view().name("venues/updateVenue"))
		.andExpect(model().attributeHasFieldErrors("venue", "name"))
		.andExpect(handler().methodName("putVenue"));

		verify(venueService, never()).save(venue);
	}
	
	
	@Test
	public void getVenue() throws Exception {
		
		Venue v = new Venue();
		v.setId(1000);
		v.setName("Test Venue 1");
		v.setCapacity(1000);
		v.setRoadName("King St");
		v.setPostCode("M2 1NL");
		v.setLatitude(53.481380);
		v.setLongitude(-2.246870);
		venueService.save(v);

		long id = 1000;
		
		when(venueService.findOne(id)).thenReturn(v);
		when(eventService.findFuture()).thenReturn(new ArrayList<Event>());
	

		mvc.perform(MockMvcRequestBuilders.get("/venues/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.accept(MediaType.TEXT_HTML)
		.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(handler().methodName("showVenueDetails"))
		.andExpect(view().name("venues/venue_details"));
		
		verify(venueService).findOne(id);
	}
	
	@Test
	public void getVenueWhenVenueDoesNotExist() throws Exception {
	
		mvc.perform(MockMvcRequestBuilders.get("/venues/1000").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.accept(MediaType.TEXT_HTML)
		.with(csrf()))
		.andExpect(status().isFound())
		.andExpect(handler().methodName("showVenueDetails"))
		.andExpect(view().name("redirect:/venues"));
		
		verify(venueService).findOne(1000);
	}

	@WithMockUser(username = "Mustafa", password = "Mustafa", roles= {"ADMINISTRATOR"})
	public void deleteVenueByName() throws Exception {
		when(venueService.findOne(1)).thenReturn(venue);
	
		mvc.perform(MockMvcRequestBuilders.delete("/venues/delete/1").accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isFound())
		.andExpect(view().name("redirect:/venues"))
		.andExpect(handler().methodName("deleteById"));
	
		verify(venueService).deleteById(1);
	}
 
	@Test
	@WithMockUser(username = "Mustafa", password = "Mustafa", roles= {"USER"})
	public void deleteVenueByNameUnauthorisedUser() throws Exception {
		when(venueService.findOne(1)).thenReturn(venue);
	
		mvc.perform(MockMvcRequestBuilders.delete("/venues/delete/1").accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isForbidden());
	
		verify(venueService, never()).deleteById(1);
	}
	
	@Test
	@WithMockUser(username = "Mustafa", password = "Mustafa", roles= {"ADMINISTRATOR"})
	public void deleteVenueByNameNoCsrf() throws Exception {
		when(venueService.findOne(1)).thenReturn(venue);
	
		mvc.perform(MockMvcRequestBuilders.delete("/venues/delete/1").accept(MediaType.TEXT_HTML))
		.andExpect(status().isForbidden());
	
		verify(venueService, never()).deleteById(1);
	}
	
	@Test
	public void searchVenueByName() throws Exception {
		when(venueService.findAllByName("Venue")).thenReturn(venues);
		mvc.perform(get("/venues/byName?search=Venue").accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk())
			.andExpect(model().attribute("venues", venues))
			.andExpect(view().name("venues/byName"))
			.andExpect(handler().methodName("getVenuesByName"));
		verify(venueService).findAllByName("Venue");
		verifyZeroInteractions(venues);	
	}
}