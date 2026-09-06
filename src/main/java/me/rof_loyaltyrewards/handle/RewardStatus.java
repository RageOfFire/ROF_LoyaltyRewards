package me.rof_loyaltyrewards.handle;

public class RewardStatus {
    public enum State {
        /** Player doesn't have the required permission. */
        NO_PERMISSION,
        /** Time requirement not yet reached. */
        LOCKED,
        /** Time requirement reached, not yet claimed - actionable in the GUI (offline only). */
        CLAIMABLE,
        /** Already claimed and its cooldown (its own time requirement) hasn't elapsed again yet. */
        COOLDOWN
    }

    private final RewardEntry entry;
    private final State state;
    private final long millisRemaining;

    public RewardStatus(RewardEntry entry, State state, long millisRemaining) {
        this.entry = entry;
        this.state = state;
        this.millisRemaining = millisRemaining;
    }

    public RewardEntry getEntry() { return entry; }
    public State getState() { return state; }
    /** Time left until LOCKED becomes CLAIMABLE, or until COOLDOWN clears. 0 for CLAIMABLE/NO_PERMISSION. */
    public long getMillisRemaining() { return millisRemaining; }
}
