package me.rof_loyaltyrewards.handle;

import java.util.List;

public class RewardEntry {
    private final String id;
    private final String type; // "offline" or "online"
    private final String permission;
    private final String timeString;
    private final long timeMillis;
    private final String message;
    private final String execute;
    private final List<String> commands;

    public RewardEntry(String id, String type, String permission, String timeString, long timeMillis,
                        String message, String execute, List<String> commands) {
        this.id = id;
        this.type = type;
        this.permission = permission;
        this.timeString = timeString;
        this.timeMillis = timeMillis;
        this.message = message;
        this.execute = execute;
        this.commands = commands;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getPermission() { return permission; }
    public String getTimeString() { return timeString; }
    public long getTimeMillis() { return timeMillis; }
    public String getMessage() { return message; }
    public String getExecute() { return execute; }
    public List<String> getCommands() { return commands; }
}
