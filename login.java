package GUI8;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class login {
	private Map<String, User> userDatabase = new HashMap<>();
	private static final String USER_INFO_FILE = "userInfo.txt";

	public login() {
		loadUsersFromFile();
	}

	private void loadUsersFromFile() {
		try (BufferedReader reader = new BufferedReader(new FileReader(USER_INFO_FILE))) {
			String line;
			List<String> block = new ArrayList<>();
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					parseAndAddUser(block);
					block.clear();
				} else {
					block.add(line);
				}
			}
			if (!block.isEmpty()) {
				parseAndAddUser(block); // 加载最后一个用户
			}
		} catch (IOException e) {
			System.err.println("Error reading user info file: " + e.getMessage());
		}
	}

	private void parseAndAddUser(List<String> block) {
		String userType = "", name = "", ID = "", password = "", phone = "", company = "";
		boolean vipStatus = false;

		for (String line : block) {
			if (line.startsWith("User Type:"))
				userType = line.split(": ")[1].trim();
			else if (line.startsWith("Name:"))
				name = line.split(": ")[1].trim();
			else if (line.startsWith("ID:"))
				ID = line.split(": ")[1].trim();
			else if (line.startsWith("Password:"))
				password = line.split(": ")[1].trim();
			else if (line.startsWith("Phone:"))
				phone = line.split(": ")[1].trim();
			else if (line.startsWith("Company:"))
				company = line.split(": ")[1].trim();
			else if (line.startsWith("VIP:"))
				vipStatus = Boolean.parseBoolean(line.split(": ")[1].trim());
		}

		User user = null;
		switch (userType) {
		case "Organizer":
			user = new Organizer(name, ID, password, company);
			break;
		case "Vip":
			user = new Vip(name, ID, password, phone, vipStatus);
			break;
		case "Regular":
			user = new Regular(name, ID, password, phone);
			break;
		}

		if (user != null) {
			userDatabase.put(ID, user);
		}
	}

	public User login(String ID, String password) {
		User user = userDatabase.get(ID);
		if (user != null && user.getPwd().equals(password)) {
			System.out.println("Login successful as " + user.getClass().getSimpleName() + ": " + user.getName());
			return user;
		} else {
			throw new IllegalArgumentException("Invalid ID or password.");
		}
	}

}
