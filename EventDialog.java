package eventManageSystem;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EventDialog {
	public Event showDialog() {
		JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
		JTextField txtName = new JTextField();
		JTextField txtArtist = new JTextField();
		JTextField txtDate = new JTextField();
		JTextField txtLocation = new JTextField();
		JTextField txtRegularTickets = new JTextField();
		JTextField txtVipTickets = new JTextField();

		panel.add(new JLabel("Event Name:"));
		panel.add(txtName);
		panel.add(new JLabel("Artist:"));
		panel.add(txtArtist);
		panel.add(new JLabel("Date (yyyy-MM-dd):"));
		panel.add(txtDate);
		panel.add(new JLabel("Location:"));
		panel.add(txtLocation);
		panel.add(new JLabel("Regular Tickets:"));
		panel.add(txtRegularTickets);
		panel.add(new JLabel("VIP Tickets:"));
		panel.add(txtVipTickets);

		int result = JOptionPane.showConfirmDialog(null, panel, "Add Event", JOptionPane.OK_CANCEL_OPTION);
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
					throw new InvalidEventInputException("The date format must beyyyy-MM-dd。");
				}
				if (regTickets < 0 || vipTickets < 0) {
					throw new InvalidEventInputException("The number of votes must be a non-negative integer.");
				}
				return new Event(name, artist, date, regTickets, vipTickets, location);
			} catch (NumberFormatException e) {
				throw new InvalidEventInputException("The number of votes must be an integer.");
			}
		}
		return null;
	}
}