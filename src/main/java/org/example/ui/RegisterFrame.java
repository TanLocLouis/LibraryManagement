package org.example.ui;

import javax.swing.JFrame;

public class RegisterFrame extends JFrame {
    public RegisterFrame() {
        initialize();
    }

    public void initialize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLayout(new BorderLayout(10, 10));
//        add(buildForm(), BorderLayout.CENTER);
//        add(buildLoginButton(), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }
}
