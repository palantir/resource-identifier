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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.errorprone.annotations.Immutable;
import com.palantir.logsafe.Safe;

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
@Immutable
public final class ResourceIdentifier {

    private static final String RID_PREFIX = "ri.";
    private static final int RID_PREFIX_LENGTH = 3;
    private static final char SEPARATOR = '.';

    private static final int INDEX_INVALID = -1;
    private static final int INDEX_END = -2;

    private final String resourceIdentifier;
    private final int serviceIndex;
    private final int instanceIndex;
    private final int typeIndex;

    private ResourceIdentifier(String validatedString, int serviceIndex, int instanceIndex, int typeIndex) {
        this.resourceIdentifier = validatedString;
        this.serviceIndex = serviceIndex;
        this.instanceIndex = instanceIndex;
        this.typeIndex = typeIndex;
    }

    /**
     * Returns the service component.
     *
     * @return the service component from this identifier
     */
    public String getService() {
        return resourceIdentifier.substring(RID_PREFIX_LENGTH, serviceIndex);
    }

    /**
     * Returns the instance component.
     *
     * @return the instance component from this identifier
     */
    public String getInstance() {
        return resourceIdentifier.substring(serviceIndex + 1, instanceIndex);
    }

    /**
     * Returns the type component.
     *
     * @return the type component from this identifier
     */
    public String getType() {
        return resourceIdentifier.substring(instanceIndex + 1, typeIndex);
    }

    /**
     * Returns the locator component.
     *
     * @return the locator component from this identifier
     */
    public String getLocator() {
        return resourceIdentifier.substring(typeIndex + 1);
    }

    /**
     * Compares the service component of this identifier with the given service.  Returns
     * {@code true} if and only if the service component this identifier is equal to the given service.
     *
     * @param service the service to be compared for equality with the service component of this identifier
     * @return {@code true} if the service component of this identifier is equal to the given service, {@code false}
     * otherwise
     */
    public boolean hasService(String service) {
        return substringMatches(RID_PREFIX_LENGTH, serviceIndex, service);
    }

    /**
     * Compares the instance component of this identifier with the given instance.  Returns
     * {@code true} if and only if the instance component this identifier is equal to the given instance.
     *
     * @param instance the instance to be compared for equality with the instance component of this identifier
     * @return {@code true} if the instance component of this identifier is equal to the given instance, {@code false}
     * otherwise
     */
    public boolean hasInstance(String instance) {
        return substringMatches(serviceIndex + 1, instanceIndex, instance);
    }

    /**
     * Compares the type component of this identifier with the given type.  Returns
     * {@code true} if and only if the type component this identifier is equal to the given type.
     *
     * @param type the type to be compared for equality with the type component of this identifier
     * @return {@code true} if the type component of this identifier is equal to the given type, {@code false}
     * otherwise
     */
    public boolean hasType(String type) {
        return substringMatches(instanceIndex + 1, typeIndex, type);
    }

    /**
     * Compares the locator component of this identifier with the given locator.  Returns
     * {@code true} if and only if the locator component this identifier is equal to the given locator.
     *
     * @param locator the locator to be compared for equality with the locator component of this identifier
     * @return {@code true} if the locator component of this identifier is equal to the given locator, {@code false}
     * otherwise
     */
    public boolean hasLocator(String locator) {
        return substringMatches(typeIndex + 1, resourceIdentifier.length(), locator);
    }

    private boolean substringMatches(int beginIndex, int endIndex, String other) {
        if (other == null) {
            return false;
        }

        int length = endIndex - beginIndex;
        if (other.length() != length) {
            return false;
        }

        return resourceIdentifier.regionMatches(beginIndex, other, 0, length);
    }

    /**
     * Returns a string representation of this ResourceIdentifier. The string representation
     * follows the format specification using the "ri" header followed by the 4 components
     * separated by periods.
     *
     * @return a string representation of this identifier
     */
    @Override
    @JsonValue
    public String toString() {
        return resourceIdentifier;
    }

    /**
     * Returns the hash code value for identifier.
     *
     * @return the hash code value for this identifier
     */
    @Override
    public int hashCode() {
        return resourceIdentifier.hashCode();
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
        // Performance note from https://github.com/palantir/resource-identifier/pull/332:
        // We explicitly check hashCode equality first to short circuit via memoized RID String hashCode for
        // any mismatch to avoid comparing full RID strings as RIDs are often the same length with common prefix.
        return this.hashCode() == other.hashCode() && resourceIdentifier.equals(other.resourceIdentifier);
    }

