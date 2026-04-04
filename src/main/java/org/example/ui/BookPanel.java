package org.example.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class BookPanel extends JPanel {
    public BookPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Book management panel"), BorderLayout.CENTER);
    }
}

