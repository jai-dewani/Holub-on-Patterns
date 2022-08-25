package com.jaid.life;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import com.jaid.io.Files;
import com.jaid.ui.MenuSite;

public class Universe extends JPanel {
    private final Cell outermostCell;
    private static final Universe theInstance = new Universe();

    private static final int DEFAULT_GRID_SIZE = 8;
    private static final int DEFAULT_CELL_SIZE = 8;

    private Universe() {
        outermostCell = new Neighborhood
                (
                        DEFAULT_GRID_SIZE,
                        new Neighborhood
                                (
                                        DEFAULT_CELL_SIZE,
                                        new Resident()
                                )
                );
        final Dimension PREFERRED_SIZE =
                new Dimension(
                        outermostCell.widthInCells() * DEFAULT_CELL_SIZE,
                        outermostCell.widthInCells() * DEFAULT_GRID_SIZE
                );
        addComponentListener(
                new ComponentAdapter() {
                    public void componentResized(ComponentEvent e) {
                        Rectangle bounds = getBounds();
                        bounds.height /= outermostCell.widthInCells();
                        bounds.height *= outermostCell.widthInCells();
                        //noinspection SuspiciousNameCombination
                        bounds.width = bounds.height;
                        setBounds(bounds);
                    }
                }
        );

        setBackground(Color.white);
        setPreferredSize(PREFERRED_SIZE);
        setMaximumSize(PREFERRED_SIZE);
        setMinimumSize(PREFERRED_SIZE);
        setOpaque(true);

        addMouseListener(
                new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        Rectangle bounds = getBounds();
                        bounds.x = 0;
                        bounds.y = 0;
                        outermostCell.userClicked(e.getPoint(), bounds);
                        repaint();
                    }
                }
        );

        MenuSite.addLine(this, "Grid", "Clear",
                e -> {
                    outermostCell.clear();
                    repaint();
                });

        MenuSite.addLine(this, "Grid", "Load",
                e -> doLoad());

        MenuSite.addLine(this, "Grid", "Store",
                e -> doStore());

        MenuSite.addLine(this, "Grid", "Exit",
                e -> System.exit(0));

        Clock.getInstance().addClockListener(
                () -> {
                    if (outermostCell.figureNextState
                            (Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY,
                                    Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY
                            )
                    ) {
                        if (outermostCell.transition())
                            refreshNow();
                    }
                }
        );
    }

    public static Universe instance() {
        return theInstance;
    }

    private void doLoad() {
        try {
            FileInputStream in = new FileInputStream(
                    Files.userSelected(".", ".life", "Life File", "Load"));

            Clock.getInstance().stop();        // stop the game and
            outermostCell.clear();            // clear the board.

            Storable memento = outermostCell.createMemento();
            memento.load(in);
            outermostCell.transfer(memento, new Point(0, 0), Cell.LOAD);

            in.close();
        } catch (IOException theException) {
            JOptionPane.showMessageDialog(null, "Read Failed!",
                    "The Game of Life", JOptionPane.ERROR_MESSAGE);
        }
        repaint();
    }

    private void doStore() {
        try {
            FileOutputStream out = new FileOutputStream(
                    Files.userSelected(".", ".life", "Life File", "Write"));

            Clock.getInstance().stop();        // stop the game

            Storable memento = outermostCell.createMemento();
            outermostCell.transfer(memento, new Point(0, 0), Cell.STORE);
            memento.flush(out);

            out.close();
        } catch (IOException theException) {
            JOptionPane.showMessageDialog(null, "Write Failed!",
                    "The Game of Life", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void paint(Graphics g) {
        Rectangle panelBounds = getBounds();
        Rectangle clipBounds = g.getClipBounds();

        // The panel bounds is relative to the upper-left
        // corner of the screen. Pretend that it's at (0,0)
        panelBounds.x = 0;
        panelBounds.y = 0;
        outermostCell.redraw(g, panelBounds, true);        //{=Universe.redraw1}
    }

    private void refreshNow() {
        SwingUtilities.invokeLater
                (() -> {
                    Graphics g = getGraphics();
                    if (g == null)        // Universe not displayable
                        return;
                    try {
                        Rectangle panelBounds = getBounds();
                        panelBounds.x = 0;
                        panelBounds.y = 0;
                        outermostCell.redraw(g, panelBounds, false); //{=Universe.redraw2}
                    } finally {
                        g.dispose();
                    }
                }
                );
    }
}

