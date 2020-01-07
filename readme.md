<p align="right">
<a href="https://autorelease.general.dmz.palantir.tech/palantir/resource-identifier"><img src="https://img.shields.io/badge/Perform%20an-Autorelease-success.svg" alt="Autorelease"></a>
</p>
Resource Identifier 
===================
[![Build Status](https://circleci.com/gh/palantir/resource-identifier.svg?style=shield)](https://circleci.com/gh/palantir/resource-identifier)
[![JCenter Release](https://img.shields.io/github/release/palantir/resource-identifier.svg)](
http://jcenter.bintray.com/com/palantir/ri/)

Resource Identifiers offer a common encoding for wrapping existing unique identifiers with some additional
context that can be useful when storing those identifiers in other applications. Additionally, the context
can be used to disambiguate application-unique, but not globally-unique, identifiers when used in a common
space.

We use a format inspired by existing standards, such as [AWS ARNs][1], [URNs][2], and [URIs][3]:

    ri.<service>.<instance>.<type>.<locator>

This project provides a basic utility class (`ResourceIdentifier`) to create and verify new identifier 
strings that follow the specified format, and, parse existing identifier strings into component parts.

**Maven Coordinates** `com.palantir.ri:resource-identifier:<version>`

Format
------
Resource Identifiers contain 4 components, prefixed by a format identifier `ri` and separated with periods:

 1. **Service**: a string that represents the service (or application) that namespaces the rest of the 
    identifier. Must conform with regex pattern `[a-z][a-z0-9\-]*`.
 2. **Instance**: an optionally empty string that represents a specific service cluster, to allow 
    disambiguation of artifacts from different service clusters. Must conform to regex pattern 
    `([a-z0-9][a-z0-9\-]*)?`.
 3. **Type**: a service-specific resource type to namespace a group of locators. Must conform to regex
    pattern `[a-z][a-z0-9\-]*`.
 4. **Locator**: a string used to uniquely locate the specific resource. Must conform to regex pattern
    `[a-zA-Z0-9\-\._]+`.

License
-------
This project is made available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).

[1]:http://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html
[2]:https://en.wikipedia.org/wiki/Uniform_resource_name
[3]:https://en.wikipedia.org/wiki/Uniform_resource_identifier
