package com.emaralabs.emaraleague.addons.replay;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReplaySystem {

    private final Map<String, Long> activeRecordings = new HashMap<>();

    public String startRecording(String matchId) {
        String recordingId = UUID.randomUUID().toString();
        activeRecordings.put(recordingId, System.currentTimeMillis());
        return recordingId;
    }

    public boolean stopRecording(String recordingId) {
        return activeRecordings.remove(recordingId) != null;
    }

    public boolean isRecording(String recordingId) {
        return activeRecordings.containsKey(recordingId);
    }

    public int getActiveRecordingCount() {
        return activeRecordings.size();
    }
}
