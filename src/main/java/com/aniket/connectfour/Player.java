package com.aniket.connectfour;

import javafx.scene.paint.Color;

public class Player {
    private final String name;
    private final Color markerColor;

    public Player(String name, Color markerColor) {
        this.name = name;
        this.markerColor = markerColor;
    }

    public String getName() {
        return name;
    }

    public Color getMarkerColor() {
        return markerColor;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
