package com.emaralabs.emaraleague.infrastructure.database;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Repository<T, ID> {

    CompletableFuture<T> save(T entity);

    CompletableFuture<Optional<T>> findById(ID id);

    CompletableFuture<List<T>> findAll();

    CompletableFuture<Void> delete(ID id);

    CompletableFuture<T> update(T entity);
}
