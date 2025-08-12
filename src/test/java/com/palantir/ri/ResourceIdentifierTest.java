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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.UnsafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

final class ResourceIdentifierTest {

    private static List<String> goodIds() {
        return List.of(
                "ri.service.instance.folder.foo",
                "ri.service-123.north-east.folder.foo.bar",
                "ri.a1p2p3.south-west.data-set.my-hello_WORLD-123",
                "ri.my-service.instance1.graph-node.._",
                "ri.service.1instance.type.emptyname",
                "ri.my-service..graph-node.noInstance",
                "ri.my-service..graph-node.noInstance.multiple.extra.dots");
    }

    private static List<String> badIds() {
        return Collections.unmodifiableList(Arrays.asList(
                null,
                "",
                "badString",
                "rid.service.instance.type.locator",
                "ri.",
                "ri.service",
                "ri.service.",
                "ri.service.instance",
                "ri.service.instance.",
                "ri.service.instance.type.",
                "ri.#service.instance.type.locator",
                "ri.service.#instance.type.locator",
                "ri.service.instance.#type.locator",
                "ri.service.instance.type.#locator",
                "ri.service.CAPLOCK.type.name",
                "ri.service.instance.-123.name",
                "ri..instance.type.noService",
                "ri.service.instance.type.",
                "id.bad.id.class.b.name",
                "ri:service::instance:type:name",
                "ri.service.instance.type.name!@#",
                "ri.service(name)..folder.foo"));
    }

