package GUI10;

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
		} else if (amount > event.getEventVip()) {
			throw new IllegalArgumentException("VIP 购票数量超过剩余票数");
		} else {
			event.setEventVip(event.getEventVip() - amount); // 减少 VIP 票数
			writeBookingInfoToFile(DEFAULT_FILE_PATH, event, amount, true); // 写入购票信息，标记为 VIP
		}
	}

	@Override
	public void cancelTicket(int amount, Event event, boolean isVip) {
		// 读取文件内容
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

		// 过滤记录
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
			}

			// 加入保留的行
			filteredLines.add(line);
		}

		if (!found) {
			throw new IllegalArgumentException("No matching booking found to cancel.");
		}

		// 重写文件内容
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(DEFAULT_FILE_PATH))) {
			for (String filteredLine : filteredLines) {
				writer.write(filteredLine);
				writer.newLine();
			}
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}

		// 恢复票数
		if (isVipBooking) {
			event.setEventVip(event.getEventVip() + amount);
		} else {
			event.setEventAmount(event.getEventAmount() + amount);
		}
	}
}