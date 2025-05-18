package GUI8;

public interface Booking {
	void bookTicket(int amount, Event event);

	void cancelTicket(int amount, Event event, boolean isVip);
}
