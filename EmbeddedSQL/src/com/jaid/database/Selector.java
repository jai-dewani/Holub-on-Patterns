package com.jaid.database;

interface Selector {
    boolean approve(Cursor[] rows);

    void modify(Cursor current);

    public static class Adapter implements Selector {
        public boolean approve(Cursor[] tables){
            return true;
        }
        public void modify(Cursor current){
            throw new UnsupportedOperationException("Can't use a Selector.Adapter in an update");
        }
    }

    public static final Selector ALL = new Selector.Adapter();
}
