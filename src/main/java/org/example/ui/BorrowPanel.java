package org.example.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class BorrowPanel extends JPanel {
    public BorrowPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Borrow slip panel"), BorderLayout.CENTER);
    }
}

