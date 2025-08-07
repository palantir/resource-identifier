/*
 * (c) Copyright 2025 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.ri;

final class FastAsciiPredicate implements CharPredicate {
    private final boolean[] mask;

    private FastAsciiPredicate(boolean[] mask) {
        this.mask = mask;
    }

    static CharPredicate compile(CharPredicate predicate) {
        boolean[] mask = new boolean[256];
        for (char ch = 0; ch < 256; ch++) {
            mask[ch] = predicate.test(ch);
        }
        return new FastAsciiPredicate(mask);
    }

    @Override
    public boolean test(char ch) {
        return ch < 256 && mask[ch];
    }
}
