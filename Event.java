package GUI8;

public class Event {
	private String eventName;
	private String eventDate;
	private int eventAmount;
	private int eventVip;
	private String eventLocation;
	private String artist;

	// Constructor
	public Event(String eventName, String artist, String eventDate, int eventAmount, int eventVip,
			String eventLocation) {
		this.eventName = eventName;
		this.artist = artist;
		this.eventDate = eventDate;
		this.eventAmount = eventAmount;
		this.eventVip = eventVip;
		this.eventLocation = eventLocation;
	}

	// Getters and Setters
	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String newArtist) {
		this.artist = newArtist;
	}

	public String getEventDate() {
		return eventDate;
	}

	public void setEventDate(String eventDate) {
		this.eventDate = eventDate;
	}

	public int getEventAmount() {
		return eventAmount;
	}

	public void setEventAmount(int eventAmount) {
		this.eventAmount = eventAmount;
	}

	public int getEventVip() {
		return eventVip;
	}

	public void setEventVip(int eventVip) {
		this.eventVip = eventVip;
	}

	public String getEventLocation() {
		return eventLocation;
	}

	public void setEventLocation(String eventLocation) {
		this.eventLocation = eventLocation;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		return eventName.equals(other.eventName) && eventDate.equals(other.eventDate)
				&& eventLocation.equals(other.eventLocation);
	}
}
