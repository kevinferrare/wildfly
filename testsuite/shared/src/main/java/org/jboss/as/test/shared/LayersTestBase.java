/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.test.shared;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jboss.as.test.layers.LayersTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class LayersTestBase {

    /**
     * Gets the expected set of packages that are not referenced from the module graph
     * but need to be provisioned.
     * This is the expected set of modules found when scanning the default configuration that are
     * not referenced directly or transitively from a standalone server's root module or from
     * one of the extensions used in standalone.xml.
     */
    protected abstract Set<String> getExpectedUnreferenced();

    /**
     * Gets the expected set of packages that are provisioned by the test-standalone-reference installation
     * but not used in the test-all-layers installation.
     * This is the expected set of not provisioned modules when all layers are provisioned; i.e.
     * those that are not associated with any layer included in the test-all-layers installation.
     */
    protected abstract Set<String> getExpectedUnusedInAllLayers();

    /**
     * Packages that are always expected to be included in the return value of {@link #getExpectedUnusedInAllLayers()}.
     */
    public static final String[] NO_LAYER_COMMON = {
            // not used
            "ibm.jdk",
            "javax.api",
            "javax.sql.api",
            "javax.xml.stream.api",
            "sun.jdk",
            "sun.scripting",
            // test-all-layers installation is non-ha and does not include layers that provide jgroups
            "org.jboss.as.clustering.jgroups",
            // TODO we need to add an agroal layer
            "org.wildfly.extension.datasources-agroal",
            "io.agroal",
            // Legacy subsystems for which we will not provide layers
            "org.wildfly.extension.picketlink",
            "org.jboss.as.jsr77",
            "org.keycloak.keycloak-adapter-subsystem",
            "org.jboss.as.security",
            // end legacy subsystems ^^^
            // Special support feature
            "org.wildfly.security.http.sfbasic",
            // TODO move eclipse link support to an external feature pack
            "org.eclipse.persistence",
            // RA not associated with any layer
            "org.jboss.genericjms",
            // Appclient support is not provided by a layer
            "org.jboss.as.appclient",
            "org.jboss.metadata.appclient",
            // TODO WFLY-16576 -- cruft?
            "org.bouncycastle",
            // This was brought in as part an RFE, WFLY-10632 & WFLY-10636. While the module is currently marked as private,
            // for now we should keep this module.
            "org.jboss.resteasy.resteasy-rxjava2",
            "org.jboss.resteasy.resteasy-tracing-api",
            // TODO these implement SPIs from RESTEasy or JBoss WS but I don't know how they integrate
            // as there is no ref to them in any module.xml nor any in WF java code.
            // Perhaps via deployment descriptor? In any case, no layer provides them
            "org.wildfly.security.jakarta.client.resteasy",
            "org.wildfly.security.jakarta.client.webservices",
            // Alternative messaging protocols besides the std Artemis core protocol
            // Use of these depends on an attribute value setting
            "org.apache.activemq.artemis.protocol.amqp",
            "org.apache.qpid.proton",
            "org.apache.activemq.artemis.protocol.hornetq",
            "org.apache.activemq.artemis.protocol.stomp",
            // Legacy client not associated with any layer
            "org.hornetq.client",
            // TODO we need to add an rts layer
            "org.wildfly.extension.rts",
            "org.jboss.narayana.rts",
            // TODO we need to add an xts layer
            "org.jboss.as.xts",
            // TODO WFLY-16586 microprofile-reactive-streams-operators layer should provision this
            "org.wildfly.reactive.dep.jts",
            // TODO should an undertow layer specify this?
            "org.wildfly.event.logger",
            // TODO test-all-layers uses microprofile-opentracing instead of opentelemetry
            "org.wildfly.extension.opentelemetry",
            "org.wildfly.extension.opentelemetry-api",
            "io.opentelemetry.exporter",
            "io.opentelemetry.sdk",
            "io.opentelemetry.proto",
            "io.opentelemetry.otlp",
            // Micrometer is not included in standard configs
            "io.micrometer",
            "org.wildfly.extension.micrometer",
            "org.wildfly.micrometer.deployment",
            "com.squareup.okhttp3",
            "org.jetbrains.kotlin.kotlin-stdlib",
            "com.google.protobuf",
            // Unreferenced Infinispan modules
            "org.infinispan.cdi.common",
            "org.infinispan.cdi.embedded",
            "org.infinispan.cdi.remote",
            "org.infinispan.counter",
            "org.infinispan.lock",
            "org.infinispan.query",
            "org.infinispan.query.core",
            // JGroups external protocols - AWS
            "org.jgroups.aws",
            "software.amazon.awssdk.s3",
            // MicroProfile
            "org.wildfly.extension.microprofile.metrics-smallrye",
            "org.wildfly.extension.microprofile.opentracing-smallrye",
            //xerces dependency is eliminated from different subsystems and use JDK JAXP instead
            "org.apache.xerces",
    };

    /**
     * Included in the return value of {@link #getExpectedUnusedInAllLayers()}
     * only when testing provisioning directly from the wildfly-ee feature pack.
     */
    public static final String[] NO_LAYER_WILDFLY_EE = {
            // Messaging broker not included in the messaging-activemq layer
            "org.jboss.xnio.netty.netty-xnio-transport",
            // No patching modules in layers
            "org.jboss.as.patching",
            "org.jboss.as.patching.cli",
            // In wildfly-ee only referenced by the
            // unused-in-all-layers org.jboss.resteasy.resteasy-rxjava2
            "io.reactivex.rxjava2.rxjava"
    };

    /**
     * Included in the return value of {@link #getExpectedUnusedInAllLayers()}
     * when testing provisioning from the wildfly or wildfly-preview feature packs.
     * Use this array for items common between the two feature packs.
     */
    public static final String[] NO_LAYER_EXPANSION = {};

    /**
     * Included in the return value of {@link #getExpectedUnusedInAllLayers()}
     * only when testing provisioning from the wildfly feature pack.
     */
    public static final String[] NO_LAYER_WILDFLY = {
            // Legacy subsystems for which we will not provide layers
            "org.wildfly.extension.microprofile.metrics-smallrye",
            "org.wildfly.extension.microprofile.opentracing-smallrye",
    };

    /**
     * Included in the return value of {@link #getExpectedUnusedInAllLayers()}
     * only when testing provisioning from the wildfly-preview feature pack.
     */
    public static final String[] NO_LAYER_WILDFLY_PREVIEW = {
            // WFP standard config uses Micrometer instead of WF Metrics
            "org.wildfly.extension.metrics",
            // MP Fault Tolerance has a dependency on MP Metrics
            "io.smallrye.fault-tolerance",
            "org.eclipse.microprofile.fault-tolerance.api",
            "org.wildfly.extension.microprofile.fault-tolerance-smallrye",
            "org.wildfly.microprofile.fault-tolerance-smallrye.deployment",
            // Used by Hibernate Search but only in preview TODO this doesn't seem right; NOT_REFERENCED should suffice
            "org.hibernate.search.mapper.orm.coordination.outboxpolling"
    };

    /**
     * Packages that are always expected to be included in the return value of {@link #getExpectedUnreferenced()}.
     */
    public static final String[] NOT_REFERENCED_COMMON = {
            // injected by logging
            "org.apache.logging.log4j.api",
            "org.jboss.logmanager.log4j2",
            // injected by ee
            "org.wildfly.naming",
            // Injected by jaxrs
            "org.jboss.resteasy.resteasy-json-binding-provider",
            // Injected by jaxrs and also depended upon by narayano-rts, which is part of the non-OOTB rts subsystem
            "org.jboss.resteasy.resteasy-json-p-provider",
            // The console ui content is not part of the kernel nor is it provided by an extension
            "org.jboss.as.console",
            // tooling
            "org.jboss.as.domain-add-user",
            // injected by server in UndertowHttpManagementService
            "org.jboss.as.domain-http-error-context",
            // injected by jsf
            "org.jboss.as.jsf-injection",
            // Brought by galleon FP config
            "org.jboss.as.product",
            // Brought by galleon FP config
            "org.jboss.as.standalone",
            // injected by logging
            "org.jboss.logging.jul-to-slf4j-stub",
            "org.jboss.resteasy.resteasy-client-microprofile",
            // Webservices tooling
            "org.jboss.ws.tools.common",
            "org.jboss.ws.tools.wsconsume",
            "org.jboss.ws.tools.wsprovide",
            "gnu.getopt",
            // Elytron tooling
            "org.wildfly.security.elytron-tool",
            // bootable jar runtime
            "org.wildfly.bootable-jar",
            // Extension not included in the default config
            "org.wildfly.extension.clustering.singleton",
            // Extension not included in the default config
            "org.wildfly.extension.microprofile.health-smallrye",
            "org.eclipse.microprofile.health.api",
            "io.smallrye.health",
            // Extension not included in the default config
            "org.wildfly.extension.microprofile.lra-coordinator",
            "org.wildfly.extension.microprofile.lra-participant",
            "org.jboss.narayana.rts.lra-coordinator",
            "org.jboss.narayana.rts.lra-participant",
            "org.eclipse.microprofile.lra.api",
            // Extension not included in the default config
            "org.wildfly.extension.microprofile.openapi-smallrye",
            "org.eclipse.microprofile.openapi.api",
            "io.smallrye.openapi",
            "com.fasterxml.jackson.dataformat.jackson-dataformat-yaml",
            // Extension not included in the default config
            "org.wildfly.extension.microprofile.reactive-messaging-smallrye",
            // Extension not included in the default config
            "org.wildfly.extension.microprofile.telemetry",
            // Extension not included in the default config
            "org.wildfly.extension.microprofile.reactive-streams-operators-smallrye",
            "org.wildfly.reactive.mutiny.reactive-streams-operators.cdi-provider",
            "io.vertx.client",
            // Dynamically added by ee-security and mp-jwt-smallrye DUPs but not referenced by subsystems.
            "org.wildfly.security.jakarta.security",
            // injected by sar
            "org.jboss.as.system-jmx",
            // Loaded reflectively by the jboss fork impl of jakarta.xml.soap.FactoryFinder
            "org.jboss.ws.saaj-impl",
            // TODO just a testsuite utility https://wildfly.zulipchat.com/#narrow/stream/174184-wildfly-developers/topic/org.2Ejboss.2Ews.2Ecxf.2Ests.20module
            "org.jboss.ws.cxf.sts",
    };


    /**
     * Included in the return value of {@link #getExpectedUnreferenced()}
     * only when testing provisioning directly from the wildfly-ee feature pack.
     */
    public static final String[] NOT_REFERENCED_WILDFLY_EE = {
            // WFLY-18386 two io.netty.netty-codec... modules should be moved to wildfly feature pack
            "io.netty.netty-codec-dns",
            "io.netty.netty-codec-http2",
            // TODO
            "io.netty.netty-resolver-dns",
            // injected by ee
            "jakarta.json.bind.api",
            // injected by jpa
            "org.hibernate.search.orm",
            "org.hibernate.search.backend.elasticsearch",
            "org.hibernate.search.backend.lucene",
            // Used by the hibernate search that's injected by jpa
            "org.elasticsearch.client.rest-client",
            "com.google.code.gson",
            "com.carrotsearch.hppc",
            "org.apache.lucene",
            // Brought by galleon ServerRootResourceDefinition
            "wildflyee.api"
    };


    /**
     * Included in the return value of {@link #getExpectedUnreferenced()}
     * when testing provisioning from the wildfly or wildfly-preview feature packs.
     * Use this array for items common between the two feature packs.
     */
    public static final String[] NOT_REFERENCED_EXPANSION = {};

    /**
     * Included in the return value of {@link #getExpectedUnreferenced()}
     * only when testing provisioning from the wildfly-preview feature pack.
     */
    public static final String[] NOT_REFERENCED_WILDFLY = {};

    /**
     * Included in the return value of {@link #getExpectedUnreferenced()}
     * only when testing provisioning from the wildfly-preview feature pack.
     */
    public static final String[] NOT_REFERENCED_WILDFLY_PREVIEW = {
            "org.wildfly.extension.metrics",
            // Used by the hibernate search that's injected by jpa
            "org.hibernate.search.mapper.orm.coordination.outboxpolling",
            "org.apache.avro"
    };

    /**
     * A HashMap to configure a banned module.
     * They key is the banned module name, the value is an optional List with the installation names that are allowed to
     * provision the banned module. This installations will be ignored.
     *
     * Notice the allowed installation names does not distinguish between different parent names, e.g test-all-layers here means
     * allowing root/test-all-layers and servletRoot/test-all-layers.
     */
    private static final HashMap<String, List<String>> BANNED_MODULES_CONF = new HashMap<>(){{
        put("org.jboss.as.security", Arrays.asList("test-all-layers-jpa-distributed", "test-all-layers", "legacy-security", "test-standalone-reference"));
    }};

    public static String root;
    public static String defaultConfigsRoot;

    @BeforeClass
    public static void setUp() {
        root = System.getProperty("layers.install.root");
        defaultConfigsRoot = System.getProperty("std.default.install.root");
    }

    @AfterClass
    public static void cleanUp() {
        Boolean delete = Boolean.getBoolean("layers.delete.installations");
        if(delete) {
            File[] installations = new File(root).listFiles(File::isDirectory);
            for(File f : installations) {
                LayersTest.recursiveDelete(f.toPath());
            }
            installations = new File(defaultConfigsRoot).listFiles(File::isDirectory);
            for(File f : installations) {
                LayersTest.recursiveDelete(f.toPath());
            }
        }
    }

    @Test
    public void test() throws Exception {
        LayersTest.test(root, getExpectedUnreferenced(), getExpectedUnusedInAllLayers());
    }

    @Test
    public void checkBannedModules() throws Exception {
        final HashMap<String, String> results = LayersTest.checkBannedModules(root, BANNED_MODULES_CONF);
        Assert.assertTrue("The following banned modules were provisioned " + results.toString(), results.isEmpty());
    }

    @Test
    public void testDefaultConfigs() throws Exception {
        LayersTest.testExecution(defaultConfigsRoot);
    }
}
