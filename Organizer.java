package GUI8;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Organizer extends User {
	private String company;
	protected static final String DEFAULT_FILE_PATH = "eventInfo.txt";

	public Organizer(String name, String ID, String pwd, String company) {
		super(name, ID, pwd);
		this.company = company;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String newCompany) {
		this.company = newCompany;
	}

	public void addEvent(Event event) {
		// Check if the event already exists to prevent data conflict
		List<String> existingEvents = readAllLines();
		for (String line : existingEvents) {
			if (line.startsWith("Event Info:")) {
				String[] infoParts = line.substring(12).split(", ");
				if (infoParts.length == 3 && infoParts[0].equals(event.getEventName())
						&& infoParts[1].equals(event.getEventDate()) && infoParts[2].equals(event.getEventLocation())) {
					throw new DataConflictException("Event already exists: " + event.getEventName());
				}
			}
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(DEFAULT_FILE_PATH, true))) { // Open in append
																									// mode
			writer.write("Event Info: " + event.getEventName() + ", " + event.getEventDate() + ", "
					+ event.getEventLocation());
			writer.newLine();
			writer.write("VIP Ticket: " + event.getEventVip());
			writer.newLine();
			writer.write("Regular Ticket: " + event.getEventAmount());
			writer.newLine();
			writer.write("Artist: " + event.getArtist());
			writer.newLine();
			writer.newLine(); // Explicit empty line separator
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}
	}

	private void validateOrganizer() {
		if (!this.getClass().equals(Organizer.class)) {
			throw new UnauthorizedAccessException("Illegal access: Non-organizer account");
		}
	}

	public void updateEvent(Event updatedEvent) {
		List<String> lines = readAllLines();
		List<String> updatedLines = new ArrayList<>();
		boolean found = false;

		for (int i = 0; i < lines.size();) {
			if (i + 4 < lines.size() && lines.get(i).startsWith("Event Info:") && lines.get(i + 4).trim().isEmpty()) {
				String[] infoParts = lines.get(i).substring(12).split(", ");
				if (infoParts.length == 3 && infoParts[0].equals(updatedEvent.getEventName())
						&& infoParts[1].equals(updatedEvent.getEventDate())
						&& infoParts[2].equals(updatedEvent.getEventLocation())) {
					// Replace with new record
					updatedLines.add("Event Info: " + updatedEvent.getEventName() + ", " + updatedEvent.getEventDate()
							+ ", " + updatedEvent.getEventLocation());
					updatedLines.add("VIP Ticket: " + updatedEvent.getEventVip());
					updatedLines.add("Regular Ticket: " + updatedEvent.getEventAmount());
					updatedLines.add("Artist: " + updatedEvent.getArtist());
					updatedLines.add(""); // Keep the empty line separator
					i += 5; // Skip the old record
					found = true;
					continue;
				}
			}
			updatedLines.add(lines.get(i++));
		}

		if (!found) {
			throw new DataNotFoundException("Event not found for update.");
		}
		writeAllLines(updatedLines);
	}

	public void deleteEvent(String eventName, String eventDate, String eventLocation) {
		List<String> lines = readAllLines();
		List<String> updatedLines = new ArrayList<>();
		boolean deleted = false;

		for (int i = 0; i < lines.size();) {
			if (i + 4 < lines.size() && lines.get(i).startsWith("Event Info:") && lines.get(i + 4).trim().isEmpty()) {
				String[] infoParts = lines.get(i).substring(12).split(", ");
				if (infoParts.length == 3 && infoParts[0].equals(eventName) && infoParts[1].equals(eventDate)
						&& infoParts[2].equals(eventLocation)) {
					i += 5; // Skip the entire record block
					deleted = true;
					continue;
				}
			}
			updatedLines.add(lines.get(i++));
		}

		if (!deleted) {
			throw new DataNotFoundException("Event not found for deletion.");
		}
		writeAllLines(updatedLines);
		removeConsumerBookingsByEvent(eventName, "");
	}

	public void updateEventInfo(List<Event> events) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(DEFAULT_FILE_PATH))) {
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
				writer.newLine(); // 空行分隔符
			}
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}
	}

	// 新增静态方法
	public static void removeConsumerBookingsByEvent(String eventName, String artist) {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(Consumer.DEFAULT_FILE_PATH))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			System.err.println("Error reading booking info: " + e.getMessage());
			return;
		}
		List<String> filteredLines = new ArrayList<>();
		for (int i = 0; i < lines.size();) {
			if (i + 3 < lines.size() && lines.get(i + 1).contains(eventName) && lines.get(i + 1).contains(artist)) {
				i += 5; // 跳过该booking（4行+空行）
			} else {
				filteredLines.add(lines.get(i++));
			}
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(Consumer.DEFAULT_FILE_PATH))) {
			for (String filteredLine : filteredLines) {
				writer.write(filteredLine);
				writer.newLine();
			}
		} catch (IOException e) {
			System.err.println("Error writing booking info: " + e.getMessage());
		}
	}

	private List<String> readAllLines() {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(DEFAULT_FILE_PATH))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}
		return lines;
	}

	// Utility method: Write the entire file
	private void writeAllLines(List<String> lines) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(DEFAULT_FILE_PATH))) {
			for (String line : lines) {
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}
	}
}
