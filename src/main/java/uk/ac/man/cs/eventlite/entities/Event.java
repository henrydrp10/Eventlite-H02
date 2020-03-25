package uk.ac.man.cs.eventlite.entities;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "events")
public class Event {

	@Id
	@GeneratedValue
	private long id;

	@NotNull(message = "Date required")
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Future(message = "Date must be in the future")
	private LocalDate date;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime time;

	@NotEmpty(message = "Name required")
	@Size(max = 256, message = "The event name must have 256 characters or less")
	private String name;
	
	@Size(max = 500, message = "The event description must have 500 characters or less")
	private String description;
	
	@Size(max = 500, message = "The event summary must have 500 characters or less")
	private String summary;
	
	@ManyToOne
	private Venue venue;

	public Event() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getTime() {
		return time;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setTime(LocalTime time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
	}
	
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public String getEvent() {
		return String.format(this.name);
	}

	@Override
	public String toString() {
		return String.format("Event [id = %d, venue = %s, time = %s, ]", this.id, this.venue.getName(), this.date.toString());
	}
}
