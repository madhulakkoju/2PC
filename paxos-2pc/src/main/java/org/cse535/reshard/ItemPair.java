package org.cse535.reshard;

import java.util.Objects;

public class ItemPair {
    int item1, item2;

    public ItemPair(int item1, int item2) {
        // Ensure consistent ordering for pairs
        this.item1 = Math.min(item1, item2);
        this.item2 = Math.max(item1, item2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPair pair = (ItemPair) o;
        return item1 == pair.item1 && item2 == pair.item2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item1, item2);
    }

    @Override
    public String toString() {
        return "(" + item1 + ", " + item2 + ")";
    }
}