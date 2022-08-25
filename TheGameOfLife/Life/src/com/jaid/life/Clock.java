package com.jaid.life;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.Timer;

import com.jaid.ui.MenuSite;
import com.jaid.tools.Publisher;

public class Clock {
    private Timer clock = new Timer();
    private TimerTask tick = null;

    // Singleton
    private Clock() {
        createMenus();
    }

    private static Clock instance;

    public synchronized static Clock getInstance() {
        if (instance == null)
            instance = new Clock();
        return instance;
    }

    public void startTicking(int millisecondsBetweenTicks) {
        if (tick != null) {
            tick.cancel();
            tick = null;
        }

        if (millisecondsBetweenTicks > 0) {
            tick = new TimerTask() {
                public void run() {
                    tick();
                }
            };
            clock.scheduleAtFixedRate(tick, 0, millisecondsBetweenTicks);
        }
    }

    public void stop() {
        startTicking(0);
    }

    private void createMenus() {
        // First set up a single listener that will handle all the
        // menu-selection events except "Exit"

        ActionListener modifier =                                    //{=startSetup}
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String name = ((JMenuItem) e.getSource()).getName();
                        char toDo = name.charAt(0);

                        if (toDo == 'T')
                            tick();                      // single tick
                        else
                            startTicking(toDo == 'A' ? 500 :      // agonizing
                                    toDo == 'S' ? 150 :      // slow
                                            toDo == 'M' ? 70 :      // medium
                                                    toDo == 'F' ? 30 : 0); // fast
                    }
                };
        MenuSite.addLine(this, "Go", "Halt", modifier);
        MenuSite.addLine(this, "Go", "Tick (Single Step)", modifier);
        MenuSite.addLine(this, "Go", "Agonizing", modifier);
        MenuSite.addLine(this, "Go", "Slow", modifier);
        MenuSite.addLine(this, "Go", "Medium", modifier);
        MenuSite.addLine(this, "Go", "Fast", modifier); // {=endSetup}
    }

    private Publisher publisher = new Publisher();

    public void addClockListener(Listener observer) {
        publisher.subscribe(observer);
    }

    public interface Listener {
        void tick();
    }

    public void tick() {
        publisher.publish(
                new Publisher.Distributor() {
                    public void deliverTo(Object subscriber) {
                        ((Listener) subscriber).tick();
                    }
                }
        );
    }
}