    @ParameterizedTest
    @MethodSource("goodIds")
    void testIsValidGood(String rid) {
        assertThat(ResourceIdentifier.isValid(rid)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("badIds")
    void testIsValidBad(String rid) {
        assertThat(ResourceIdentifier.isValid(rid)).isFalse();
        assertThatLoggableExceptionThrownBy(() -> ResourceIdentifier.of(rid))
                .isInstanceOf(SafeIllegalArgumentException.class)
                .hasLogMessage("Illegal resource identifier format")
                .containsArgs(UnsafeArg.of("rid", rid));
        assertThatLoggableExceptionThrownBy(() -> ResourceIdentifier.valueOf(rid))
                .isInstanceOf(SafeIllegalArgumentException.class)
                .hasLogMessage("Illegal resource identifier format")
                .containsArgs(UnsafeArg.of("rid", rid));
    }

    @Test
    void testIsValidService() {
        assertThat(ResourceIdentifier.isValidService("valid-service-123")).isTrue();
        assertThat(ResourceIdentifier.isValidType("")).isFalse();
        assertThat(ResourceIdentifier.isValidService("invalid.service!")).isFalse();
        assertThat(ResourceIdentifier.isValidService(null)).isFalse();
    }

    @Test
    void testIsValidInstance() {
        assertThat(ResourceIdentifier.isValidInstance("")).isTrue();
        assertThat(ResourceIdentifier.isValidInstance("valid-instance-123")).isTrue();
        assertThat(ResourceIdentifier.isValidInstance(".")).isFalse();
        assertThat(ResourceIdentifier.isValidInstance("..")).isFalse();
        assertThat(ResourceIdentifier.isValidInstance("in#valid")).isFalse();
        assertThat(ResourceIdentifier.isValidInstance("invalid.instance!")).isFalse();
        assertThat(ResourceIdentifier.isValidInstance(null)).isFalse();
    }

    @Test
    void testIsValidType() {
        assertThat(ResourceIdentifier.isValidType("valid-type-123")).isTrue();
        assertThat(ResourceIdentifier.isValidType("")).isFalse();
        assertThat(ResourceIdentifier.isValidType("in#valid")).isFalse();
        assertThat(ResourceIdentifier.isValidType("invalid.type!")).isFalse();
        assertThat(ResourceIdentifier.isValidType(null)).isFalse();
    }

    @Test
    void testIsValidLocator() {
        assertThat(ResourceIdentifier.isValidLocator("valid-Locator_123.")).isTrue();
        assertThat(ResourceIdentifier.isValidLocator("invalid.locator!")).isFalse();
        assertThat(ResourceIdentifier.isValidLocator(null)).isFalse();
    }

    @Test
    void testConstructionErrorMessage() {
        assertThatLoggableExceptionThrownBy(() -> ResourceIdentifier.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasLogMessage("Illegal resource identifier format")
                .containsArgs(UnsafeArg.of("rid", null));

        assertThatLoggableExceptionThrownBy(() -> ResourceIdentifier.of("ri.bad....dots"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasLogMessage("Illegal resource identifier format")
                .containsArgs(UnsafeArg.of("rid", "ri.bad....dots"));

        assertThatLoggableExceptionThrownBy(() -> ResourceIdentifier.of("123Service", "", "type", "name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasLogMessage("Illegal service format")
                .containsArgs(SafeArg.of("service", "123Service"));

        assertThatLoggableExceptionThrownBy(() -> ResourceIdentifier.of("service", "i", "type-name", "!@#$"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasLogMessage("Illegal locator format")
                .containsArgs(UnsafeArg.of("locator", "!@#$"));

        assertThatLoggableExceptionThrownBy(() -> ResourceIdentifier.of(null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasLogMessage("Illegal service format")
                .containsArgs(SafeArg.of("service", null));

        assertThatLoggableExceptionThrownBy(() -> ResourceIdentifier.of("service", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasLogMessage("Illegal instance format")
                .containsArgs(SafeArg.of("instance", null));

        assertThatLoggableExceptionThrownBy(() -> ResourceIdentifier.of("service", "", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasLogMessage("Illegal type format")
                .containsArgs(SafeArg.of("type", null));
    }

    @ParameterizedTest
    @MethodSource("goodIds")
    void testReconstruction(String rid) {
        ResourceIdentifier resourceId = ResourceIdentifier.of(rid);
        String service = resourceId.getService();
        String instance = resourceId.getInstance();
        String type = resourceId.getType();
        String locator = resourceId.getLocator();
        assertThat(ResourceIdentifier.of(service, instance, type, locator)).isEqualTo(resourceId);
    }

    @ParameterizedTest
    @MethodSource("goodIds")
    void testHas(String rid) {
        ResourceIdentifier resourceId = ResourceIdentifier.of(rid);
        assertThat(resourceId.hasService(resourceId.getService())).isTrue();
        assertThat(resourceId.hasInstance(resourceId.getInstance())).isTrue();
        assertThat(resourceId.hasType(resourceId.getType())).isTrue();
        assertThat(resourceId.hasLocator(resourceId.getLocator())).isTrue();

        assertThat(resourceId.hasService(null)).isFalse();
        assertThat(resourceId.hasInstance(null)).isFalse();
        assertThat(resourceId.hasType(null)).isFalse();
        assertThat(resourceId.hasLocator(null)).isFalse();

        assertThat(resourceId.hasService(resourceId.getService() + "a")).isFalse();
        assertThat(resourceId.hasInstance(resourceId.getInstance() + "a")).isFalse();
        assertThat(resourceId.hasType(resourceId.getType() + "a")).isFalse();
        assertThat(resourceId.hasLocator(resourceId.getLocator() + "a")).isFalse();
    }

    @Test
    void testValueOf() {
        assertThat(ResourceIdentifier.valueOf("ri.service.instance.type.name"))
                .isEqualTo(ResourceIdentifier.of("ri.service.instance.type.name"));
    }

    @Test
    void testSerialization() throws IOException {
        ObjectMapper om = new ObjectMapper();
        String ridString = "ri.service.instance.type.name";
        String ridString1 = "ri.service..type-123.aBC-name_123";
        String ridString2 = "ri.myservice.instance-1.folder.foo.bar";
        String ridString3 = "ri.myservice..data.MyDATA";
        ResourceIdentifier rid = ResourceIdentifier.of(ridString);
        ResourceIdentifier rid1 = ResourceIdentifier.of(ridString1);
        ResourceIdentifier rid2 = ResourceIdentifier.of("myservice", "instance-1", "folder", "foo.bar");
        ResourceIdentifier rid3 = ResourceIdentifier.of("myservice", "", "data", "MyDATA");
        assertThat(om.writeValueAsString(rid)).isEqualTo(toJsonString(ridString));
        assertThat(om.writeValueAsString(rid1)).isEqualTo(toJsonString(ridString1));
        assertThat(om.writeValueAsString(rid2)).isEqualTo(toJsonString(ridString2));
        assertThat(om.writeValueAsString(rid3)).isEqualTo(toJsonString(ridString3));
        assertThat(om.readValue(om.writeValueAsString(rid), ResourceIdentifier.class))
                .isEqualTo(rid);
        assertThat(om.readValue(om.writeValueAsString(rid1), ResourceIdentifier.class))
                .isEqualTo(rid1);
        assertThat(om.readValue(om.writeValueAsString(rid2), ResourceIdentifier.class))
                .isEqualTo(rid2);
        assertThat(om.readValue(om.writeValueAsString(rid3), ResourceIdentifier.class))
                .isEqualTo(rid3);
    }

    private String toJsonString(String string) {
        return "\"" + string + "\"";
    }

    @Test
    void testStringConstruction() {
        assertThat(ResourceIdentifier.of("service", "", "type", "name").toString())
                .isEqualTo("ri.service..type.name");
        assertThat(ResourceIdentifier.of("service", "instance", "type", "name").toString())
                .isEqualTo("ri.service.instance.type.name");
    }

    @Test
    void testStringConstructionWithMultipleLocatorComponents() {
        assertThat(ResourceIdentifier.of("service", "", "type", "name1")).hasToString("ri.service..type.name1");
        assertThat(ResourceIdentifier.of("service", "", "type", "name1", "name2"))
                .hasToString("ri.service..type.name1.name2");
        assertThat(ResourceIdentifier.of("service", "", "type", "name1", "name2", "name3"))
                .hasToString("ri.service..type.name1.name2.name3");
    }

    @ParameterizedTest
    @MethodSource("goodIds")
    void testEqualsHashCode(String rid) {
        ResourceIdentifier rid1 = ResourceIdentifier.of(rid);
        ResourceIdentifier rid2 = ResourceIdentifier.of(rid);
        assertThat(rid1).isEqualTo(rid2).isEqualTo(rid1);
        assertThat(rid2).isEqualTo(rid1).isEqualTo(rid2);
        assertThat(rid1.toString()).isEqualTo(rid2.toString());
        assertThat(rid1.hashCode()).isEqualTo(rid2.hashCode());

        ResourceIdentifier copy1 =
                ResourceIdentifier.of(rid1.getService(), rid1.getInstance(), rid1.getType(), rid1.getLocator());
        assertThat(rid1)
                .hasSameHashCodeAs(rid2)
                .isEqualTo(rid2)
                .isEqualTo(copy1)
                .hasSameHashCodeAs(copy1)
                .hasToString(rid1.toString())
                .hasToString(rid2.toString());
    }

    @ParameterizedTest
    @MethodSource("goodIds")
    void testNotEqualsAndDifferentHashCode(String rid) {
        ResourceIdentifier rid1 = ResourceIdentifier.of(rid);
        ResourceIdentifier copy1 =
                ResourceIdentifier.of(rid1.getService() + "1", rid1.getInstance(), rid1.getType(), rid1.getLocator());
        ResourceIdentifier copy2 =
                ResourceIdentifier.of(rid1.getService(), rid1.getInstance() + "1", rid1.getType(), rid1.getLocator());
        ResourceIdentifier copy3 =
                ResourceIdentifier.of(rid1.getService(), rid1.getInstance(), rid1.getType() + "1", rid1.getLocator());
        ResourceIdentifier copy4 =
                ResourceIdentifier.of(rid1.getService(), rid1.getInstance(), rid1.getType(), rid1.getLocator() + "1");
        assertThat(rid1)
                .isNotEqualTo(copy1)
                .isNotEqualTo(copy2)
                .isNotEqualTo(copy3)
                .isNotEqualTo(copy4)
                .doesNotHaveSameHashCodeAs(copy1)
                .doesNotHaveSameHashCodeAs(copy2)
                .doesNotHaveSameHashCodeAs(copy3)
                .doesNotHaveSameHashCodeAs(copy4)
                .doesNotHaveToString(copy1.toString())
                .doesNotHaveToString(copy2.toString())
                .doesNotHaveToString(copy3.toString())
                .doesNotHaveToString(copy4.toString())
                .isNotEqualTo(NotEqualsObj.INSTANCE);
    }

    private enum NotEqualsObj {
        INSTANCE
    }
}
