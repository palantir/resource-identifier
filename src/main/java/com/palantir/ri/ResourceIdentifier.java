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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines a common format for wrapping existing unique identifiers to provide additional context. This class
 * provides utility method: {@code #of(String)} to parse an existing identifier or the ability to generate new
 * identifiers by using provided context with method {@code #of(String, String, String)}.
 * <p>
 * Resource identifier specification:
 * <p>
 * Resource Identifiers contain 4 components, prefixed by a format identifier ri and separated with periods:
 * {@code ri.<service>.<instance>.<type>.<locator>}
 * <ol>
 * <li> Service: a string that represents the service (or application) that namespaces the rest of the identifier.
 *      Must conform with regex pattern {@code [a-z][a-z0-9\-]*}
 * <li> Instance: an optionally empty string that represents a specific service cluster, to allow disambiguation of
 *      artifacts from different service clusters. Must conform to regex pattern {@code ([a-z0-9][a-z0-9\-]*)?}
 * <li> Type: a service-specific resource type to namespace a group of locators. Must conform to regex pattern
 *      {@code [a-z][a-z0-9\-]*}
 * <li> Locator: a string used to uniquely locate the specific resource. Must conform to regex pattern
 *      {@code [a-zA-Z0-9\-\._]+}
 * </ol>
 */
public class ResourceIdentifier {
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
            RID_CLASS + "\\." + SERVICE_REGEX + "\\." + INSTANCE_REGEX + "\\."
            + TYPE_REGEX + "\\." + LOCATOR_REGEX);

    private final String service;
    private final String instance;
    private final String type;
    private final String locator;

    private ResourceIdentifier(String service, String instance, String type, String locator) {
        this.service = service;
        this.instance = instance == null ? "" : instance;
        this.type = type;
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
     * Returns the locator component.
     *
     * @return the locator component from this identifier
     */
    public String getLocator() {
        return locator;
    }

    /**
     * Returns a string representation of this ResourceIdentifier. The string representation
     * follows the format specification using the "ri" header followed by the 5 components
     * separated by periods.
     *
     * @return a string representation of this identifier
     */
    @Override
    @JsonValue
    public String toString() {
        StringBuilder builder = new StringBuilder(RID_CLASS).append(SEPARATOR)
                .append(service).append(SEPARATOR)
                .append(instance).append(SEPARATOR)
                .append(type).append(SEPARATOR)
                .append(locator);
        return builder.toString();
    }

    /**
     * Returns the hash code value for identifier.  The hash code
     * is calculated using the Java {@link Objects#hash(Object...)} method
     * over each of the 5 components.
     *
     * @return the hash code value for this identifier
     */
    @Override
    public int hashCode() {
        return Objects.hash(service, instance, type, locator);
    }

    /**
     * Compares the specified object with this identifier for equality.  Returns
     * {@code true} if and only if the specified object is also a resource identifier and
     * contain exactly the same values for all 5 components.
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
        return Objects.equals(service, other.service)
                && Objects.equals(instance, other.instance)
                && Objects.equals(type, other.type)
                && Objects.equals(locator, other.locator);
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
     * Checks if the input string is a valid service as defined in the specification.
     *
     * @param service the input string to be checked
     * @return {@code true} if and only if the input satisfy the service specification,
     *         {@code false} otherwise.
     */
    public static boolean isValidService(String service) {
        return service != null && SERVICE_PATTERN.matcher(service).matches();
    }

    /**
     * Checks if the input string is a valid instance as defined in the specification.
     *
     * @param instance the input string to be checked
     * @return {@code true} if and only if the input satisfy the instance specification,
     *         {@code false} otherwise.
     */
    public static boolean isValidInstance(String instance) {
        return instance != null && INSTANCE_PATTERN.matcher(instance).matches();
    }

    /**
     * Checks if the input string is a valid type as defined in the specification.
     *
     * @param type the input string to be checked
     * @return {@code true} if and only if the input satisfy the type specification,
     *         {@code false} otherwise.
     */
    public static boolean isValidType(String type) {
        return type != null && TYPE_PATTERN.matcher(type).matches();
    }

    /**
     * Checks if the input string is a valid locator as defined in the specification.
     *
     * @param locator the input string to be checked
     * @return {@code true} if and only if the input satisfy the locator specification,
     *         {@code false} otherwise.
     */
    public static boolean isValidLocator(String locator) {
        return locator != null && LOCATOR_PATTERN.matcher(locator).matches();
    }

    /**
     * Same as {@link #of(String)}. Included to support JAX-RS
     * <a href="http://docs.oracle.com/javaee/7/api/javax/ws/rs/PathParam.html">
     * query and path parameters</a>
     */
    @JsonCreator
    public static ResourceIdentifier valueOf(String rid) {
        return of(rid);
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
                    matcher.group(4));
            }
        }
        throw new IllegalArgumentException("Illegal resource identifier format: " + rid);
    }


    /**
     * Generates a new resource identifier object from each of the 5 input components. Each component must
     * satisfy the requirements as defined by the specification.
     *
     * @param service input representing the service component
     * @param instance input representing the instance component
     * @param type input representing the type component
     * @param locator input representing the locator component
     * @return a resource identifier object representing the input components
     *
     * @throws IllegalArgumentException if any of the inputs do not satisfy the resource identifier specification
     */
    public static ResourceIdentifier of(String service, String instance, String type, String locator) {
        checkServiceIsValid(service);
        checkInstanceIsValid(instance);
        checkTypeIsValid(type);
        checkLocatorIsValid(locator);
        return new ResourceIdentifier(service, instance, type, locator);
    }

    /**
     * Generates a new resource identifier object from each of the input components. The locator component is produced
     * by joining together locatorComponent and locatorComponents with the default separator.
     *
     * @param service input representing the service component
     * @param instance input representing the instance component
     * @param type input representing the type component
     * @param firstLocatorComponent the first part of the locator component
     * @param locatorComponents the remaining locator components
     * @return a resource identifier object representing the input components
     *
     * @throws IllegalArgumentException if any of the inputs do not satisfy the resource identifier specification
     */
    public static ResourceIdentifier of(String service, String instance, String type,
            String firstLocatorComponent, String... locatorComponents) {
        StringBuilder builder = new StringBuilder(firstLocatorComponent);
        for (String component : locatorComponents) {
            builder.append(SEPARATOR).append(component);
        }
        return of(service, instance, type, builder.toString());
    }

    private static void checkServiceIsValid(String service) {
        if (!isValidService(service)) {
            throw new IllegalArgumentException("Illegal service format: " + service);
        }
    }

    private static void checkInstanceIsValid(String instance) {
        if (!isValidInstance(instance)) {
            throw new IllegalArgumentException("Illegal instance format: " + instance);
        }
    }

    private static void checkTypeIsValid(String type) {
        if (!isValidType(type)) {
            throw new IllegalArgumentException("Illegal type format: " + type);
        }
    }

    private static void checkLocatorIsValid(String value) {
        if (!isValidLocator(value)) {
            throw new IllegalArgumentException("Illegal locator format: " + value);
        }
    }

}
