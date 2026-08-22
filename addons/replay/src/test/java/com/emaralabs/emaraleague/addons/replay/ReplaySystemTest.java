package com.emaralabs.emaraleague.addons.replay;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReplaySystemTest {

    @Test
    void testReplaySystemCreation() {
        ReplaySystem replay = new ReplaySystem();
        assertNotNull(replay);
    }

    @Test
    void testStartRecording() {
        ReplaySystem replay = new ReplaySystem();
        String recordingId = replay.startRecording("match-123");
        assertNotNull(recordingId);
    }

    @Test
    void testStopRecording() {
        ReplaySystem replay = new ReplaySystem();
        String recordingId = replay.startRecording("match-123");
        assertTrue(replay.stopRecording(recordingId));
    }
}
