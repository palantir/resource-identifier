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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ResourceIdentifier {
    private static final String RID_CLASS = "ri";
    private static final String SEPARATOR = ".";
    private static final String SERVICE_REGEX = "([a-z][a-z0-9\\-]*)";
    private static final String INSTANCE_REGEX = "([a-z0-9][a-z0-9\\-]*)?";
    private static final String TYPE_REGEX = "([a-z][a-z0-9\\-]*)";
    private static final String LOCATOR_REGEX = "([a-zA-Z0-9_\\-\\.]+)";

    private static final Pattern SERVICE_PATTERN = Pattern.compile(SERVICE_REGEX);
    private static final Pattern INSTANCE_PATTERN = Pattern.compile(INSTANCE_REGEX);
    private static final Pattern TYPE_PATTERN = Pattern.compile(TYPE_REGEX);
    private static final Pattern LOCATOR_PATTERN = Pattern.compile(LOCATOR_REGEX);
    // creates a Pattern in form of ri.<service>.<instance>.<type>.<locator>
    private static final Pattern SPEC_PATTERN = Pattern.compile(
            RID_CLASS + "\\." + SERVICE_REGEX + "\\." + INSTANCE_REGEX + "\\." + TYPE_REGEX + "\\." + LOCATOR_REGEX);

    // fields are not final due to Jackson default constructor
    private String service;
    private String instance;
    private String type;
    private String locator;

    private ResourceIdentifier() {
        // default constructor for Jackson
    }

    private ResourceIdentifier(String service, String instance, String type, String locator) {
        this.service = service;
        this.instance = instance == null ? "" : instance;
        this.type = type;
        this.locator = locator;
    }

    public String getService() {
        return service;
    }

    public String getInstance() {
        return instance;
    }

    public String getType() {
        return type;
    }

    public String getLocator() {
        return locator;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(RID_CLASS).append(SEPARATOR)
                .append(service).append(SEPARATOR)
                .append(instance).append(SEPARATOR)
                .append(type).append(SEPARATOR)
                .append(locator);
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, instance, type, locator);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ResourceIdentifier)) {
            return false;
        }
        ResourceIdentifier other = (ResourceIdentifier) obj;
        return Objects.equals(service, other.service) &&
                Objects.equals(instance, other.instance) &&
                Objects.equals(type, other.type) &&
                Objects.equals(locator, other.locator);
    }

    public static boolean isValid(String rid) {
        return rid != null && SPEC_PATTERN.matcher(rid).matches();
    }

    public static ResourceIdentifier of(String rid) {
        if (rid != null) {
            Matcher matcher = SPEC_PATTERN.matcher(rid);
            if (matcher.matches()) {
                return new ResourceIdentifier(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
            }
        }
        throw new IllegalArgumentException("Illegal resource identifier format: " + rid);
    }

    public static ResourceIdentifier of(String service, String instance, String type, String locator) {
        checkServiceIsValid(service);
        checkInstanceIsValid(instance);
        checkTypeIsValid(type);
        checkLocatorIsValid(locator);
        return new ResourceIdentifier(service, instance, type, locator);
    }

    private static void checkServiceIsValid(String service) {
        if (service == null || !SERVICE_PATTERN.matcher(service).matches()) {
            throw new IllegalArgumentException("Illegal service format: " + service);
        }
    }

    private static void checkInstanceIsValid(String instance) {
        if (instance == null || !INSTANCE_PATTERN.matcher(instance).matches()) {
            throw new IllegalArgumentException("Illegal instance format: " + instance);
        }
    }

    private static void checkTypeIsValid(String type) {
        if (type == null || !TYPE_PATTERN.matcher(type).matches()) {
            throw new IllegalArgumentException("Illegal type format: " + type);
        }
    }

    private static void checkLocatorIsValid(String value) {
        if (value == null || !LOCATOR_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Illegal locator format: " + value);
        }
    }
}
