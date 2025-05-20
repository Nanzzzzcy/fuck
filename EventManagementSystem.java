package eventManageSystem;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class EventManagementSystem extends JFrame {
	private CardLayout cardLayout;
	private JPanel mainPanel;
	private User currentUser;

	public EventManagementSystem() {
		setTitle("Event Management System");
		setSize(1000, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		setLayout(new BorderLayout());

		// Create side navigation panel
		JPanel navPanel = createNavPanel();

		// Create main panel
		cardLayout = new CardLayout();
		mainPanel = new JPanel(cardLayout);

		// Initialize different pages (don't create cancel panel yet)
		mainPanel.add(createBrowsePanel(), "Browse");
		mainPanel.add(createManagePanel(), "Manage");
		mainPanel.add(createBookingPanel(), "Book");

		// Layout settings
		add(navPanel, BorderLayout.WEST);
		add(mainPanel, BorderLayout.CENTER);
	}

	//Creates the left-side navigation panel with action buttons.
	private JPanel createNavPanel() {
		JPanel navPanel = new JPanel(new GridLayout(8, 1, 5, 5));
		navPanel.setBackground(new Color(50, 50, 50));

		JButton btnBrowseEvents = new JButton("Browse Events");
		JButton btnManageEvents = new JButton("Manage Events");
		JButton btnBookTicket = new JButton("Book Ticket");
		JButton btnCancelBooking = new JButton("Cancel Booking");

		// Always rebuild the Browse panel for real-time update
		btnBrowseEvents.addActionListener(e -> {
			mainPanel.remove(0); // Remove current Browse panel
			mainPanel.add(createBrowsePanel(), "Browse", 0); // Add new Browse panel at index 0
			cardLayout.show(mainPanel, "Browse");
		});

		btnManageEvents.addActionListener(e -> {
			if (currentUser instanceof Organizer) {
				cardLayout.show(mainPanel, "Manage");
			} else {
				JOptionPane.showMessageDialog(this, "You do not have permission to manage events.", "Permission Denied",
						JOptionPane.ERROR_MESSAGE);
			}
		});
		btnBookTicket.addActionListener(e -> cardLayout.show(mainPanel, "Book"));

		// Updated Cancel Booking button action listener
		btnCancelBooking.addActionListener(e -> {
			// Create cancel panel only when needed
			if (mainPanel.getComponentCount() < 4) { // If cancel panel hasn't been created yet
				mainPanel.add(createCancelPanel(), "Cancel");
			}
			refreshUI("Cancel");
			cardLayout.show(mainPanel, "Cancel");
		});

		navPanel.add(btnBrowseEvents);
		navPanel.add(btnManageEvents);
		navPanel.add(btnBookTicket);
		navPanel.add(btnCancelBooking);

		return navPanel;
	}

	//Creates the Browse Events panel. Always loads the latest event info from the file.
	private JPanel createBrowsePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Browse Events", JLabel.CENTER), BorderLayout.NORTH);
		panel.add(loadAndDisplayEvents(false, false, false), BorderLayout.CENTER);
		return panel;
	}

	//Creates the Book Tickets panel.
	private JPanel createBookingPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Book Tickets", JLabel.CENTER), BorderLayout.NORTH);
		panel.add(loadAndDisplayEvents(true, false, false), BorderLayout.CENTER);
		return panel;
	}

	//Creates the Cancel Bookings panel, showing the user's bookings.
	private JPanel createCancelPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Cancel Bookings", JLabel.CENTER), BorderLayout.NORTH);

		JPanel bookingListPanel = new JPanel();
		bookingListPanel.setLayout(new BoxLayout(bookingListPanel, BoxLayout.Y_AXIS));

		List<String> bookings = loadBookingInfoFromFile();
		for (String booking : bookings) {
			JPanel bookingPanel = new JPanel(new BorderLayout());
			bookingPanel.setBorder(BorderFactory.createTitledBorder("Booking"));

			JTextArea bookingInfo = new JTextArea(booking);
			bookingInfo.setEditable(false);
			bookingPanel.add(new JScrollPane(bookingInfo), BorderLayout.CENTER);

			JButton btnCancel = new JButton("Cancel Booking");
			btnCancel.addActionListener(e -> handleCancelBooking(booking));
			bookingPanel.add(btnCancel, BorderLayout.SOUTH);

			bookingListPanel.add(bookingPanel);
		}

		panel.add(new JScrollPane(bookingListPanel), BorderLayout.CENTER);
		return panel;
	}

	//Creates the Manage Events panel for organizers.
	private JPanel createManagePanel() {
		JPanel panel = new JPanel(new BorderLayout());

		JLabel label = new JLabel("Manage Events", JLabel.CENTER);
		label.setFont(new Font("Arial", Font.PLAIN, 24));
		panel.add(label, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
		JButton btnAdd = new JButton("Add Event");
		JButton btnUpdate = new JButton("Update Event");
		JButton btnDelete = new JButton("Delete Event");

		btnAdd.addActionListener(e -> handleAddEvent());
		btnUpdate.addActionListener(e -> handleUpdateEvent());
		btnDelete.addActionListener(e -> handleDeleteEvent());

		buttonPanel.add(btnAdd);
		buttonPanel.add(btnUpdate);
		buttonPanel.add(btnDelete);

		panel.add(buttonPanel, BorderLayout.CENTER);
		return panel;
	}

	// Handles the Add Event dialog and logic for organizers.
	private void handleAddEvent() {
		EventDialog dialog = new EventDialog();
		try {
			Event newEvent = dialog.showDialog();
			if (newEvent != null) {
				Organizer organizer = (Organizer) currentUser;
				organizer.addEvent(newEvent);

				JOptionPane.showMessageDialog(this, "Event added successfully.", "Success",
						JOptionPane.INFORMATION_MESSAGE);
				refreshUI("Manage");
			}
		} catch (InvalidEventInputException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "输入错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	//Handles updating an existing event. Fields name, artist, and date are NOT editable to ensure event identity.
	private void handleUpdateEvent() {
		// Let user select which event to update
		List<Event> events = loadEventsFromFile(Organizer.DEFAULT_FILE_PATH);
		String[] eventNames = events.stream().map(Event::getEventName).toArray(String[]::new);

		String selectedEvent = (String) JOptionPane.showInputDialog(this, "Select event to update:", "Update Event",
				JOptionPane.QUESTION_MESSAGE, null, eventNames, eventNames.length > 0 ? eventNames[0] : null);

		if (selectedEvent == null)
			return;

		// Find the selected event
		Event oldEvent = events.stream().filter(e -> e.getEventName().equals(selectedEvent)).findFirst().orElse(null);

		if (oldEvent == null) {
			JOptionPane.showMessageDialog(this, "Event not found.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Create edit panel with all editable fields.
		// Name, Artist, Date are NOT editable for event identity consistency.
		JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
		JTextField txtName = new JTextField(oldEvent.getEventName());
		txtName.setEditable(false);
		JTextField txtArtist = new JTextField(oldEvent.getArtist());
		txtArtist.setEditable(false);
		JTextField txtDate = new JTextField(oldEvent.getEventDate());
		txtDate.setEditable(false);
		JTextField txtLocation = new JTextField(oldEvent.getEventLocation());
		JTextField txtRegularTickets = new JTextField(String.valueOf(oldEvent.getEventAmount()));
		JTextField txtVipTickets = new JTextField(String.valueOf(oldEvent.getEventVip()));

		panel.add(new JLabel("Event Name:"));
		panel.add(txtName);
		panel.add(new JLabel("Artist:"));
		panel.add(txtArtist);
		panel.add(new JLabel("Date:"));
		panel.add(txtDate);
		panel.add(new JLabel("Location:"));
		panel.add(txtLocation);
		panel.add(new JLabel("Regular Tickets:"));
		panel.add(txtRegularTickets);
		panel.add(new JLabel("VIP Tickets:"));
		panel.add(txtVipTickets);

		int result = JOptionPane.showConfirmDialog(this, panel, "Update Event", JOptionPane.OK_CANCEL_OPTION);

		if (result == JOptionPane.OK_OPTION) {
			try {
				String name = txtName.getText();
				String artist = txtArtist.getText();
				String date = txtDate.getText();
				String location = txtLocation.getText();
				int regTickets = Integer.parseInt(txtRegularTickets.getText());
				int vipTickets = Integer.parseInt(txtVipTickets.getText());
				if (name.isEmpty() || artist.isEmpty() || date.isEmpty() || location.isEmpty()) {
					throw new InvalidEventInputException("All fields must be filled in.");
				}
				if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
					throw new InvalidEventInputException("The date format must be yyyy-MM-dd");
				}
				if (regTickets < 0 || vipTickets < 0) {
					throw new InvalidEventInputException("The number of votes must be a non-negative integer");
				}
				Event updatedEvent = new Event(name, artist, date, regTickets, vipTickets, location);

				Organizer organizer = (Organizer) currentUser;
				organizer.deleteEvent(oldEvent.getEventName(), oldEvent.getEventDate(), oldEvent.getEventLocation());
				organizer.addEvent(updatedEvent);

				JOptionPane.showMessageDialog(this, "Event updated successfully.", "Success",
						JOptionPane.INFORMATION_MESSAGE);
				refreshUI("Manage");
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "The number of votes must be an integer.", "input error", JOptionPane.ERROR_MESSAGE);
			} catch (InvalidEventInputException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "input error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Error updating event: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	//Handles deleting an event. Will also remove all related bookings automatically.
	private void handleDeleteEvent() {
		JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
		JTextField txtName = new JTextField();
		JTextField txtDate = new JTextField();
		JTextField txtLocation = new JTextField();

		panel.add(new JLabel("Event Name:"));
		panel.add(txtName);
		panel.add(new JLabel("Event Date:"));
		panel.add(txtDate);
		panel.add(new JLabel("Event Location:"));
		panel.add(txtLocation);

		int result = JOptionPane.showConfirmDialog(this, panel, "Delete Event", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			try {
				Organizer organizer = (Organizer) currentUser;
				organizer.deleteEvent(txtName.getText(), txtDate.getText(), txtLocation.getText());

				// After event deletion, also remove all related bookings
				Organizer.deleteRelatedBookings(txtName.getText(), txtDate.getText(), txtLocation.getText());

				JOptionPane.showMessageDialog(this, "Event deleted successfully.", "Success",
						JOptionPane.INFORMATION_MESSAGE);
				refreshUI("Manage");
				refreshUI("Browse"); // Also refresh the browse panel after deletion
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Error deleting event: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	//Handles the cancellation of a specific booking. After cancel, updates event file and UI.
	private void handleCancelBooking(String bookingInfo) {
		// Parse booking info to get event details
		String[] lines = bookingInfo.split("\n");
		String consumerLine = lines[0];
		String eventLine = lines[1];
		String amountLine = lines[2];
		String vipLine = lines[3];

		String[] eventParts = eventLine.substring("Event Info: ".length()).split(", ");
		String eventName = eventParts[0];
		String eventDate = eventParts[1];
		String eventLocation = eventParts[2];

		int amount = Integer.parseInt(amountLine.substring("Ticket Amount: ".length()));
		boolean isVip = vipLine.endsWith("Yes");

		// Find the event
		List<Event> events = loadEventsFromFile(Organizer.DEFAULT_FILE_PATH);
		Event targetEvent = events.stream().filter(e -> e.getEventName().equals(eventName)
				&& e.getEventDate().equals(eventDate) && e.getEventLocation().equals(eventLocation)).findFirst()
				.orElse(null);

		if (targetEvent == null) {
			JOptionPane.showMessageDialog(this, "Event not found", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			Consumer consumer = (Consumer) currentUser;
			consumer.cancelTicket(amount, targetEvent, isVip);

			updateEventFile(targetEvent);

			refreshUI("Cancel");
			refreshUI("Book"); // <-- 新增
			JOptionPane.showMessageDialog(this, "Booking cancelled successfully", "Success",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error cancelling booking: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Refreshes the UI for a given panel name. "Browse" will always reload the
	 * browse event panel from file. "Cancel" will reload the cancel booking panel.
	 * "Manage" will reload the browse panel for organizer.
	 */
	private void refreshUI(String showPanel) {
		if ("Cancel".equals(showPanel)) {
			if (mainPanel.getComponentCount() > 3)
				mainPanel.remove(3); // Remove existing cancel panel if it exists
			mainPanel.add(createCancelPanel(), "Cancel");
		} else if ("Manage".equals(showPanel)) {
			mainPanel.remove(0); // Remove browse panel
			mainPanel.add(createBrowsePanel(), "Browse", 0); // Add new browse panel at index 0
		} else if ("Browse".equals(showPanel)) {
			mainPanel.remove(0);
			mainPanel.add(createBrowsePanel(), "Browse", 0);
		} else if ("Book".equals(showPanel)) {
			mainPanel.remove(2);
			mainPanel.add(createBookingPanel(), "Book", 2);
		}
		if (showPanel != null) {
			cardLayout.show(mainPanel, showPanel);
		}
	}

	// Loads and displays events from the event file.
	private JPanel loadAndDisplayEvents(boolean bookingEnabled, boolean cancelEnabled, boolean manageEnabled) {
		JPanel eventListPanel = new JPanel(new GridLayout(0, 3, 10, 10));
		eventListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Always reload event list from file for latest data
		List<Event> events = loadEventsFromFile(Organizer.DEFAULT_FILE_PATH);
		for (Event event : events) {
			JPanel eventPanel = new JPanel(new BorderLayout());
			eventPanel.setBorder(BorderFactory.createTitledBorder("Event: " + event.getEventName()));

			JTextArea eventInfo = new JTextArea();
			eventInfo.setEditable(false);
			eventInfo.setFont(new Font("Monospaced", Font.PLAIN, 14));
			eventInfo.setText(getEventInfoText(event));
			eventPanel.add(new JScrollPane(eventInfo), BorderLayout.CENTER);

			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			if (bookingEnabled) {
				JButton btnRegular = new JButton("Regular Book");
				JButton btnVIP = new JButton("VIP Book");

				btnRegular.addActionListener(e -> handleTicketBooking(event, false, eventInfo));
				btnVIP.addActionListener(e -> handleTicketBooking(event, true, eventInfo));

				buttonPanel.add(btnRegular);
				buttonPanel.add(btnVIP);
			}

			if (cancelEnabled) {
				JButton btnCancel = new JButton("Cancel Booking");
				btnCancel.addActionListener(e -> handleTicketCancellation(event, eventListPanel, eventPanel));
				buttonPanel.add(btnCancel);
			}

			eventPanel.add(buttonPanel, BorderLayout.SOUTH);
			eventListPanel.add(eventPanel);
		}
		return eventListPanel;
	}

	//Handles ticket booking for regular or VIP tickets. After booking, updates event info and refreshes Browse panel.
	private void handleTicketBooking(Event event, boolean isVip, JTextArea eventInfo) {
		if (!(currentUser instanceof Consumer)) {
			JOptionPane.showMessageDialog(this, "Current user does not have booking permissions.", "Permission Denied",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String input = JOptionPane.showInputDialog(this, "Enter the number of tickets:");
		try {
			int amount = Integer.parseInt(input);
			Consumer consumer = (Consumer) currentUser;

			if (isVip) {
				if (consumer instanceof Vip) {
					((Vip) consumer).vipBooking(amount, event);
					JOptionPane.showMessageDialog(this, "VIP Tickets booked successfully.", "Success",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(this, "Only VIP users can book VIP tickets.", "Permission Denied",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				consumer.bookTicket(amount, event);
				JOptionPane.showMessageDialog(this, "Regular Tickets booked successfully.", "Success",
						JOptionPane.INFORMATION_MESSAGE);
			}

			// Update eventInfo.txt file after booking
			updateEventFile(event);

			// Refresh event info text area
			eventInfo.setText(getEventInfoText(event));

			// Refresh browse events panel after booking
			refreshUI("Browse");
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Invalid number entered.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (InvalidTicketAmountException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "input error", JOptionPane.ERROR_MESSAGE);
		} catch (UnauthorizedAccessException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Permission Denied", JOptionPane.ERROR_MESSAGE);
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	//Handles ticket cancellation from the event list panel (not booking list).
	private void handleTicketCancellation(Event event, JPanel eventListPanel, JPanel eventPanel) {
		if (!(currentUser instanceof Consumer)) {
			JOptionPane.showMessageDialog(this, "Current user does not have cancellation permissions.",
					"Permission Denied", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String input = JOptionPane.showInputDialog(this, "Enter the number of tickets to cancel:");
		if (input == null)
			return; // User clicked cancel

		try {
			int amount = Integer.parseInt(input);
			Consumer consumer = (Consumer) currentUser;

			// Choose ticket type
			String[] options = { "tickets", "VIP tickets" };
			int choice = JOptionPane.showOptionDialog(this, "Please select the type of ticket to be cancelled: ", "Cancel ticket type", JOptionPane.DEFAULT_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (choice == JOptionPane.CLOSED_OPTION)
				return;
			boolean isVip = (choice == 1);

			// Save original ticket numbers for restoring if cancel fails
			int originalAmount = event.getEventAmount();
			int originalVip = event.getEventVip();

			try {
				consumer.cancelTicket(amount, event, isVip);

				updateEventFile(event);

				refreshUI("Cancel");
				refreshUI("Book"); // <-- news

				JOptionPane.showMessageDialog(this, "Successfully canceled " + amount + " tickets.", "Success",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (IllegalArgumentException e) {
				// Restore original ticket numbers if error
				event.setEventAmount(originalAmount);
				event.setEventVip(originalVip);
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Updates the event information file with new ticket numbers for the given
	 * event. Keeps all other events unchanged.
	 */
	private void updateEventFile(Event updatedEvent) {
		List<Event> events = loadEventsFromFile(Organizer.DEFAULT_FILE_PATH);
		boolean found = false;

		for (Event event : events) {
			if (event.getEventName().equals(updatedEvent.getEventName())
					&& event.getEventDate().equals(updatedEvent.getEventDate())
					&& event.getEventLocation().equals(updatedEvent.getEventLocation())) {

				// Update ticket numbers
				event.setEventAmount(updatedEvent.getEventAmount());
				event.setEventVip(updatedEvent.getEventVip());
				found = true;
				break;
			}
		}

		if (!found) {
			throw new IllegalArgumentException("Event not found in records");
		}

		// Rewrite event info file with updated tickets
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(Organizer.DEFAULT_FILE_PATH))) {
			for (Event event : events) {
				writer.write("Event Info: " + event.getEventName() + ", " + event.getEventDate() + ", "
						+ event.getEventLocation());
				writer.newLine();
				writer.write("VIP Ticket: " + event.getEventVip());
				writer.newLine();
				writer.write("Regular Ticket: " + event.getEventAmount());
				writer.newLine();
				writer.write("Artist: " + event.getArtist());
				writer.newLine();
				writer.newLine();
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Error updating event file: " + e.getMessage());
		}
	}

	// Returns human-readable event info.
	private String getEventInfoText(Event event) {
		return "Event Name: " + event.getEventName() + "\n" + "Artist: " + event.getArtist() + "\n" + "Date: "
				+ event.getEventDate() + "\n" + "Location: " + event.getEventLocation() + "\n" + "Regular Tickets: "
				+ event.getEventAmount() + "\n" + "VIP Tickets: " + event.getEventVip();
	}

	//Loads all events from the event info file.
	private List<Event> loadEventsFromFile(String filePath) {
		List<Event> events = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("Event Info:")) {
					String[] infoParts = line.substring(12).split(", ");
					String eventName = infoParts[0];
					String eventDate = infoParts[1];
					String eventLocation = infoParts[2];

					int vipTickets = Integer.parseInt(reader.readLine().split(": ")[1]);
					int regularTickets = Integer.parseInt(reader.readLine().split(": ")[1]);
					String artist = reader.readLine().split(": ")[1];

					events.add(new Event(eventName, artist, eventDate, regularTickets, vipTickets, eventLocation));
					reader.readLine();
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error loading events: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return events;
	}

	//Loads the current user's bookings from the booking info file.
	private List<String> loadBookingInfoFromFile() {
		List<String> bookingInfo = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(Consumer.DEFAULT_FILE_PATH))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("Consumer Info:") && line.contains(currentUser.getID())) {
					// Add the entire booking block (4 lines)
					StringBuilder bookingBlock = new StringBuilder();
					bookingBlock.append(line).append("\n");
					for (int i = 0; i < 3; i++) {
						line = reader.readLine();
						bookingBlock.append(line).append("\n");
					}
					bookingInfo.add(bookingBlock.toString());
					reader.readLine(); // skip empty line
				}
			}
		} catch (IOException e) {
			System.err.println("Error reading booking info: " + e.getMessage());
		}
		return bookingInfo;
	}

	//Sets the current logged-in user.
	public void setCurrentUser(User user) {
		this.currentUser = user;
	}

	//Main entry point for launching the GUI.
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			EventManagementSystem frame = new EventManagementSystem();
			frame.setVisible(true);
		});
	}
}