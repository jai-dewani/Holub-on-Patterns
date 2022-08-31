package com.jaid.database;

import java.io.*;
import java.util.*;

import com.jaid.tools.ArrayIterator;

public class ConcreteTable implements Table {
    private LinkedList rowSet = new LinkedList();
    private String[] columnNames;
    private String tableName;
    private transient boolean isDirty = false;
    private transient LinkedList transactionStack = new LinkedList();

    public ConcreteTable(String tableName, String[] columnNames) {
        this.tableName = tableName;
        this.columnNames = columnNames;
    }

    private int indexOf(String columnName) {
        for (int i = 0; i < columnNames.length; ++i)
            if (columnNames[i].equals(columnName))
                return i;

        throw new IndexOutOfBoundsException("Column (" + columnName + ") doesn't exist in " + tableName);
    }

    public ConcreteTable(Table.Importer importer) throws IOException {
        importer.startTable();

        tableName = importer.loadTableName();
        int width = importer.loadWidth();
        Iterator columns = importer.loadColumnNames();

        this.columnNames = new String[width];
        for (int i = 0; columns.hasNext(); )
            columnNames[i++] = (String) columns.next();

        while ((columns = importer.loadRow()) != null) {
            Object[] current = new Object[width];
            for (int i = 0; columns.hasNext(); )
                current[i++] = columns.next();
            this.insert(current);
        }
    }

    public void export(Table.Exporter exporter) throws IOException {
        exporter.startTable();
        exporter.storeMetadata(tableName,
                columnNames.length,
                rowSet.size(),
                new ArrayIterator(columnNames));

        for (Iterator i = rowSet.iterator(); i.hasNext(); )
            exporter.storeRow(new ArrayIterator((Object[]) i.next()));

        exporter.endTable();
        isDirty = false;
    }

    public int insert(String[] intoTheseColumsn, Object[] values) {
        assert (intoTheseColumsn.length == values.length)
                : "There must be exactly one value for each specified column";

        Object[] newRow = new Object[width()];

        for (int i = 0; i < intoTheseColumsn.length; ++i)
            newRow[indexOf(intoTheseColumsn[i])] = values[i];

        doInsert(newRow);
        return 1;
    }

    public int insert(Collection intoTheseColumsn, Collection values) {
        assert (intoTheseColumsn.size() == values.size())
                : "There must be exactly one value ofr each specified column";

        Object[] newRow = new Object[width()];

        Iterator v = values.iterator();
        Iterator c = intoTheseColumsn.iterator();
        while (c.hasNext() && v.hasNext())
            newRow[indexOf((String) c.next())] = v.next();

        doInsert(newRow);
        return 1;
    }

    public int insert(Map row) {
        return insert(row.keySet(), row.values());
    }

    public int insert(Object[] values) {
        doInsert((Object[]) values.clone());
        return 1;
    }

    public int insert(Collection values) {
        return insert(values.toArray());
    }

    public void doInsert(Object[] newRow) {
        rowSet.add(newRow);
        registerInsert(newRow);
        isDirty = true;
    }

    public Cursor rows() {
        return new Results();
    }

    private final class Results implements Cursor {
        private final Iterator rowIterator = rowSet.iterator();
        private Object[] row = null;

        public String tableName() {
            return ConcreteTable.this.tableName;
        }

        public boolean advance() {
            if (rowIterator.hasNext()) {
                row = (Object[]) rowIterator.next();
                return true;
            }
            return false;
        }

        public Object column(String columnName) {
            return row[indexOf(columnName)];
        }

        public Iterator columns() {
            return new ArrayIterator(row);
        }

        public boolean isTraversing(Table t) {
            return t == ConcreteTable.this;
        }

        private Object[] cloneRow() {
            return (Object[]) (row.clone());
        }

        public Object update(String columnName, Object newValue) {
            int index = indexOf(columnName);

            if (row[index] == newValue)
                throw new IllegalArgumentException(
                        "May not replace object with itself");

            Object oldValue = row[index];
            row[index] = newValue;
            isDirty = true;

            registerUpdate(row, index, oldValue);
            return oldValue;
        }

