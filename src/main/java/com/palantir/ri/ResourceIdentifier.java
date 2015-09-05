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

/**
 * Defines a common format for wrapping existing unique identifiers to provide additional context. This class
 * provides utility method: {@code #of(String)} to parse an existing identifier or the ability to generate new
 * identifiers by using provided context with method {@code #of(String, String, String, String)}.
 * <p>
 * Resource identifier specification:
 * <p>
 * Resource Identifiers contain 4 components, prefixed by a format identifier ri and separated with periods:
 * {@code ri.<service>.<instance>.<type>.<locator>}
 * <ol>
 * <li> Service: a string that represents the service (or application) that namespaces the rest of the identifier.
 *      Must conform with regex pattern [a-z][a-z0-9\-]*
 * <li> Instance: an optionally empty string that represents a specific service cluster, to allow disambiguation of
 *      artifacts from different service clusters. Must conform to regex pattern ([a-z0-9][a-z0-9\-]*)?
 * <li> Type: a service-specific resource type to namespace a group of locators. Must conform to regex pattern [a-z][a-z0-9\-]*
 * <li> Locator: a string used to uniquely locate the specific resource. Must conform to regex pattern [a-zA-Z0-9\-\._]+
 * </ol>
 */
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

    /**
     * Returns the application component
     *
     * @return the application component from the current identifier
     */
    public String getApplication() {
        return application;
    }

    /**
     * Returns the instance component
     *
     * @return the instance component from the current identifier
     */
    public String getInstance() {
        return instance;
    }

    /**
     * Returns the type component
     *
     * @return the type component from the current identifier
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the locator component
     *
     * @return the locator component from the current identifier
     */
    public String getLocator() {
        return locator;
    }

    /**
     * Returns a string representation of this ResourceIdentifier. The string representation
     * follows the format specification using the "ri" header followed by the 4 components
     * separated by periods.
     *
     * @return a string representation of this identifier
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(RID_CLASS).append(SEPARATOR).append(application).append(SEPARATOR)
                .append(instance).append(SEPARATOR).append(type).append(SEPARATOR).append(locator);
        return builder.toString();
    }

    /**
     * Returns the hash code value for identifier.  The hash code
     * is calculated using the Java @{code {@link Objects#hash(Object...)} method
     * over each of the 4 components.
     *
     * @return the hash code value for this identifier
     */
    @Override
    public int hashCode() {
        return Objects.hash(application, instance, type, locator);
    }

    /**
     * Compares the specified object with this identifier for equality.  Returns
     * {@code true} if and only if the specified object is also a resource identifier and
     * contain exactly the same values for all 4 components.
     *
     * @param obj the object to be compared for equality with this identifier
     * @return {@code true} if the specified object is equal to this identifier, {@code false} otherwise
     */
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

    /**
     * Checks if the input string is a valid resource identifier as defined in the specification
     *
     * @param rid the input string to be checked
     * @return {@code true} if and only if the input satisfy the resource identifier specification,
     *         {@code false} otherwise.
     */
    public static boolean isValid(String rid) {
        return rid != null && SPEC_PATTERN.matcher(rid).matches();
    }

    /**
     * Generates a new resource identifier object from the input string. This method will validate
     * that the input is a valid resource identifier string as defined by the specification prior
     * to creating the object.
     *
     * @param rid the input string to be converted to a resource identifier
     * @return a resource identifier object representing the input string
     *
     * @throws IllegalArgumentException if the input string is not a valid resource identifier
     */
    public static ResourceIdentifier of(String rid) {
        if (rid != null) {
            Matcher matcher = SPEC_PATTERN.matcher(rid);
            if (matcher.matches()) {
                return new ResourceIdentifier(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
            }
        }
        throw new IllegalArgumentException("Illegal resource identifier format: " + rid);
    }

    /**
     * Generates a new resource identifier object from each of the 4 input components. Each component must
     * satisfy the requirements as defined by the specification.
     *
     * @param application input representing the application component
     * @param instance input representing the instance component
     * @param type input representing the type component
     * @param locator input representing the locator component
     * @return a resource identifier object representing the input components
     *
     * @throws IllegalArgumentException if any of the inputs do not satisfy the resource identifier specification
     */
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
