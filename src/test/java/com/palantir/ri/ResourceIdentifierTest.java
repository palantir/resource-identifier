// Copyright 2015 Palantir Technologies
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.palantir.ri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class ResourceIdentifierTest {
    private static List<String> goodIds;
    private static List<String> badIds;

    @BeforeClass
    public static void setUp() {
        goodIds = new ArrayList<>();
        goodIds.add("ri.app.instance.folder.foo");
        goodIds.add("ri.app-123.north-east.folder.foo.bar");
        goodIds.add("ri.a1p2p3.south-west.data-set.hello_WORLD-123");
        goodIds.add("ri.my-app.instance1.graph-node._");
        goodIds.add("ri.my-app..graph-node.noInstance");
        goodIds.add("ri.my-app..graph-node.noInstance.extra.dots");

        badIds = new ArrayList<>();
        badIds.add("");
        badIds.add("badString");
        badIds.add("ri.123.instance.type.name");
        badIds.add("ri.app.CAPLOCK.type.name");
        badIds.add("ri.app.instance.-123.name");
        badIds.add("ri..instance.type.noApp");
        badIds.add("id.bad.id.class.name");
        badIds.add("ri.app.1instance.type.name");
        badIds.add("ri:app::instance:type:name");
        badIds.add("ri.app.instance.noLocator.");
        badIds.add("ri.app.instance.type.name!@#");
        badIds.add("ri.app(name)..folder.foo");
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
            assertFalse(ResourceIdentifier.isValid(rid));
        }
        assertFalse(ResourceIdentifier.isValid(null));
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
            ResourceIdentifier.of("123App", "", "type", "name");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal application field format: 123App", e.getMessage());
        }
        try {
            ResourceIdentifier.of("app", "Instance", "type", "name");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal instance field format: Instance", e.getMessage());
        }
        try {
            ResourceIdentifier.of("app", "i", "type-name", "!@#$");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal locator format: !@#$", e.getMessage());
        }
        try {
            ResourceIdentifier.of(null, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal application field format: null", e.getMessage());
        }
        try {
            ResourceIdentifier.of("app", null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal instance field format: null", e.getMessage());
        }
        try {
            ResourceIdentifier.of("app", "", null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal type field format: null", e.getMessage());
        }
    }

    @Test
    public void testReconstruction() {
        for (String rid : goodIds) {
            ResourceIdentifier resourceId = ResourceIdentifier.of(rid);
            String app = resourceId.getApplication();
            String instance = resourceId.getInstance();
            String type = resourceId.getType();
            String oid = resourceId.getLocator();
            assertEquals(resourceId, ResourceIdentifier.of(app, instance, type, oid));
        }
    }

    @Test
    public void testSerialization() throws IOException {
        ObjectMapper om = new ObjectMapper();
        ResourceIdentifier rid = ResourceIdentifier.of("ri.app.instance.type.name");
        ResourceIdentifier rid1 = ResourceIdentifier.of("ri.app..type-123.aBC-name_123");
        ResourceIdentifier rid2 = ResourceIdentifier.of("myapp", "instance-1", "folder", "foo.bar");
        ResourceIdentifier rid3 = ResourceIdentifier.of("myapp", "", "data", "MyDATA");
        String serializedString = om.writeValueAsString(rid);
        String serializedString1 = om.writeValueAsString(rid1);
        String serializedString2 = om.writeValueAsString(rid2);
        String serializedString3 = om.writeValueAsString(rid3);
        ResourceIdentifier value = om.readValue(serializedString, ResourceIdentifier.class);
        ResourceIdentifier value1 = om.readValue(serializedString1, ResourceIdentifier.class);
        ResourceIdentifier value2 = om.readValue(serializedString2, ResourceIdentifier.class);
        ResourceIdentifier value3 = om.readValue(serializedString3, ResourceIdentifier.class);
        assertEquals(rid, value);
        assertEquals(rid1, value1);
        assertEquals(rid2, value2);
        assertEquals(rid3, value3);
    }

    @Test
    public void testEqualsHashCode() {
        ResourceIdentifier prevRid = null;
        for (int i = 0; i < goodIds.size(); ++i) {
            ResourceIdentifier curRid = ResourceIdentifier.of(goodIds.get(i));
            ResourceIdentifier curRid2 = ResourceIdentifier.of(goodIds.get(i));
            assertEquals(curRid, curRid);
            assertEquals(curRid, curRid2);
            assertEquals(curRid.hashCode(), curRid2.hashCode());
            if (prevRid != null) {
                Assert.assertNotEquals(prevRid, curRid);
                Assert.assertNotEquals(prevRid.hashCode(), curRid.hashCode());
            }
            prevRid = curRid;
        }
    }
}
