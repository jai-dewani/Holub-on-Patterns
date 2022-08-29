package com.jaid.database;

import java.util.Iterator;
import java.util.NoSuchElementException;

public interface Cursor {
    String tableName();
    boolean advance() throws NoSuchElementException;
    Object column(String column);
    Iterator columns();
    boolean isTraversing(Table t);
    Object update(String columnName, Object newValue);
    void delete();
}
