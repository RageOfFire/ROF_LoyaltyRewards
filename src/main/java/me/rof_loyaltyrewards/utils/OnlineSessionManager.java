package me.rof_loyaltyrewards.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks when each currently-online player's session started (real wall-clock
 * time), so online reward eligibility is based on actual elapsed time rather
 * than the PLAY_ONE_MINUTE statistic - which is tick-based and can drift from
 * real time if the server is lagging or ticks are skipped.
 */
public class OnlineSessionManager {
    private final Map<UUID, Long> sessionStart = new ConcurrentHashMap<>();

    public void startSession(UUID uuid) {
        sessionStart.put(uuid, System.currentTimeMillis());
    }

    public void endSession(UUID uuid) {
        sessionStart.remove(uuid);
    }

    /** Milliseconds elapsed in the player's current session. 0 if no session is tracked. */
    public long getSessionMillis(UUID uuid) {
        Long start = sessionStart.get(uuid);
        if (start == null) return 0L;
        return System.currentTimeMillis() - start;
    }

    public boolean hasSession(UUID uuid) {
        return sessionStart.containsKey(uuid);
    }
}
