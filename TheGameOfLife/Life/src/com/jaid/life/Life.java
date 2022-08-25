package com.jaid.life;

import java.awt.*;
import javax.swing.*;

import com.jaid.ui.MenuSite;

public final class Life extends JFrame {

    private static JComponent universe;

    public static void main(String[] arguments) {
        new Life();
    }

    private Life() {
        super("The Game of Life");
        MenuSite.establish(this);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(Universe.instance(), BorderLayout.CENTER);
        pack();
        setVisible(true);
    }


}
