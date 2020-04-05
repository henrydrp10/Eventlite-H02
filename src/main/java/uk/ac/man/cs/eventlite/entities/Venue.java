package uk.ac.man.cs.eventlite.entities;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Entity
@Table(name = "venues")
public class Venue {

	@Id
	@GeneratedValue
	private long id;

	@NotEmpty(message = "Name required")
	@Size(max = 256, message = "The venue name must have 256 characters or less")
	private String name;
	
	@NotEmpty(message = "Road name required")
	@Size(max = 300, message = "The road name must have 300 characters or less")
	private String roadName;
	
	@NotEmpty(message = "Postcode required")
	@Size(max = 256, message = "The postcode must have 256 characters or less")
	private String postCode;

	@Min(value = 0, message = "The venue capacity must be a positive integer")
	private int capacity;

	private double latitude;
	private double longitude;
	
	public Venue() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getRoadName() {
		return roadName;
	}

	public void setRoadName(String roadName) {
		this.roadName = roadName;
	}
	
	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	public double getLatitude()
	{
		return latitude;
	}
	
	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}
	
	public double getLongitude()
	{
		return longitude;
	}
	
	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}
}
