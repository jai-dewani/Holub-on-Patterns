package com.jaid.database;

import java.io.*;
import java.util.*;
import com.jaid.tools.ArrayIterator;

public class ConcreteTable implements Table
{
    private LinkedList rowSet = new LinkedList();
    private String[] columnNames;
    private String tableName;

    private transient boolean isDirty = false;
    private transient LinkedList transactionStack = new LinkedList();

    public ConcreteTable(String tableName, String[] columnNames)
    {
        this.tableName = tableName;
        this.columnNames = columnNames;
    }

    private int indexOf(String columnName)
    {
        for(int i=0;i<columnNames.length; ++i)
            if(columnNames[i].equals(columnName))
                return i;

        throw new IndexOutOfBoundsException("Column ("+columnName+") doesn't exist in " + tableName);
    }
}
