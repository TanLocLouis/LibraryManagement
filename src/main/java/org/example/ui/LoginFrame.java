package org.example.ui;

import org.example.service.LibrarianService;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginFrame extends JFrame {
    private final LibrarianService librarianService;
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginFrame() {
        this(new LibrarianService());
    }

    public LoginFrame(LibrarianService librarianService) {
        super("Library Management - Login");
        this.librarianService = librarianService;
        this.usernameField = new JTextField(18);
        this.passwordField = new JPasswordField(18);
        initialize();
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        add(buildForm(), BorderLayout.CENTER);
        add(buildLoginButton(), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private JComponent buildForm() {
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        javax.swing.JPanel panel = new javax.swing.JPanel(layout);

        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(new JLabel("Username:"), constraints);

        constraints.gridx = 1;
        panel.add(usernameField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(new JLabel("Password:"), constraints);

        constraints.gridx = 1;
        panel.add(passwordField, constraints);

        return panel;
    }

    private JButton buildLoginButton() {
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        return loginButton;
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (librarianService.authenticate(username, password)) {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}