    /**
     * Checks if the input string is a valid resource identifier as defined in the specification.
     *
     * @param rid the input string to be checked
     * @return {@code true} if and only if the input satisfy the resource identifier specification,
     *         {@code false} otherwise.
     */
    public static boolean isValid(String rid) {
        if (rid == null) {
            return false;
        }

        if (!rid.startsWith(RID_PREFIX)) {
            return false;
        }

        int serviceIndex = getServiceIndex(rid, RID_PREFIX_LENGTH);
        if (serviceIndex < 0) {
            return false;
        }

        int instanceIndex = getInstanceIndex(rid, serviceIndex + 1);
        if (instanceIndex < 0) {
            return false;
        }

        int typeIndex = getTypeIndex(rid, instanceIndex + 1);
        if (typeIndex < 0) {
            return false;
        }

        int locatorIndex = getLocatorIndex(rid, typeIndex + 1);
        if (locatorIndex != INDEX_END) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the input string is a valid service as defined in the specification.
     *
     * @param service the input string to be checked
     * @return {@code true} if and only if the input satisfy the service specification,
     *         {@code false} otherwise.
     */
    public static boolean isValidService(String service) {
        return isValidService((CharSequence) service);
    }

    private static boolean isValidService(CharSequence service) {
        return getServiceIndex(service, 0) == INDEX_END;
    }

    /**
     * Checks if the input string is a valid instance as defined in the specification.
     *
     * @param instance the input string to be checked
     * @return {@code true} if and only if the input satisfy the instance specification,
     *         {@code false} otherwise.
     */
    public static boolean isValidInstance(String instance) {
        return isValidInstance((CharSequence) instance);
    }

    private static boolean isValidInstance(CharSequence instance) {
        return getInstanceIndex(instance, 0) == INDEX_END;
    }

    /**
     * Checks if the input string is a valid type as defined in the specification.
     *
     * @param type the input string to be checked
     * @return {@code true} if and only if the input satisfy the type specification,
     *         {@code false} otherwise.
     */
    public static boolean isValidType(String type) {
        return isValidType((CharSequence) type);
    }

    private static boolean isValidType(CharSequence type) {
        return getTypeIndex(type, 0) == INDEX_END;
    }

    /**
     * Checks if the input string is a valid locator as defined in the specification.
     *
     * @param locator the input string to be checked
     * @return {@code true} if and only if the input satisfy the locator specification,
     *         {@code false} otherwise.
     */
    public static boolean isValidLocator(String locator) {
        return isValidLocator((CharSequence) locator);
    }

    private static boolean isValidLocator(CharSequence locator) {
        return getLocatorIndex(locator, 0) == INDEX_END;
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
        ResourceIdentifier resultRid = tryOf(rid);
        if (resultRid == null) {
            throw new IllegalArgumentException("Illegal resource identifier format: " + rid);
        }

        return resultRid;
    }

    /**
     * Generates a new resource identifier object from each of the 4 input components. Each component must
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
    public static ResourceIdentifier of(
            @Safe String service, @Safe String instance, @Safe String type, String locator) {
        return of((CharSequence) service, instance, type, locator);
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
    public static ResourceIdentifier of(
            @Safe String service,
            @Safe String instance,
            @Safe String type,
            String firstLocatorComponent,
            String... locatorComponents) {
        int locatorLength = firstLocatorComponent.length() + locatorComponents.length;
        for (String component : locatorComponents) {
            locatorLength += component.length();
        }

        StringBuilder locator = new StringBuilder(locatorLength);
        locator.append(firstLocatorComponent);
        for (String component : locatorComponents) {
            locator.append(SEPARATOR).append(component);
        }

        return of(service, instance, type, locator);
    }

    private static ResourceIdentifier of(
            @Safe CharSequence service, @Safe CharSequence instance, @Safe CharSequence type, CharSequence locator) {
        checkServiceIsValid(service);
        checkInstanceIsValid(instance);
        checkTypeIsValid(type);
        checkLocatorIsValid(locator);

        String resourceIdentifier =
                RID_PREFIX + service + SEPARATOR + instance + SEPARATOR + type + SEPARATOR + locator;

        int serviceIndex = RID_PREFIX_LENGTH + service.length();
        int instanceIndex = serviceIndex + 1 + instance.length();
        int typeIndex = instanceIndex + 1 + type.length();
        return new ResourceIdentifier(resourceIdentifier, serviceIndex, instanceIndex, typeIndex);
    }

    private static ResourceIdentifier tryOf(String rid) {
        if (rid == null) {
            return null;
        }

        if (!rid.startsWith(RID_PREFIX)) {
            return null;
        }

        int serviceIndex = getServiceIndex(rid, RID_PREFIX_LENGTH);
        if (serviceIndex < 0) {
            return null;
        }

        int instanceIndex = getInstanceIndex(rid, serviceIndex + 1);
        if (instanceIndex < 0) {
            return null;
        }

        int typeIndex = getTypeIndex(rid, instanceIndex + 1);
        if (typeIndex < 0) {
            return null;
        }

        int locatorIndex = getLocatorIndex(rid, typeIndex + 1);
        if (locatorIndex != INDEX_END) {
            return null;
        }

        return new ResourceIdentifier(rid, serviceIndex, instanceIndex, typeIndex);
    }

    private static void checkServiceIsValid(CharSequence service) {
        if (!isValidService(service)) {
            throw new IllegalArgumentException("Illegal service format: " + service);
        }
    }

    private static void checkInstanceIsValid(CharSequence instance) {
        if (!isValidInstance(instance)) {
            throw new IllegalArgumentException("Illegal instance format: " + instance);
        }
    }

    private static void checkTypeIsValid(CharSequence type) {
        if (!isValidType(type)) {
            throw new IllegalArgumentException("Illegal type format: " + type);
        }
    }

    private static void checkLocatorIsValid(CharSequence locator) {
        if (!isValidLocator(locator)) {
            throw new IllegalArgumentException("Illegal locator format: " + locator);
        }
    }

    private static int getServiceIndex(CharSequence value, int start) {
        if (value == null) {
            return INDEX_INVALID;
        }

        int length = value.length();
        if (start >= length) {
            return INDEX_INVALID;
        }

        for (int i = start; i < length; i++) {
            char ch = value.charAt(i);
            if (i == start) {
                if (!isLowerAlpha(ch)) {
                    return INDEX_INVALID;
                }
            } else if (ch == SEPARATOR) {
                return i;
            } else if (!(isLowerAlpha(ch) || isDigit(ch) || isDash(ch))) {
                return INDEX_INVALID;
            }
        }

        return INDEX_END;
    }

    @SuppressWarnings("CyclomaticComplexity")
    private static int getInstanceIndex(CharSequence value, int start) {
        if (value == null) {
            return INDEX_INVALID;
        }

        int length = value.length();
        if (start > length) {
            return INDEX_INVALID;
        }

        for (int i = start; i < length; i++) {
            char ch = value.charAt(i);
            if (ch == SEPARATOR) {
                return i;
            } else if (i == start) {
                if (!(isLowerAlpha(ch) || isDigit(ch))) {
                    return INDEX_INVALID;
                }
            } else if (!(isLowerAlpha(ch) || isDigit(ch) || isDash(ch))) {
                return INDEX_INVALID;
            }
        }

        return INDEX_END;
    }

    private static int getTypeIndex(CharSequence value, int start) {
        // The type component has the same format as the service component
        return getServiceIndex(value, start);
    }

    private static int getLocatorIndex(CharSequence value, int start) {
        if (value == null) {
            return INDEX_INVALID;
        }

        int length = value.length();
        if (start >= length) {
            return INDEX_INVALID;
        }

        for (int i = start; i < length; i++) {
            char ch = value.charAt(i);
            if (!(isLowerAlpha(ch) || isUpperAlpha(ch) || isDigit(ch) || isDot(ch) || isDash(ch) || isUnderscore(ch))) {
                return INDEX_INVALID;
            }
        }

        return INDEX_END;
    }

    private static boolean isLowerAlpha(char ch) {
        return 'a' <= ch && ch <= 'z';
    }

    private static boolean isUpperAlpha(char ch) {
        return 'A' <= ch && ch <= 'Z';
    }

    private static boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

    private static boolean isDot(char ch) {
        return ch == '.';
    }

    private static boolean isDash(char ch) {
        return ch == '-';
    }

    private static boolean isUnderscore(char ch) {
        return ch == '_';
    }
}
