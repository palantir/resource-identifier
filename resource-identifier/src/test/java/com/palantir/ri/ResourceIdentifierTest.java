/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.ri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public final class ResourceIdentifierTest {
    private static List<String> goodIds;
    private static List<String> badIds;

    @BeforeClass
    public static void setUp() {
        goodIds = new ArrayList<>();
        goodIds.add("ri.service.instance.folder.foo");
        goodIds.add("ri.service-123.north-east.folder.foo.bar");
        goodIds.add("ri.a1p2p3.south-west.data-set.my-hello_WORLD-123");
        goodIds.add("ri.my-service.instance1.graph-node.._");
        goodIds.add("ri.service.1instance.type.emptyname");
        goodIds.add("ri.my-service..graph-node.noInstance");
        goodIds.add("ri.my-service..graph-node.noInstance.multiple.extra.dots");

        badIds = new ArrayList<>();
        badIds.add("");
        badIds.add("badString");
        badIds.add("ri.service.CAPLOCK.type.name");
        badIds.add("ri.service.instance.-123.name");
        badIds.add("ri..instance.type.noService");
        badIds.add("ri.service.instance.type.");
        badIds.add("id.bad.id.class.b.name");
        badIds.add("ri:service::instance:type:name");
        badIds.add("ri.service.instance.type.name!@#");
        badIds.add("ri.service(name)..folder.foo");
    }

    @Test
    public void testIsValidGood() {
        for (String rid : goodIds) {
            assertTrue(ResourceIdentifier.isValid(rid));
        }
    }

    @Test
    public void testIsValidBad() {
        for (String rid : badIds) {
            assertFalse("bad rid " + rid, ResourceIdentifier.isValid(rid));
        }
        assertFalse(ResourceIdentifier.isValid(null));
    }

    @Test
    public void testIsValidService() {
        assertTrue(ResourceIdentifier.isValidService("valid-service-123"));
        assertFalse(ResourceIdentifier.isValidService("invalid.service!"));
    }

    @Test
    public void testIsValidInstance() {
        assertTrue(ResourceIdentifier.isValidInstance("valid-instance-123"));
        assertFalse(ResourceIdentifier.isValidInstance("invalid.instance!"));
    }

    @Test
    public void testIsValidType() {
        assertTrue(ResourceIdentifier.isValidType("valid-type-123"));
        assertFalse(ResourceIdentifier.isValidType("invalid.type!"));
    }

    @Test
    public void testIsValidLocator() {
        assertTrue(ResourceIdentifier.isValidLocator("valid-Locator_123."));
        assertFalse(ResourceIdentifier.isValidLocator("invalid.locator!"));
    }

    @Test
    public void testConstructionErrorMessage() {
        try {
            ResourceIdentifier.of("ri.bad....dots");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal resource identifier format: ri.bad....dots", e.getMessage());
        }
        try {
            ResourceIdentifier.of("123Service", "", "type", "name");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal service format: 123Service", e.getMessage());
        }
        try {
            ResourceIdentifier.of("service", "Instance", "type", "name");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal instance format: Instance", e.getMessage());
        }
        try {
            ResourceIdentifier.of("service", "i", "type-name", "!@#$");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal locator format: !@#$", e.getMessage());
        }
        try {
            ResourceIdentifier.of(null, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal service format: null", e.getMessage());
        }
        try {
            ResourceIdentifier.of("service", null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal instance format: null", e.getMessage());
        }
        try {
            ResourceIdentifier.of("service", "", null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal type format: null", e.getMessage());
        }
    }

    @Test
    public void testReconstruction() {
        for (String rid : goodIds) {
            ResourceIdentifier resourceId = ResourceIdentifier.of(rid);
            String service = resourceId.getService();
            String instance = resourceId.getInstance();
            String type = resourceId.getType();
            String oid = resourceId.getLocator();
            assertEquals(resourceId, ResourceIdentifier.of(service, instance, type, oid));
        }
    }

    @Test
    public void testValueOf() {
        ResourceIdentifier of = ResourceIdentifier.of("ri.service.instance.type.name");
        ResourceIdentifier valueOf = ResourceIdentifier.valueOf("ri.service.instance.type.name");
        assertEquals(of, valueOf);
    }

    @Test
    public void testSerialization() throws IOException {
        ObjectMapper om = new ObjectMapper();
        String ridString = "ri.service.instance.type.name";
        String ridString1 = "ri.service..type-123.aBC-name_123";
        String ridString2 = "ri.myservice.instance-1.folder.foo.bar";
        String ridString3 = "ri.myservice..data.MyDATA";
        ResourceIdentifier rid = ResourceIdentifier.of(ridString);
        ResourceIdentifier rid1 = ResourceIdentifier.of(ridString1);
        ResourceIdentifier rid2 = ResourceIdentifier.of("myservice", "instance-1", "folder", "foo.bar");
        ResourceIdentifier rid3 = ResourceIdentifier.of("myservice", "", "data", "MyDATA");
        String serializedString = om.writeValueAsString(rid);
        String serializedString1 = om.writeValueAsString(rid1);
        String serializedString2 = om.writeValueAsString(rid2);
        String serializedString3 = om.writeValueAsString(rid3);
        ResourceIdentifier value = om.readValue(serializedString, ResourceIdentifier.class);
        ResourceIdentifier value1 = om.readValue(serializedString1, ResourceIdentifier.class);
        ResourceIdentifier value2 = om.readValue(serializedString2, ResourceIdentifier.class);
        ResourceIdentifier value3 = om.readValue(serializedString3, ResourceIdentifier.class);
        assertEquals(toJsonString(ridString), serializedString);
        assertEquals(toJsonString(ridString1), serializedString1);
        assertEquals(toJsonString(ridString2), serializedString2);
        assertEquals(toJsonString(ridString3), serializedString3);
        assertEquals(rid, value);
        assertEquals(rid1, value1);
        assertEquals(rid2, value2);
        assertEquals(rid3, value3);
    }

    private String toJsonString(String string) {
        return String.format("\"%s\"", string);
    }

    @Test
    public void testStringConstruction() {
        assertEquals("ri.service..type.name",
                ResourceIdentifier.of("service", "", "type", "name").toString());
        assertEquals("ri.service.instance.type.name",
                ResourceIdentifier.of("service", "instance", "type", "name").toString());
    }

    @Test
    public void testStringConstructionWithMultipleLocatorComponents() {
        assertEquals("ri.service..type.name1",
                ResourceIdentifier.of("service", "", "type", "name1", new String[0]).toString());
        assertEquals("ri.service..type.name1.name2",
                ResourceIdentifier.of("service", "", "type", "name1",
                        new String[] {"name2"}).toString());
        assertEquals("ri.service..type.name1.name2.name3",
                ResourceIdentifier.of("service", "", "type", "name1",
                        new String[] {"name2", "name3"}).toString());
    }

    @Test
    public void testEqualsHashCode() {
        ResourceIdentifier prevRid = null;
        for (int i = 0; i < goodIds.size(); ++i) {
            ResourceIdentifier curRid = ResourceIdentifier.of(goodIds.get(i));
            ResourceIdentifier curRid2 = ResourceIdentifier.of(goodIds.get(i));
            assertEquals(curRid, curRid);
            assertEquals(curRid, curRid2);
            assertEquals(curRid.toString(), curRid2.toString());
            assertEquals(curRid.hashCode(), curRid2.hashCode());
            assertNotEquals(curRid, NotEqualsObj.INSTANCE);
            if (prevRid != null) {
                assertNotEquals(prevRid, curRid);
                assertNotEquals(prevRid.hashCode(), curRid.hashCode());
            }
            prevRid = curRid;
        }
    }

    private enum NotEqualsObj {
        INSTANCE
    }
}
