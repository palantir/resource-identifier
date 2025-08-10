/*
 * (c) Copyright 2025 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.ri;

final class FastAsciiPredicate implements CharPredicate {
    private static final int TABLE_SIZE = 256;

    private final boolean[] truthTable;

    private FastAsciiPredicate(boolean[] truthTable) {
        this.truthTable = truthTable;
    }

    static CharPredicate compile(CharPredicate predicate) {
        boolean[] mask = new boolean[TABLE_SIZE];
        for (char ch = 0; ch < TABLE_SIZE; ch++) {
            mask[ch] = predicate.test(ch);
        }
        return new FastAsciiPredicate(mask);
    }

    @Override
    public boolean test(char ch) {
        return ch < TABLE_SIZE && truthTable[ch];
    }
}
