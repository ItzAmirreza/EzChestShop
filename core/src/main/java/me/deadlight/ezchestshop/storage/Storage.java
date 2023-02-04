package me.deadlight.ezchestshop.storage;

import java.io.Closeable;
import java.io.IOException;

public interface Storage extends Closeable {

    void load() throws IOException;

}
