package com.emaralabs.emaraleague.infrastructure.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TournamentRepository implements Repository<Object, UUID> {

    @Override
    public CompletableFuture<Object> save(Object entity) {
        return CompletableFuture.completedFuture(entity);
    }

    @Override
    public CompletableFuture<Optional<Object>> findById(UUID id) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<List<Object>> findAll() {
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    @Override
    public CompletableFuture<Void> delete(UUID id) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Object> update(Object entity) {
        return CompletableFuture.completedFuture(entity);
    }
}
