package ru.purpir.block;

import net.minecraft.util.StringIdentifiable;

public enum CrusherPart implements StringIdentifiable {
    BOTTOM_LEFT("bottom_left", 0, 0),
    BOTTOM_RIGHT("bottom_right", 1, 0),
    TOP_LEFT("top_left", 0, 1),
    TOP_RIGHT("top_right", 1, 1);

    private final String name;
    private final int x;
    private final int y;

    CrusherPart(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    @Override
    public String asString() {
        return name;
    }
}
