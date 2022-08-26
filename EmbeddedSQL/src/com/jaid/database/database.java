package com.jaid.database;

import java.io.*;
import java.util.*;
import com.jaid.database.Selector;

public interface Table extends Serializable, Cloneable
{
    // Return a shallow copy of the table
    Object clone() throws CloneNotSupportedException;

    // Return the table name
    String name();

    // Rename the table to the indicated name
    void rename(String newName);

    // Return true if this table has changed since it was last saved.
    // The dirty bit is cleared when you export the table
    boolean isDirty();

    /** Insert new values into the table corresponding to the
     * specified column names. For example, the value at
     * <code>values[i]</code> is put into the column specified in
     * <code>columnNames[i]</code>
     * @param columnNames
     * @param values
     * @return the number of rows affectd by the operation
     * @throws IndexOutOfBoundsException One of the requested column doesn't exist in either table
     */
    int insert(String[] columnNames, Object[] values);

    /**A convenience overload of {@link #insert(String[], Object[])} */
    int insert(Collection columnNames, Collection values);

    /**
     * In this insert, values must have as many elements as there are columns,
     * and in the same order of columns
     * @param values
     * @return the number of rows affected by the operation
     */
    int insert(Object[] values);

    /** A convenience overload of {@link #insert(Object[])} */
    int insert(Collection values);

    /** Update the cells in the table. The {@link Selector} object server
     * as a visitor whose <code>includeInSelect(...)</code> method
     * is called for each row in the table.
     * @param where
     * @return the number of rows affected by the operation.
     */
    int update(Selector where);

    /**
     * Delete from the table all rows approved by the Selector.
     * @param where
     * @return the number of rows affected by the operation
     */
    int delete(Selector where);

    /** begin a transaction */
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
        public void startTable() throws  IOException;
        public void storeMetadata(
                String tableName,
                int width,
                int height,
                Iterator columnNames) throws IOException;
        public void storeRow(Iterator data) throws IOException;
        public void endTable() throws IOException;
    }

    public interface Importer
    {
        void startTable() throws IOException;
        String loadTableName() throws IOException;
        int loadWidth() throws IOException;
        Iterator loadColumnNames() throws IOException;
        Iterator loadRow() throws IOException;
        void endTable() throws IOException;
    }
}