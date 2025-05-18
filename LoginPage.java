package GUI8;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LoginPage extends JFrame {
	private CardLayout cardLayout;
	private JPanel formPanel;
	private login loginService = new login();

	public LoginPage() {
		setTitle("Login - Event Management System");
		setSize(800, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		setLayout(new BorderLayout());

		// Create navigation panel
		JPanel navPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		navPanel.setBackground(new Color(50, 50, 50));

		JButton btnConsumer = new JButton("Consumer");
		JButton btnOrganizer = new JButton("Organizer");

		navPanel.add(btnConsumer);
		navPanel.add(btnOrganizer);

		add(navPanel, BorderLayout.WEST);

		// Create main form panel
		cardLayout = new CardLayout();
		formPanel = new JPanel(cardLayout);

		formPanel.add(createConsumerForm(), "Consumer");
		formPanel.add(createOrganizerForm(), "Organizer");

		add(formPanel, BorderLayout.CENTER);

		// Button actions
		btnConsumer.addActionListener(e -> cardLayout.show(formPanel, "Consumer"));
		btnOrganizer.addActionListener(e -> cardLayout.show(formPanel, "Organizer"));
	}

	private JPanel createConsumerForm() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel title = new JLabel("Consumer Login", JLabel.CENTER);
		title.setFont(new Font("Arial", Font.BOLD, 24));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		panel.add(title, gbc);

		gbc.gridwidth = 1;

		JLabel lblName = new JLabel("Name:");
		JTextField txtName = new JTextField(20);

		JLabel lblID = new JLabel("ID:");
		JTextField txtID = new JTextField(20);

		JLabel lblPhone = new JLabel("Phone:");
		JTextField txtPhone = new JTextField(20);

		JLabel lblPassword = new JLabel("Password:");
		JPasswordField txtPassword = new JPasswordField(20);

		JCheckBox chkVIP = new JCheckBox("VIP Consumer");

		JButton btnLogin = new JButton("Login");

		// 添加各个组件
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(lblName, gbc);
		gbc.gridx = 1;
		panel.add(txtName, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(lblID, gbc);
		gbc.gridx = 1;
		panel.add(txtID, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		panel.add(lblPhone, gbc);
		gbc.gridx = 1;
		panel.add(txtPhone, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		panel.add(lblPassword, gbc);
		gbc.gridx = 1;
		panel.add(txtPassword, gbc);

		gbc.gridx = 0;
		gbc.gridy = 5;
		panel.add(new JLabel(""), gbc); // 空位对齐
		gbc.gridx = 1;
		panel.add(chkVIP, gbc);

		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 2;
		panel.add(btnLogin, gbc);

		btnLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String ID = txtID.getText();
					String password = new String(txtPassword.getPassword());
					User user = loginService.login(ID, password);
					if (user != null) {
						EventManagementSystem ems = new EventManagementSystem();
						ems.setCurrentUser(user);
						ems.setVisible(true);
						LoginPage.this.dispose();
					}
				} catch (IllegalArgumentException ex) {
					JOptionPane.showMessageDialog(LoginPage.this, ex.getMessage(), "Login Failed",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		return panel;
	}

	private JPanel createOrganizerForm() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel title = new JLabel("Organizer Login", JLabel.CENTER);
		title.setFont(new Font("Arial", Font.BOLD, 24));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		panel.add(title, gbc);

		gbc.gridwidth = 1;

		JLabel lblName = new JLabel("Name:");
		JTextField txtName = new JTextField(20);

		JLabel lblID = new JLabel("ID:");
		JTextField txtID = new JTextField(20);

		JLabel lblPassword = new JLabel("Password:");
		JPasswordField txtPassword = new JPasswordField(20);

		JLabel lblOrganization = new JLabel("Organization Name:");
		JTextField txtOrganization = new JTextField(20);

		JButton btnLogin = new JButton("Login");

		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(lblName, gbc);
		gbc.gridx = 1;
		panel.add(txtName, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(lblID, gbc);
		gbc.gridx = 1;
		panel.add(txtID, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		panel.add(lblPassword, gbc);
		gbc.gridx = 1;
		panel.add(txtPassword, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		panel.add(lblOrganization, gbc);
		gbc.gridx = 1;
		panel.add(txtOrganization, gbc);

		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		panel.add(btnLogin, gbc);

		btnLogin.addActionListener(e -> handleOrganizerLogin(txtID, txtPassword));

		return panel;
	}

	private void handleOrganizerLogin(JTextField txtID, JPasswordField txtPassword) {
		try {
			String ID = txtID.getText();
			String password = new String(txtPassword.getPassword());
			User user = loginService.login(ID, password);
			if (user != null && user instanceof Organizer) {
				EventManagementSystem ems = new EventManagementSystem();
				ems.setCurrentUser(user);
				ems.setVisible(true);
				LoginPage.this.dispose();
			} else {
				JOptionPane.showMessageDialog(this, "Invalid credentials or user type.", "Login Failed",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			LoginPage frame = new LoginPage();
			frame.setVisible(true);
		});
	}
}