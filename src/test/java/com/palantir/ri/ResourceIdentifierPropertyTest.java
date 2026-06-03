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

import static com.palantir.logsafe.testing.Assertions.assertThatLoggableExceptionThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.lists;

import com.palantir.logsafe.UnsafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.quicktheories.core.Gen;
import org.quicktheories.generators.Generate;

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

    private static final String VALID_SERVICE_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789-";
    private static final String VALID_INSTANCE_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789-";
    private static final String VALID_TYPE_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789-";
    private static final String VALID_LOCATOR_CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-.";

    @Test
    void testIsValid() {
        qt().withExamples(1_000_000)
                .forAll(
                        stringGen(VALID_SERVICE_CHARS),
                        stringGen(VALID_INSTANCE_CHARS),
                        stringGen(VALID_TYPE_CHARS),
                        stringGen(VALID_LOCATOR_CHARS))
                .checkAssert((service, instance, type, locator) -> {
                    String string = "ri." + service + "." + instance + "." + type + "." + locator;

                    boolean isValidRid = SPEC_PATTERN.matcher(string).matches();
                    assertThat(ResourceIdentifier.isValid(string)).as(string).isEqualTo(isValidRid);
                    assertThat(ResourceIdentifier.isValidService(service))
                            .as(service)
                            .isEqualTo(SERVICE_PATTERN.matcher(service).matches());
                    assertThat(ResourceIdentifier.isValidInstance(instance))
                            .as(instance)
                            .isEqualTo(INSTANCE_PATTERN.matcher(instance).matches());
                    assertThat(ResourceIdentifier.isValidType(type))
                            .as(type)
                            .isEqualTo(TYPE_PATTERN.matcher(type).matches());
                    assertThat(ResourceIdentifier.isValidLocator(locator))
                            .as(locator)
                            .isEqualTo(LOCATOR_PATTERN.matcher(locator).matches());

                    if (isValidRid) {
                        assertThat(ResourceIdentifier.of(string))
                                .isNotNull()
                                .isEqualTo(ResourceIdentifier.valueOf(string))
                                .satisfies(rid -> {
                                    assertThat(rid.getService()).isEqualTo(service);
                                    assertThat(rid.hasService(service)).isTrue();
                                    assertThat(rid.getInstance()).isEqualTo(instance);
                                    assertThat(rid.hasInstance(instance)).isTrue();
                                    assertThat(rid.getType()).isEqualTo(type);
                                    assertThat(rid.hasType(type)).isTrue();
                                    assertThat(rid.getLocator()).isEqualTo(locator);
                                    assertThat(rid.hasLocator(locator)).isTrue();
                                });
                    } else {
                        assertThatLoggableExceptionThrownBy(() -> ResourceIdentifier.of(string))
                                .isInstanceOf(SafeIllegalArgumentException.class)
                                .hasLogMessage("Illegal resource identifier format")
                                .containsArgs(UnsafeArg.of("rid", string));
                    }
                });
    }

    private static Gen<String> stringGen(String chars) {
        Gen<Character> characterGen =
                Generate.pick(chars.chars().mapToObj(c -> (char) c).toList());
        return lists().of(characterGen).ofSizeBetween(0, 20).map(ch -> {
            StringBuilder sb = new StringBuilder(ch.size());
            ch.forEach(sb::append);
            return sb.toString();
        });
    }
}
