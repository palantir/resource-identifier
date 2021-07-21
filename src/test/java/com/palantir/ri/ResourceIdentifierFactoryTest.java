/*
 * (c) Copyright 2021 Palantir Technologies Inc. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.palantir.ri.ResourceIdentifier.Factory;
import org.junit.jupiter.api.Test;

public class ResourceIdentifierFactoryTest {
    @Test
    void testConstructionErrorMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ResourceIdentifier.factory()
                .service("*"));
        assertEquals(ex.getMessage(), "Illegal service format: *");
        ex = assertThrows(IllegalArgumentException.class, () -> ResourceIdentifier.factory()
                .instance("*"));
        assertEquals(ex.getMessage(), "Illegal instance format: *");
        ex = assertThrows(IllegalArgumentException.class, () -> ResourceIdentifier.factory()
                .type("*"));
        assertEquals(ex.getMessage(), "Illegal type format: *");
        ex = assertThrows(IllegalArgumentException.class, () -> ResourceIdentifier.factory()
                .create("*"));
        assertEquals(ex.getMessage(), "Illegal locator format: *");
    }

    @Test
    void testMissingType() {
        assertThrows(
                NullPointerException.class,
                () -> ResourceIdentifier.factory()
                        .service("service")
                        .instance("instance")
                        .create("locator"),
                "Missing type");
    }

    @Test
    void testValidRids() {
        Factory factory =
                ResourceIdentifier.factory().service("s").instance("i").type("t");
        assertEquals(factory.create("l1"), ResourceIdentifier.of("ri.s.i.t.l1"));
        assertEquals(factory.create("l2"), ResourceIdentifier.of("ri.s.i.t.l2"));
    }
}
