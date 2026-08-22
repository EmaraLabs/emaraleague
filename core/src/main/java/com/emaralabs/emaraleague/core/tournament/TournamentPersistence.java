package com.emaralabs.emaraleague.core.tournament;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TournamentPersistence {

    CompletableFuture<Tournament> save(Tournament tournament);

    CompletableFuture<Optional<Tournament>> findById(UUID id);

    CompletableFuture<List<Tournament>> findAll();

    CompletableFuture<Void> delete(UUID id);

    CompletableFuture<Tournament> update(Tournament tournament);
}
