package GUI8;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Consumer extends User implements Booking {
	private String phone;
	protected static final String DEFAULT_FILE_PATH = "bookingInfo.txt";

	public Consumer(String name, String ID, String pwd, String phone) {
		super(name, ID, pwd);
		this.phone = phone;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String newPhone) {
		this.phone = newPhone;
	}

	@Override
	public void bookTicket(int amount, Event event) {
		if (amount > event.getEventAmount()) {
			throw new IllegalArgumentException("购票数量超过剩余票数");
		}

		// 更新事件的普通票数
		event.setEventAmount(event.getEventAmount() - amount);
		writeBookingInfoToFile(DEFAULT_FILE_PATH, event, amount, false);

	}

	// 只展示 cancelTicket 关键匹配修改

	@Override
	public void cancelTicket(int amount, Event event, boolean isVip) {
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

		List<String> filteredLines = new ArrayList<>();
		boolean found = false;
		String vipString = "VIP Ticket: " + (isVip ? "Yes" : "No");

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("Consumer Info: " + getName() + ", " + getID() + ", " + getPhone())
					&& i + 3 < lines.size() && lines.get(i + 1).contains(event.getEventName())
					&& lines.get(i + 1).contains(event.getArtist()) && lines.get(i + 3).equals(vipString)) {

				int recordedAmount = Integer.parseInt(lines.get(i + 2).split(": ")[1]);

				if (recordedAmount == amount) {
					// 完全取消该记录
					i += 4; // 跳过这一组
					found = true;
					continue;
				} else if (recordedAmount > amount) {
					// 部分取消，修改剩余数量
					lines.set(i + 2, "Ticket Amount: " + (recordedAmount - amount));
					found = true;
				}
			}
			filteredLines.add(line);
		}

		if (!found) {
			throw new IllegalArgumentException(
					"No matching booking found to cancel. Please check the event details and ticket type.");
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(DEFAULT_FILE_PATH))) {
			for (String filteredLine : filteredLines) {
				writer.write(filteredLine);
				writer.newLine();
			}
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}

		// 恢复票数
		if (isVip) {
			event.setEventVip(event.getEventVip() + amount);
		} else {
			event.setEventAmount(event.getEventAmount() + amount);
		}
	}

	protected void writeBookingInfoToFile(String filePath, Event event, int ticketAmount, boolean isVip) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
			String consumerInfo = getName() + ", " + getID() + ", " + getPhone();
			String eventInfo = event.getEventName() + ", " + event.getEventDate() + ", " + event.getEventLocation();
			writer.write("Consumer Info: " + consumerInfo);
			writer.newLine();
			writer.write("Event Info: " + eventInfo);
			writer.newLine();
			writer.write("Ticket Amount: " + ticketAmount);
			writer.newLine();
			writer.write("VIP Ticket: " + (isVip ? "Yes" : "No"));
			writer.newLine();
			writer.newLine();
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}
	}
}