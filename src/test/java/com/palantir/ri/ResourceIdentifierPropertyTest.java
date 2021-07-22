/*
 * (c) Copyright 2015 Palantir Technologies Inc. All rights reserved.
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.CharRange;
import net.jqwik.api.constraints.Chars;

final class ResourceIdentifierPropertyTest {

    private static final String SERVICE_REGEX = "([a-z][a-z0-9\\-]*)";
    private static final String INSTANCE_REGEX = "([a-z0-9][a-z0-9\\-]*)?";
    private static final String TYPE_REGEX = "([a-z][a-z0-9\\-]*)";
    private static final String LOCATOR_REGEX = "([a-zA-Z0-9_\\-\\.]+)";

    private static final Pattern SERVICE_PATTERN = Pattern.compile(SERVICE_REGEX);
    private static final Pattern INSTANCE_PATTERN = Pattern.compile(INSTANCE_REGEX);
    private static final Pattern TYPE_PATTERN = Pattern.compile(TYPE_REGEX);
    private static final Pattern LOCATOR_PATTERN = Pattern.compile(LOCATOR_REGEX);
    private static final Pattern SPEC_PATTERN = Pattern.compile(
            "ri\\." + SERVICE_REGEX + "\\." + INSTANCE_REGEX + "\\." + TYPE_REGEX + "\\." + LOCATOR_REGEX);

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @CharRange(from = 'a', to = 'z')
    @CharRange(from = '0', to = '9')
    @Chars('-')
    @interface Service {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @CharRange(from = 'a', to = 'z')
    @CharRange(from = '0', to = '9')
    @Chars('-')
    @interface Instance {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @CharRange(from = 'a', to = 'z')
    @CharRange(from = '0', to = '9')
    @Chars('-')
    @interface Type {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @CharRange(from = 'a', to = 'z')
    @CharRange(from = 'A', to = 'Z')
    @CharRange(from = '0', to = '9')
    @Chars({'_', '-', '.'})
    @interface Locator {}

    @Property(tries = 5000)
    void testIsValid(
            @ForAll @Service String service,
            @ForAll @Instance String instance,
            @ForAll @Type String type,
            @ForAll @Locator String locator) {
        String string = "ri." + service + "." + instance + "." + type + "." + locator;
        assertEquals(SPEC_PATTERN.matcher(string).matches(), ResourceIdentifier.isValid(string), string);
        assertEquals(SERVICE_PATTERN.matcher(service).matches(), ResourceIdentifier.isValidService(service), service);
        assertEquals(
                INSTANCE_PATTERN.matcher(instance).matches(), ResourceIdentifier.isValidInstance(instance), instance);
        assertEquals(TYPE_PATTERN.matcher(type).matches(), ResourceIdentifier.isValidType(type), type);
        assertEquals(LOCATOR_PATTERN.matcher(locator).matches(), ResourceIdentifier.isValidLocator(locator), locator);
    }
}
