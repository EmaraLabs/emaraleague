package com.emaralabs.emaraleague.core.arena;

/**
 * Reusable spawn slot identifiers for arenas.
 * Slots are independent of participant identity — the tournament runtime
 * assigns players or teams to slots at match time.
 */
public enum SpawnSlot {
    A("A"),
    B("B"),
    C("C"),
    D("D");

    private final String displayName;

    SpawnSlot(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SpawnSlot fromString(String value) {
        return switch (value.toUpperCase()) {
            case "A" -> A;
            case "B" -> B;
            case "C" -> C;
            case "D" -> D;
            default -> throw new IllegalArgumentException("Invalid spawn slot: " + value);
        };
    }
}
