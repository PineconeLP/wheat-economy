package io.github.pineconelp.wheateconomy.bank;


public class LedgerEntry {
    private String id;
    private String type;
    private int balanceChange;
    private int balanceAfter;
    private String targetPlayerId;
    private long createdAt;

    public LedgerEntry(String id, String type, int balanceChange, int balanceAfter, String targetPlayerId, long createdAt) {
        this.id = id;
        this.type = type;
        this.balanceChange = balanceChange;
        this.balanceAfter = balanceAfter;
        this.targetPlayerId = targetPlayerId;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public int getBalanceChange() { return balanceChange; }
    public int getBalanceAfter() { return balanceAfter; }
    public String getTargetPlayerId() { return targetPlayerId; }
    public long getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %+d (Balance: %d)",
                new java.util.Date(createdAt), type, balanceChange, balanceAfter);
    }
}