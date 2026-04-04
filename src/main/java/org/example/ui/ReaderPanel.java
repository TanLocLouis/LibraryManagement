package org.example.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class ReaderPanel extends JPanel {
    public ReaderPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Reader management panel"), BorderLayout.CENTER);
    }
}

