package com.jaid.database;

import java.io.*;

public class TableFactory
{
    public static Table create(String name, String[] columns)
    {
        return new ConcreteTable(name,columns);
    }

    public static Table create(Table.Importer importer) throws IOException
    {
        return new ConcreteTable(importer);
    }

    public static Table load(String name) throws IOException
    {
        return load(name, new File("."));
    }
    public static Table load(String name, String location) throws IOException
    {
        return load(name, new File(location));
    }

    public static Table load(String name, File directory) throws IOException
    {
        if(!(name.endsWith(".csv") || name.endsWith(".CSV")))
            throw new java.io.IOException(
                    "Filename (" + name + ") does not end in "
                    + "supported extension (.csv)");
        Reader in = new FileReader(new File(directory, name));
        Table loaded = new ConcreteTable(new CSVImporter(in));
        in.close();
        return loaded;
    }

}
