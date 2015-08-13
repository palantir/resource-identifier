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
    private static final String FIELD_REGEX = "([a-z][a-z0-9-]*)";
    private static final String LOCATOR_REGEX = "([a-zA-Z0-9\\_\\-\\.]+)";

    private static final Pattern FIELD_PATTERN = Pattern.compile(FIELD_REGEX);
    private static final Pattern LOCATOR_PATTERN = Pattern.compile(LOCATOR_REGEX);
    // creates a Pattern in form of <rid class>.<application>.<instance>.<type>.<locator>
    private static final Pattern SPEC_PATTERN = Pattern.compile(
            RID_CLASS + "\\." + FIELD_REGEX + "\\." + FIELD_REGEX + "?\\." + FIELD_REGEX + "\\." + LOCATOR_REGEX);

    // fields are not final due to Jackson default constructor
    private String application;
    private String instance;
    private String type;
    private String locator;

    private ResourceIdentifier() {
        // default constructor for Jackson
    }

    private ResourceIdentifier(String application, String instance, String type, String locator) {
        this.application = application;
        this.instance = instance == null ? "" : instance;
        this.type = type;
        this.locator = locator;
    }

    public String getApplication() {
        return application;
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
        StringBuilder builder = new StringBuilder(RID_CLASS).append(SEPARATOR).append(application).append(SEPARATOR)
                .append(instance).append(SEPARATOR).append(type).append(SEPARATOR).append(locator);
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(application, instance, type, locator);
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
        return Objects.equals(application, other.application) &&
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

    public static ResourceIdentifier of(String application, String instance, String type, String locator) {
        checkFieldIsValid("application", application);
        checkFieldIsValid("instance", instance, true);
        checkFieldIsValid("type", type);
        checkLocatorIsValid(locator);

        return new ResourceIdentifier(application, instance, type, locator);
    }

    private static void checkFieldIsValid(String fieldName, String value) {
        checkFieldIsValid(fieldName, value, false);
    }

    private static void checkFieldIsValid(String fieldName, String value, boolean allowEmpty) {
        if (allowEmpty && "".equals(value)) {
            return;
        }
        if (value == null || !FIELD_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Illegal " + fieldName + " field format: " + value);
        }
    }

    private static void checkLocatorIsValid(String value) {
        if (value == null || !LOCATOR_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Illegal locator format: " + value);
        }
    }
}
