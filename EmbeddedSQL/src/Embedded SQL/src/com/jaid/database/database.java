package com.jaid;

import java.io.*;
import java.util.*;
import com.jaid.database.Selector;

public interface Table extends Serializable, Cloneable
{
    Object clone();

    String name();

    void rename(String newName);

    boolean isDirty();

    int insert(String[] columnNames, Object[] values);

    int insert(Collection columnNames, Collection values);

    int insert(Object[] values);

    int insert(Collection values);

    int update(Selector where);

    int delete(Selector where);

    public void begin();

    public void commit(boolean all) throws IllegalStateException;

    public void rollback(boolean all) throws  IllegalStateException;

    public static final boolean THIS_LEVEL = false;

    public static final boolean ALL = true;

    Table select(Selector where, String[] requestedColumns, Table[] other);

    Table select(Selector where, String[] requestedColumns);

    Table select(Selector where);

    Table select(Selector where, Collection requestedColumns, Collection other);

    Table select(Selector where, Collection requestedColumns);

    Cursor rows();

    void export(Table.Exporter importer) throws IOException;


    public interface Exporter
    {
        public void startTable() throws  
    }
}