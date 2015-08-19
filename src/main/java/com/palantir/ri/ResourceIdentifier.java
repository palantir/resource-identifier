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

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.StringUtils;

public final class ResourceIdentifier {
    private static final String RID_CLASS = "ri";
    private static final String RID32_CLASS = "ri32";
    private static final String SEPARATOR = ".";
    private static final String FIELD_REGEX = "([a-z][a-z0-9-]*)";
    private static final String STRICT_LOCATOR_REGEX = "([a-zA-Z0-9\\_\\-\\.]+)";
    private static final String BASE32_REGEX = "([A-Z2-7]+)";

    private static final Pattern FIELD_PATTERN = Pattern.compile(FIELD_REGEX);
    private static final Pattern STRICT_LOCATOR_PATTERN = Pattern.compile(STRICT_LOCATOR_REGEX);
    // creates a Pattern in form of <rid class>.<application>.<instance>.<type>.<locator>
    private static final Pattern RID_SPEC_PATTERN = Pattern.compile(
            RID_CLASS + "\\." + FIELD_REGEX + "\\." + FIELD_REGEX + "?\\." + FIELD_REGEX + "\\." + STRICT_LOCATOR_REGEX);
    private static final Pattern RID32_SPEC_PATTERN = Pattern.compile(
            RID32_CLASS + "\\." + FIELD_REGEX + "\\." + FIELD_REGEX + "?\\." + FIELD_REGEX + "\\." + BASE32_REGEX);

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
        boolean isStrict = STRICT_LOCATOR_PATTERN.matcher(locator).matches();

        StringBuilder builder = new StringBuilder();

        if (isStrict) {
            builder.append(RID_CLASS);
        } else {
            builder.append(RID32_CLASS);
        }
        builder.append(SEPARATOR)
               .append(application)
               .append(SEPARATOR)
               .append(instance)
               .append(SEPARATOR)
               .append(type)
               .append(SEPARATOR);

        if (isStrict) {
            builder.append(locator);
        } else {
            builder.append(new Base32().encodeAsString(locator.getBytes(Charset.forName("UTF-8"))).replace("=", ""));
        }

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
        return rid != null && (RID_SPEC_PATTERN.matcher(rid).matches() || RID32_SPEC_PATTERN.matcher(rid).matches());
    }

    public static ResourceIdentifier of(String rid) {
        if (rid != null) {
            Matcher matcher = RID_SPEC_PATTERN.matcher(rid);
            if (matcher.matches()) {
                return new ResourceIdentifier(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
            }
            matcher = RID32_SPEC_PATTERN.matcher(rid);
            if (matcher.matches()) {
                return new ResourceIdentifier(matcher.group(1), matcher.group(2), matcher.group(3),
                        StringUtils.newStringUtf8(new Base32().decode(matcher.group(4))));
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
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Illegal locator format: " + value);
        }
    }
}
