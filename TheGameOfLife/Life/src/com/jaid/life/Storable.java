package com.jaid.life;

import java.io.*;

public interface Storable {
    void load(InputStream in) throws IOException;

    void flush(OutputStream out) throws IOException;
}
