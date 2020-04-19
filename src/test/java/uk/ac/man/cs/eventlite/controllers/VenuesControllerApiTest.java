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
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerApiTest {
	
	private final static String BAD_ROLE = "USER";

	private MockMvc mvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@Mock
	private VenueService venueService;
	
	@Mock
	private Venue venue;

	@InjectMocks
	private VenuesControllerApi venuesController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(venuesController).apply(springSecurity(springSecurityFilterChain))
				.setMessageConverters(getMessageConverters()).build();
	}
	
	@Test
	public void showVenueTest() throws Exception {
		
		Venue v = new Venue();
		v.setName("Venue");
		v.setCapacity(1000);
		venueService.save(v);
		
		when(venueService.findOne(v.getId())).thenReturn(v);
        String uri = "/api/venues/" + v.getId();
        System.out.println(uri);
		mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("showVenue")).andExpect(jsonPath("$.length()", equalTo(7)))
				.andExpect(jsonPath("$._links.self.href", endsWith(uri)))
				.andExpect(jsonPath("$._links.venue.href", endsWith(uri)));
				
		verify(venueService).findOne(v.getId());
	}
	
	@Test
	public void getNewVenue() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/api/venues/new").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isNotAcceptable()).andExpect(handler().methodName("newVenue"));
	}

	@Test
	public void postVenueNoAuth() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/api/venues").contentType(MediaType.APPLICATION_JSON)
				.content("{ \"name\": \"Test Venue New\" }").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnauthorized());

		verify(venueService, never()).save(venue);
	}

	@Test
	public void postVenueBadAuth() throws Exception {
		mvc.perform(
				MockMvcRequestBuilders.post("/api/venues").with(anonymous())
				.contentType(MediaType.APPLICATION_JSON).content("{ \"name\": \"Test Venue New\" }")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

		verify(venueService, never()).save(venue);
	}

	@Test
	public void postVenueBadRole() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/api/venues").with(user("Rob").roles(BAD_ROLE))
				.contentType(MediaType.APPLICATION_JSON).content("{ \"name\": \"Test Venue New\" }")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

		verify(venueService, never()).save(venue);
	}

	@Test
	public void postLongVenue() throws Exception {
		mvc.perform(
				MockMvcRequestBuilders.post("/api/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"template\": \"abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyzabcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz abcdefghij s klmnopqrst uvwxyz\" }").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity()).andExpect(content().string(""))
		.andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}

	@Test
	public void postEmptyVenue() throws Exception {
		mvc.perform(
				MockMvcRequestBuilders.post("/api/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"template\": \"\" }").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity()).andExpect(content().string(""))
		.andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
}