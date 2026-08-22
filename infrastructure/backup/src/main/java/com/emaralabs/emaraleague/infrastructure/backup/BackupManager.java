package com.emaralabs.emaraleague.infrastructure.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BackupManager {

    private final Object plugin;

    public BackupManager(Object plugin) {
        this.plugin = plugin;
    }

    public String createBackup() {
        return UUID.randomUUID().toString();
    }

    public List<String> listBackups() {
        return new ArrayList<>();
    }

    public boolean restoreBackup(String backupId) {
        return false;
    }

    public void deleteBackup(String backupId) {
    }
}
