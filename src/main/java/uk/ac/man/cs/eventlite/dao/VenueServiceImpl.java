package uk.ac.man.cs.eventlite.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;
@Service
public class VenueServiceImpl implements VenueService {
	
	String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoiZXZlbnRsaXRlaDAyIiwiYSI6ImNrOG44NjNrNTBrZGMzbW9jbGRqc3kxbXQifQ.H2MJkZCOBTT-X9_noMmreA";
	
	@Autowired
	private VenueRepository venueRepository;
	
	@Autowired
	private EventRepository eventRepository;
	
	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAll();
	}
	
	@Override
	public void deleteById(long id) {
		venueRepository.deleteById(id);
	}
	
	
	@Override
	public Venue save(Venue v) {
		return venueRepository.save(v);
	}
	
	@Override
	public Venue findOne(long id) {		
		return venueRepository.findById(id).orElse(null);
	}

	public Iterable<Venue> findAllByName (String search) {
		String regex = "\\b" + search.toUpperCase() + "\\b";
		return venueRepository.findAllByName(regex);
	}
	
	public Venue updateLatLonIn(Venue venue) {
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken(MAPBOX_ACCESS_TOKEN)
				.query(venue.getRoadName() + " " + venue.getPostCode())
				.build();
		

        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
	       @Override
	       public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

		List<CarmenFeature> results = response.body().features();

		if (results.size() > 0) {

		  // Log the first results Point.
		  Point firstResultPoint = results.get(0).center();
		  venue.setLatitude(firstResultPoint.latitude());
		  venue.setLongitude(firstResultPoint.longitude());

		} else {

		  // No result for your request were found.
			System.out.println(":(( No  response :((");
			venue.setLatitude(1000);
			venue.setLongitude(1000);

		}
	}
	       
	  
	    
	@Override
	public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
		throwable.printStackTrace();
	}


});
        try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return venue;
	}
	
	@Autowired
	private EventService eventService;
	
	@Override
	public List<Event> getThreeUpcomingEventsForVenue(Long venueId) {

		Iterable<Event> futureEvents = eventService.findFuture();
		
		List<Event> returnList = new ArrayList<Event>();
		int i = 0;
		for( Event event : futureEvents )
		{
			if(event.getVenue().getId() == venueId && i<3)
			{
				i++;
				returnList.add(event);
				
			}
		}
		
		return returnList;
	}
	
	  @Override
	   	public List<Event> getEventsForVenue(Long venueId){
	   		Iterable<Event> events = eventRepository.findAll();
	   		
	   		List<Event> eventsAtThisVenue = new ArrayList<Event>();
	   		
	   		for (Event event : events)
	   		{   
	   			Venue venueAtThisEvent = event.getVenue();
	   			if (venueAtThisEvent.getId() == venueId)
	   				eventsAtThisVenue.add(event);
	   		}
	   		
	   		return eventsAtThisVenue;
	   	} 
		
}