/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.vault.validation.spi.impl;

import org.apache.jackrabbit.vault.validation.ValidationExecutorTest;
import org.apache.jackrabbit.vault.validation.it.AbstractValidationIT;
import org.apache.jackrabbit.vault.validation.spi.NodeContext;
import org.apache.jackrabbit.vault.validation.spi.ValidationMessage;
import org.apache.jackrabbit.vault.validation.spi.ValidationMessageSeverity;
import org.apache.jackrabbit.vault.validation.spi.util.NodeContextImpl;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BannedSubPackageValidatorTest {

    @Test
    public void testBannedSubPackage() throws URISyntaxException {
        Set<String> bannedSubPackages = new HashSet<>();
        bannedSubPackages.add("org.apache.jackrabbit.filevault:package-plugin-test-pkg");
        BannedSubPackageValidator validator = new BannedSubPackageValidator(ValidationMessageSeverity.ERROR, false, bannedSubPackages);
        NodeContext nodeContext = new NodeContextImpl("/apps/packages/content/install/content-pkg.zip", Paths.get("apps/packages/content/install/content-pkg.zip"), Paths.get(AbstractValidationIT.class.getResource("/container-with-banned-subpackage/jcr_root").toURI()));
        Collection<ValidationMessage> messages = validator.validateJcrPath(nodeContext, false, false);
        ValidationExecutorTest.assertViolation(messages,
                new ValidationMessage(
                        ValidationMessageSeverity.ERROR, "Found banned sub-package, group: org.apache.jackrabbit.filevault, name: package-plugin-test-pkg"));
    }

    @Test
    public void testIncorrectConfiguredBannedSubPackage() throws URISyntaxException {
        Set<String> bannedSubPackages = new HashSet<>();
        bannedSubPackages.add("wrong-config");
        BannedSubPackageValidator validator = new BannedSubPackageValidator(ValidationMessageSeverity.ERROR, false, bannedSubPackages);
        NodeContext nodeContext = new NodeContextImpl("/apps/packages/content/install/content-pkg.zip", Paths.get("apps/packages/content/install/content-pkg.zip"), Paths.get(AbstractValidationIT.class.getResource("/container-with-banned-subpackage/jcr_root").toURI()));
        Collection<ValidationMessage> messages = validator.validateJcrPath(nodeContext, false, false);
        ValidationExecutorTest.assertViolation(messages,
                new ValidationMessage(
                        ValidationMessageSeverity.ERROR, "Incorrect configuration of banned package: wrong-config"));

    }
}
