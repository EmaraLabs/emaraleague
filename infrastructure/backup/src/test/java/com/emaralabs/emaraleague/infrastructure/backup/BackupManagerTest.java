package com.emaralabs.emaraleague.infrastructure.backup;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BackupManagerTest {

    @Test
    void testBackupManagerCreation() {
        BackupManager backup = new BackupManager(null);
        assertNotNull(backup);
    }

    @Test
    void testCreateBackup() {
        BackupManager backup = new BackupManager(null);
        String backupId = backup.createBackup();
        assertNotNull(backupId);
        assertFalse(backupId.isEmpty());
    }

    @Test
    void testListBackups() {
        BackupManager backup = new BackupManager(null);
        assertNotNull(backup.listBackups());
    }
}
