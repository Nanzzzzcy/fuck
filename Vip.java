package eventManageSystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Vip extends Consumer {
	private boolean vip;

	public Vip(String name, String ID, String pwd, String phone, boolean vip) {
		super(name, ID, pwd, phone);
		this.vip = vip;
	}

	public void vipBooking(int amount, Event event) {
		if (!vip) {
			throw new UnauthorizedAccessException("Only VIP users can book VIP tickets.");
		}
		if (amount <= 0) {
			throw new InvalidTicketAmountException("The number of VIP tickets purchased must be a positive integer.");
		}
		if (amount > event.getEventVip()) {
			throw new IllegalArgumentException("The number of VIP tickets purchased exceeds the remaining number of tickets.");
		}
		event.setEventVip(event.getEventVip() - amount); // Reduce the number of VIP votes

		writeBookingInfoToFile(DEFAULT_FILE_PATH, event, amount, true); // Fill in the ticket purchase information and mark it as VIP

	}

	@Override
	public void cancelTicket(int amount, Event event, boolean isVip) {
		if (amount <= 0) {
			throw new InvalidTicketAmountException("The number of cancellation votes must be a positive integer.");
		}
		// Read the content of the file
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(DEFAULT_FILE_PATH))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			System.err.println("Error reading from file: " + e.getMessage());
			return;
		}

		// Filtering Records
		List<String> filteredLines = new ArrayList<>();
		boolean found = false;
		boolean isVipBooking = false;

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("Consumer Info: " + getName() + ", " + getID() + ", " + getPhone())
					&& i + 3 < lines.size() && lines.get(i + 1).startsWith("Event Info: " + event.getEventName() + ", "
							+ event.getEventDate() + ", " + event.getEventLocation())) {

				// Check both VIP and regular bookings
				String vipLine = lines.get(i + 3);
				if (vipLine.startsWith("VIP Ticket: Yes") || vipLine.startsWith("VIP Ticket: No")) {
					int recordedAmount = Integer.parseInt(lines.get(i + 2).split(": ")[1]);
					isVipBooking = vipLine.endsWith("Yes");

					if (recordedAmount == amount) {
						// Completely cancel this record
						i += 4; // Skip this group
						found = true;
						continue;
					} else if (recordedAmount > amount) {
						// Partially cancel and modify the remaining quantity
						lines.set(i + 2, "Ticket Amount: " + (recordedAmount - amount));
						found = true;
					}
				}
			}

			// Add the reserved rows
			filteredLines.add(line);
		}

		if (!found) {
			throw new IllegalArgumentException("No matching booking found to cancel.");
		}

		// Rewrite the content of the file
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(DEFAULT_FILE_PATH))) {
			for (String filteredLine : filteredLines) {
				writer.write(filteredLine);
				writer.newLine();
			}
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}

		// Restore the number of votes
		if (isVipBooking) {
			event.setEventVip(event.getEventVip() + amount);
		} else {
			event.setEventAmount(event.getEventAmount() + amount);
		}
	}
}