        public void delete() {
            Object[] oldRow = row;
            rowIterator.remove();
            isDirty = true;
            registerDelete(oldRow);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    //Undo subsystem
    private interface Undo {
        void execute();
    }

    private class UndoInsert implements Undo {
        private final Object[] insertedRow;

        public UndoInsert(Object[] insertedRow) {
            this.insertedRow = insertedRow;
        }

        public void execute() {
            rowSet.remove(insertedRow);
        }
    }

    private class UndoDelete implements Undo {
        private final Object[] deleteRow;

        public UndoDelete(Object[] deleteRow) {
            this.deleteRow = deleteRow;
        }

        public void execute() {
            rowSet.add(deleteRow);
        }
    }

    private class UndoUpdate implements Undo {
        private Object[] row;
        private int cell;
        private Object oldContents;

        public UndoUpdate(Object[] row, int cell, Object oldContents) {
            this.row = row;
            this.cell = cell;
            this.oldContents = oldContents;
        }

        public void execute() {
            row[cell] = oldContents;
        }
    }

    public void begin() {
        transactionStack.addLast(new LinkedList());
    }

    private void register(Undo op) {
        ((LinkedList) transactionStack.getLast()).addLast(op);
    }

    private void registerUpdate(Object[] row, int cell, Object oldContents) {
        if (!transactionStack.isEmpty())
            register(new UndoUpdate(row, cell, oldContents));
    }

    private void registerDelete(Object[] oldRow) {
        if (!transactionStack.isEmpty())
            register(new UndoDelete(oldRow));
    }

    private void registerInsert(Object[] newRow) {
        if (!transactionStack.isEmpty())
            register(new UndoInsert(newRow));
    }

    public void commit(boolean all) throws IllegalStateException {
        if (transactionStack.isEmpty())
            throw new IllegalStateException("No BEGIN for COMMIT");
        do {
            LinkedList currentLevel = (LinkedList) transactionStack.removeLast();

            if (!transactionStack.isEmpty())
                ((LinkedList) transactionStack.getLast()).addAll(currentLevel);
        } while (all & !transactionStack.isEmpty());
    }

    public void rollback(boolean all) throws IllegalStateException {
        if (transactionStack.isEmpty())
            throw new IllegalStateException("No BEGIN for ROLLBACK");

        do {
            LinkedList currentLevel = (LinkedList) transactionStack.removeLast();
            while (!transactionStack.isEmpty())
                ((Undo) currentLevel.removeLast()).execute();
        } while (all && !transactionStack.isEmpty());
    }

    public int update(Selector where) {
        Results currentRow = (Results) rows();
        Cursor[] envelope = new Cursor[]{currentRow};
        int updated = 0;

        while (currentRow.advance()) {
            if (where.approve(envelope)) {
                where.modify(currentRow);
                ++updated;
            }
        }

        return updated;
    }

    public int delete(Selector where) {
        int deleted = 0;

        Results currentRow = (Results) rows();
        Cursor[] envelope = new Cursor[]{currentRow};

        while (currentRow.advance()) {
            if (where.approve(envelope)) {
                where.modify(currentRow);
                ++deleted;
            }
        }
        return deleted;
    }

    public Table select(Selector where) {
        Table resultTable = new ConcreteTable(null, (String[]) columnNames.clone());

        Results currentRow = currentRow = (Results) rows();
        Cursor[] envelope = new Cursor[]{currentRow};

        while (currentRow.advance()) {
            if (where.approve(envelope))
                resultTable.insert((Object[]) currentRow.cloneRow());

        }
        return new UnmodifiableTable(resultTable);
    }

    public Table select(Selector where, String[] requestedColumns) {
        if (requestedColumns == null)
            return select(where);

        Table resultTable = new ConcreteTable(null, (String[]) requestedColumns.clone());

        Results currentRow = (Results) rows();
        Cursor[] envelope = new Cursor[]{currentRow};

        while (currentRow.advance()) {
            if (where.approve(envelope)) {
                Object[] newRow = new Object[requestedColumns.length];
                for (int column = 0; column < requestedColumns.length; column++) {
                    newRow[column] = currentRow.column(requestedColumns[column]);
                }
                resultTable.insert(newRow);
            }
        }
        return new UnmodifiableTable(resultTable);
    }

    public Table select(Selector where, String[] requestedColumns, Table[] otherTables) {
        if (otherTables == null || otherTables.length == 0)
            return select(where, requestedColumns);

        Table[] allTables = new Table[otherTables.length + 1];
        allTables[0] = this;
        System.arraycopy(otherTables, 0, allTables, 1, otherTables.length);

        Table resultTable = new ConcreteTable(null, requestedColumns);
        Cursor[] envelope = new Cursor[allTables.length];

        selectFromCartesianProduct(0, where, requestedColumns,
                allTables, envelope, resultTable);

        return new UnmodifiableTable(resultTable);
    }

    private static void selectFromCartesianProduct(
            int level,
            Selector where,
            String[] requestedColumns,
            Table[] allTables,
            Cursor[] allIterators,
            Table resultTable) {
        allIterators[level] = allTables[level].rows();
        while (allIterators[level].advance()) {
            if (level < allIterators.length - 1)
                selectFromCartesianProduct(level + 1, where,
                        requestedColumns, allTables,
                        allIterators, resultTable);

            if (level == allIterators.length - 1) {
                if (where.approve(allIterators))
                    insertApprovedRows(resultTable,
                            requestedColumns, allIterators);
            }
        }
    }

    private static void insertApprovedRows(Table resultTable,
                                           String[] requestedColumns,
                                           Cursor[] allTables) {
        Object[] resultRow = new Object[requestedColumns.length];

        for (int i = 0; i < requestedColumns.length; ++i) {
            for (int table = 0; table < allTables.length; ++table) {
                try {
                    resultRow[i] = allTables[table].column(requestedColumns[i]);
                } catch (Exception e) {
                    // otherwise, try the next table
                }
            }
        }
        resultTable.insert(resultRow);
    }

    public Table select(Selector where, Collection requestedColumns, Collection other) {
        String[] columnNames = null;
        Table[] otherTables = null;

        if (requestedColumns != null) {
            columnNames = new String[requestedColumns.size()];
            int i = 0;
            Iterator column = requestedColumns.iterator();

            while (column.hasNext())
                columnNames[i++] = column.next().toString();
        }

        if (other != null)
            otherTables = (Table[]) other.toArray(new Table[other.size()]);

        return select(where, columnNames, otherTables);
    }

    public Table select(Selector where, Collection requestedColumns) {
        return select(where, requestedColumns, null);
    }





}
