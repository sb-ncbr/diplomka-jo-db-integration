package com.example.model;

import com.example.services.entrypoints.secondaryFiltering.SketchDataSingleton;

public class SimpleProtein {
    private final int intId;
    private final String gesamtId;

    public SimpleProtein(int intId, String gesamtId) {
        this.intId = intId;
        this.gesamtId = gesamtId;
    }

    public int getIntId() {
        return intId;
    }

    public String getGesamtId() {
        return gesamtId;
    }

    public long[] getBigSketch() {
        return SketchDataSingleton.getInstance().getSketch(this.intId);
    }
}

