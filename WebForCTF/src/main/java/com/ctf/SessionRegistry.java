package com.ctf;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private final Map<String, Boolean> completedTest = new ConcurrentHashMap<>();

    public void registerSession(String sessionId, String username) {
        sessions.put(sessionId, username);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        completedTest.remove(usernameBySession(sessionId));
    }

    public Map<String, String> getActiveSessions() {
        return sessions;
    }

    public boolean isUserActive(String username) {
        return sessions.containsValue(username);
    }

    public void markTestCompleted(String username) {
        completedTest.put(username, true);
    }

    public boolean hasCompletedTest(String username) {
        return completedTest.getOrDefault(username, false);
    }

    public String usernameBySession(String sessionId) {
        return sessions.get(sessionId);
    }
}
