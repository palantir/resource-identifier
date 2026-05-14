/*
 * (c) Copyright 2025 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.ri;

final class AsciiPredicate {
    private static final int TABLE_SIZE = 256;

    private final boolean[] truthTable;

    private AsciiPredicate(boolean[] truthTable) {
        this.truthTable = truthTable;
    }

    interface CharPredicate {
        boolean test(char ch);
    }

    static AsciiPredicate compile(CharPredicate predicate) {
        boolean[] truthTable = new boolean[TABLE_SIZE];
        for (char ch = 0; ch < TABLE_SIZE; ch++) {
            truthTable[ch] = predicate.test(ch);
        }
        return new AsciiPredicate(truthTable);
    }

    boolean test(char ch) {
        return ch < TABLE_SIZE && truthTable[ch];
    }
}
