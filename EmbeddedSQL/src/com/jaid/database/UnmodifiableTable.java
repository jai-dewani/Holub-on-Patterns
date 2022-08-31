package com.jaid.database;

import java.io.*;
import java.util.*;

public class UnmodifiableTable implements Table {
    private Table wrapped;
    public UnmodifiableTable(Table wrapped) {
        this.wrapped = wrapped;
    }


    public Object clone() throws CloneNotSupportedException {
        UnmodifiableTable copy =(UnmodifiableTable) super.clone();
        copy.wrapped = (Table)(wrapped.clone());
        return copy;
    }

    public String name() {
        return wrapped.name();
    }

    public void rename(String newName) {
        wrapped.rename(newName);
    }

    public boolean isDirty() {
        return wrapped.isDirty();
    }
    private final void illegal(){
        throw new UnsupportedOperationException();
    }
    public int insert(String[] columnNames, Object[] values) {
        illegal();
        return 0;
    }

    public int insert(Collection columnNames, Collection values) {
        illegal();
        return 0;
    }

    public int insert(Object[] values) {
        illegal();
        return 0;
    }

    public int insert(Collection values) {
        illegal();
        return 0;
    }

    public int update(Selector where) {
        illegal();
        return 0;
    }

    public int delete(Selector where) {
        illegal();
        return 0;
    }

    public void begin() {
        illegal();
    }

    public void commit(boolean all) throws IllegalStateException {
        illegal();
    }

    public void rollback(boolean all) throws IllegalStateException {
        illegal();
    }

    public Table select(Selector where, String[] requestedColumns, Table[] other) {
        illegal();
        return null;
    }

    public Table select(Selector where, String[] requestedColumns) {
        illegal();
        return null;
    }

    public Table select(Selector where) {
        illegal();
        return null;
    }

    public Table select(Selector where, Collection requestedColumns, Collection other) {
        illegal();
        return null;
    }

    public Table select(Selector where, Collection requestedColumns) {
        illegal();
        return null;
    }

    public Cursor rows() {
        return wrapped.rows();
    }

    public void export(Exporter exporter) throws IOException {
        wrapped.export(exporter);
    }
}
