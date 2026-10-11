package redstonedisorder.mendingtracker;

/** The original estimate rounds the combined durability deficit up to whole XP points. */
public record MendingTotals(int damagedItems, long durabilityNeeded) {
    public static final MendingTotals EMPTY = new MendingTotals(0, 0);

    public MendingTotals add(int damage, boolean hasMending) {
        return damage > 0 && hasMending
                ? new MendingTotals(damagedItems + 1, durabilityNeeded + damage) : this;
    }

    public long xpRequired() { return durabilityNeeded / 2 + durabilityNeeded % 2; }
}