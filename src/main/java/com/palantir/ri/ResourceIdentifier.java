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
 * {@code ri.<service>.<instance>.<type>.<context>.<locator>}
 * <ol>
 * <li> Service: a string that represents the service (or application) that namespaces the rest of the identifier.
 *      Must conform with regex pattern [a-z][a-z0-9\-]*
 * <li> Instance: an optionally empty string that represents a specific service cluster, to allow disambiguation of
 *      artifacts from different service clusters. Must conform to regex pattern ([a-z0-9][a-z0-9\-]*)?
 * <li> Type: a service-specific resource type to namespace a group of locators. Must conform to regex pattern [a-z][a-z0-9\-]*
 * <li> Context: a service-specific context. Must conform to regex pattern [a-zA-Z0-9_\-]*
 * <li> Locator: a string used to uniquely locate the specific resource. Must conform to regex pattern [a-zA-Z0-9\-\._]+
 * </ol>
 */
public final class ResourceIdentifier {
    private static final String RID_CLASS = "ri";
    private static final String SEPARATOR = ".";
    private static final String SERVICE_REGEX = "([a-z][a-z0-9\\-]*)";
    private static final String INSTANCE_REGEX = "([a-z0-9][a-z0-9\\-]*)?";
    private static final String TYPE_REGEX = "([a-z][a-z0-9\\-]*)";
    private static final String CONTEXT_REGEX = "([a-zA-Z0-9_\\-]*)";
    private static final String LOCATOR_REGEX = "([a-zA-Z0-9_\\-\\.]+)";

    private static final Pattern SERVICE_PATTERN = Pattern.compile(SERVICE_REGEX);
    private static final Pattern INSTANCE_PATTERN = Pattern.compile(INSTANCE_REGEX);
    private static final Pattern TYPE_PATTERN = Pattern.compile(TYPE_REGEX);
    private static final Pattern CONTEXT_PATTERN = Pattern.compile(CONTEXT_REGEX);
    private static final Pattern LOCATOR_PATTERN = Pattern.compile(LOCATOR_REGEX);
    // creates a Pattern in form of ri.<service>.<instance>.<type>.<context>.<locator>
    private static final Pattern SPEC_PATTERN = Pattern.compile(
            RID_CLASS + "\\." + SERVICE_REGEX + "\\." + INSTANCE_REGEX + "\\." +
            TYPE_REGEX + "\\." + CONTEXT_REGEX + "\\." + LOCATOR_REGEX);

    // fields are not final due to Jackson default constructor
    private String service;
    private String instance;
    private String type;
    private String context;
    private String locator;

    private ResourceIdentifier() {
        // default constructor for Jackson
    }

    private ResourceIdentifier(String service, String instance, String type, String context, String locator) {
        this.service = service;
        this.instance = instance == null ? "" : instance;
        this.type = type;
        this.context = context;
        this.locator = locator;
    }

    /**
     * Returns the service component.
     *
     * @return the service component from this identifier
     */
    public String getService() {
        return service;
    }

    /**
     * Returns the instance component.
     *
     * @return the instance component from this identifier
     */
    public String getInstance() {
        return instance;
    }

    /**
     * Returns the type component.
     *
     * @return the type component from this identifier
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the context component.
     *
     * @return the context component from this identifier
     */
    public String getContext() {
        return context;
    }

    /**
     * Returns the locator component.
     *
     * @return the locator component from this identifier
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
        StringBuilder builder = new StringBuilder(RID_CLASS).append(SEPARATOR)
                .append(service).append(SEPARATOR)
                .append(instance).append(SEPARATOR)
                .append(type).append(SEPARATOR)
                .append(context).append(SEPARATOR)
                .append(locator);
        return builder.toString();
    }

    /**
     * Returns the hash code value for identifier.  The hash code
     * is calculated using the Java {@link Objects#hash(Object...)} method
     * over each of the 4 components.
     *
     * @return the hash code value for this identifier
     */
    @Override
    public int hashCode() {
        return Objects.hash(service, instance, type, context, locator);
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
        return Objects.equals(service, other.service) &&
                Objects.equals(instance, other.instance) &&
                Objects.equals(type, other.type) &&
                Objects.equals(context, other.context) &&
                Objects.equals(locator, other.locator);
    }

    /**
     * Checks if the input string is a valid resource identifier as defined in the specification.
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
                return new ResourceIdentifier(matcher.group(1), matcher.group(2), matcher.group(3),
                    matcher.group(4), matcher.group(5));
            }
        }
        throw new IllegalArgumentException("Illegal resource identifier format: " + rid);
    }


    /**
     * Generates a new resource identifier object from each of the 4 input components. Each component must
     * satisfy the requirements as defined by the specification.
     *
     * @param service input representing the service component
     * @param instance input representing the instance component
     * @param type input representing the type component
     * @param context input representing the context component
     * @param locator input representing the locator component
     * @return a resource identifier object representing the input components
     *
     * @throws IllegalArgumentException if any of the inputs do not satisfy the resource identifier specification
     */
    public static ResourceIdentifier of(String service, String instance, String type, String context, String locator) {
        checkServiceIsValid(service);
        checkInstanceIsValid(instance);
        checkTypeIsValid(type);
        checkContextIsValid(context);
        checkLocatorIsValid(locator);
        return new ResourceIdentifier(service, instance, type, context, locator);
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

    private static void checkContextIsValid(String context) {
        if (context == null || !CONTEXT_PATTERN.matcher(context).matches()) {
            throw new IllegalArgumentException("Illegal context format: " + context);
        }
    }

    private static void checkLocatorIsValid(String value) {
        if (value == null || !LOCATOR_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Illegal locator format: " + value);
        }
    }
}
