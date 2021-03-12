package com.ttsr.commands;

import java.io.Serializable;

public class FileData implements Serializable {

    private final byte[] buffer;
    private final int pointer;

    public FileData(byte[] buffer, int pointer) {
        this.buffer = buffer;
        this.pointer = pointer;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getPointer() {
        return pointer;
    }
}
