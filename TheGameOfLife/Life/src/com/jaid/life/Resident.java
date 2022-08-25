package com.jaid.life;

import java.awt.*;
import javax.swing.*;

import com.jaid.ui.Colors;

import com.jaid.life.Cell;
import com.jaid.life.Storable;
import com.jaid.life.Neighborhood;
import com.jaid.life.Direction;
import com.jaid.life.Universe;

public final class Resident implements Cell {

    private static final Color BORDER_COLOR = Colors.DARK_YELLOW;
    private static final Color LIVE_COLOR = Color.RED;
    private static final Color DEAD_COLOR = Colors.LIGHT_YELLOW;

    private boolean amAlive = false;
    private boolean willBeAlive = false;

    private boolean isStable() {
        return amAlive == willBeAlive;
    }



    public boolean figureNextState(Cell north, Cell south, Cell east, Cell west, Cell northeast, Cell northwest, Cell southeast, Cell southwest) {
        verify(north, "north");
        verify(south, "south");
        verify(east, "east");
        verify(west, "west");
        verify(northeast, "northeast");
        verify(northwest, "northwest");
        verify(southeast, "southeast");
        verify(southwest, "southwest");

        int neighbors = 0;

        if(north.isAlive()) ++ neighbors;
        if(south.isAlive()) ++ neighbors;
        if(west.isAlive()) ++ neighbors;
        if(east.isAlive()) ++ neighbors;
        if(northeast.isAlive()) ++ neighbors;
        if(northwest.isAlive()) ++ neighbors;
        if(southeast.isAlive()) ++ neighbors;
        if(southwest.isAlive()) ++ neighbors;

        willBeAlive = (neighbors == 3 || (amAlive && neighbors==2));
        return !isStable();
    }

    private void verify(Cell c, String direction){
        assert (c instanceof Resident) || (c == Cell.DUMMY)
                : "incorrect type for " + direction + ": " + c.getClass().getName();
    }

    public Cell edge(int row, int column) {
        assert row==0 && column==0;
        return this;
    }

    public boolean transition() {
        boolean changed = isStable();
        amAlive = willBeAlive;
        return changed;
    }

    public void redraw(Graphics g, Rectangle here, boolean drawAll) {
        g = g.create();
        g.setColor(amAlive ? LIVE_COLOR : DEAD_COLOR);
        g.fillRect(here.x+1, here.y+1,here.width-1, here.height-1);

        g.setColor(BORDER_COLOR);
        g.drawLine(here.x, here.y, here.x,here.y+here.height);
        g.drawLine(here.x, here.y, here.x + here.width, here.y);
        g.dispose();
    }

    public void userClicked(Point here, Rectangle surface) {
        amAlive = !amAlive;
    }

    public boolean isAlive() {
        return amAlive;
    }

    public int widthInCells() {
        return 1;
    }

    public Cell create() {
        return new Resident();
    }

    public Direction isDisruptiveTo() {
        return isStable() ? Direction.NONE : Direction.ALL;
    }

    public void clear() {
        amAlive = willBeAlive = false;
    }

    @Override
    public boolean transfer(Storable memento, Point upperLeftCorner, boolean doLoad) {
        return false;
    }

    @Override
    public Storable createMemento() {
        return null;
    }

}
