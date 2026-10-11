package redstonedisorder.mendingtracker;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MendingTotalsTest {
    @Test void excludesUndamagedAndNonMendingEquipment() {
        assertEquals(MendingTotals.EMPTY, MendingTotals.EMPTY.add(0, true).add(10, false).add(-1, true));
    }

    @Test void combinesSixSlotsAndRoundsTheCombinedDeficit() {
        MendingTotals totals = MendingTotals.EMPTY;
        for (int damage : new int[] {1, 2, 3, 4, 5, 6}) totals = totals.add(damage, true);
        assertEquals(6, totals.damagedItems());
        assertEquals(21, totals.durabilityNeeded());
        assertEquals(11, totals.xpRequired());
        assertEquals(1, MendingTotals.EMPTY.add(1, true).add(1, true).xpRequired());
    }

    @Test void emptyAndEvenDeficitsHaveExactEstimates() {
        assertEquals(0, MendingTotals.EMPTY.xpRequired());
        assertEquals(2, MendingTotals.EMPTY.add(4, true).xpRequired());
    }

    @Test void largeDurabilityValuesDoNotOverflow() {
        MendingTotals totals = MendingTotals.EMPTY.add(Integer.MAX_VALUE, true).add(Integer.MAX_VALUE, true);
        assertEquals(4294967294L, totals.durabilityNeeded());
        assertEquals(2147483647L, totals.xpRequired());
    }
}