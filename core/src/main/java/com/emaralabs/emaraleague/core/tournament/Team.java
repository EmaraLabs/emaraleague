package com.emaralabs.emaraleague.core.tournament;

import java.util.UUID;

public record Team(UUID id, String name, int seed) {

    public Team(String name, int seed) {
        this(UUID.randomUUID(), name, seed);
    }
}
