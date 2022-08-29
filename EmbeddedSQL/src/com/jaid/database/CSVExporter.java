package com.jaid.database;

import java.io.*;
import java.util.*;

public class CSVExporter implements Table.Exporter {
    private final Writer out;
    private int width;

    public CSVExporter(Writer out) {
        this.out = out;
    }

    public void storeMetadata(String tableName, int width,
                              int height, Iterator columnNames) throws IOException {
        this.width = width;
        out.write(tableName == null ? "<anonymous>" : tableName);
        out.write("\n");
        storeRow(columnNames);
    }

    public void storeRow(Iterator data) throws IOException {
        int i = width;
        while (data.hasNext()) {
            Object datum = data.next();

            if (datum != null)
                out.write(datum.toString());

            if (--i > 0)
                out.write("\t");
        }
        out.write("\n");
    }

    public void startTable() throws IOException {
    }

    public void endTable() throws IOException {
    }
}
