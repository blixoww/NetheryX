package com.netheryx.world;

import java.util.Locale;

/** Evenements dynamiques du Nether. */
public enum NetherEventType {
    NONE(0, 0x000000),
    /** Pluie de cendres : brouillard gris epais, spectres et chevaliers fletris renforces. */
    ASHFALL(6000, 0x5A5654),
    /** Tempete de soufre : de la poudre de soufre tombe de la voute, a ramasser. */
    SULFUR_STORM(4800, 0x8A7A2A),
    /** Eruption : bombes volcaniques et projections de minerais dans les Failles de magma et deltas de basalte. */
    ERUPTION(3600, 0x7A1E06);

    private final int duration;
    private final int fogColor;

    NetherEventType(int duration, int fogColor) {
        this.duration = duration;
        this.fogColor = fogColor;
    }

    public int duration() {
        return this.duration;
    }

    public int fogColor() {
        return this.fogColor;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static NetherEventType byId(int ordinal) {
        NetherEventType[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NONE;
    }
}
