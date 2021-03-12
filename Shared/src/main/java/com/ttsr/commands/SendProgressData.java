package com.ttsr.commands;

import java.io.Serializable;

public class SendProgressData implements Serializable {

    private final Double progress;

    public SendProgressData(Double progress) {
        this.progress = progress;
    }

    public Double getProgress() {
        return progress;
    }
}
