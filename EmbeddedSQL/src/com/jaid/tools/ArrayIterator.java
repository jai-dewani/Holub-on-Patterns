package com.jaid.tools;

import java.util.*;

public final class ArrayIterator implements Iterator {
    private int position = 0;
    private final Object[] items;

    public ArrayIterator(Object[] items) {
        this.items = items;
    }

    public boolean hasNext() {
        return (position < items.length);
    }

    public Object next() {
        if(position >= items.length)
            throw new NoSuchElementException();
        return items[position];
    }

    public void rename(){
        throw new UnsupportedOperationException("ArrayIterator.remove()");
    }

    public Object[] toArray(){
        return (Object[]) items.clone();
    }


}
