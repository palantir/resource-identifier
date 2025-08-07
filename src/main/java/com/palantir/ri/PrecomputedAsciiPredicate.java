/*
 * (c) Copyright 2025 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.ri;

import java.util.function.Predicate;

final class PrecomputedAsciiPredicate implements Predicate<Character> {
    private final boolean[] mask;

    private PrecomputedAsciiPredicate(boolean[] mask) {
        this.mask = mask;
    }

    static Predicate<Character> precompute(Predicate<Character> predicate) {
        boolean[] mask = new boolean[256];
        for (char ch = 0; ch < 256; ch++) {
            mask[ch] = predicate.test(ch);
        }
        return new PrecomputedAsciiPredicate(mask);
    }

    @Override
    public boolean test(Character ch) {
        return ch < 256 && mask[ch];
    }
}
