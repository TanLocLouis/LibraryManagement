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

public class RegisterFrame extends JFrame {
    private final LibrarianService librarianService;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;

    public RegisterFrame() {
        this(new LibrarianService());
    }

    public RegisterFrame(LibrarianService librarianService) {
        super("Library Management - Register");
        this.librarianService = librarianService;
        this.usernameField = new JTextField(18);
        this.passwordField = new JPasswordField(18);
        this.confirmPasswordField = new JPasswordField(18);
        initialize();
    }

    public void initialize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        add(buildForm(), BorderLayout.CENTER);

        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.add(buildRegisterButton());
        buttonPanel.add(buildBackButton());
        add(buttonPanel, BorderLayout.SOUTH);

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

        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), constraints);

        constraints.gridx = 1;
        panel.add(confirmPasswordField, constraints);

        return panel;
    }

    private JButton buildRegisterButton() {
        JButton registerButton = new JButton("Create Account");
        registerButton.addActionListener(e -> handleRegister());
        return registerButton;
    }

    private JButton buildBackButton() {
        JButton backButton = new JButton("Back to Login");
        backButton.addActionListener(e -> {
            new LoginFrame(librarianService).setVisible(true);
            dispose();
        });
        return backButton;
    }

    private void handleRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            librarianService.register(username, password);
            JOptionPane.showMessageDialog(this, "Account created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            new LoginFrame(librarianService).setVisible(true);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
