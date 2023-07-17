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

import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.jackrabbit.vault.fs.io.ZipArchive;
import org.apache.jackrabbit.vault.packaging.PackageProperties;
import org.apache.jackrabbit.vault.validation.spi.JcrPathValidator;
import org.apache.jackrabbit.vault.validation.spi.NodeContext;
import org.apache.jackrabbit.vault.validation.spi.ValidationMessage;
import org.apache.jackrabbit.vault.validation.spi.ValidationMessageSeverity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

public class BannedSubPackageValidator implements JcrPathValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BannedSubPackageValidator.class);

    private final ValidationMessageSeverity severity;
    private final boolean isSubPackage;
    private final Set<String> bannedSubPackages;

    public BannedSubPackageValidator(ValidationMessageSeverity severity, boolean isSubPackage, Set<String> bannedSubPackages) {
        this.severity = severity;
        this.isSubPackage = isSubPackage;
        this.bannedSubPackages = bannedSubPackages;
    }

    @Override
    public @Nullable Collection<ValidationMessage> validateJcrPath(@NotNull NodeContext nodeContext, boolean isFolder, boolean isDocViewXml) {
        if (isSubPackage) {
            return null; // not relevant for sub packages
        }
        Collection<ValidationMessage> messages = new LinkedList<>();
        for (String bannedSubPackage : bannedSubPackages) {
            if (isSubPackage(nodeContext.getNodePath())) {
                if (bannedSubPackage.split(":").length == 2) {
                    String bannedGroup = bannedSubPackage.split(":")[0];
                    String bannedName = bannedSubPackage.split(":")[1];
                    validateSubPackage(nodeContext, messages, bannedGroup, bannedName);
                } else {
                    messages.add(new ValidationMessage(severity, String.format("Incorrect configuration of banned package: %s", bannedSubPackage)));
                }
            }
        }
        return messages;
    }

    private void validateSubPackage(NodeContext nodeContext, Collection<ValidationMessage> messages, String bannedGroup, String bannedName) {
        String filePath = nodeContext.getBasePath().resolve(nodeContext.getFilePath()).toString();
        try (Archive subArchive = new ZipArchive(new File(filePath))) {
            subArchive.open(true);
            // assure this is a real content package
            if (subArchive.getJcrRoot() == null) {
                LOGGER.debug("ZIP entry {} is no subpackage as it is lacking the mandatory jcr_root entry", nodeContext.getBasePath());
            } else {
                PackageProperties properties = subArchive.getMetaInf().getPackageProperties();
                if (bannedGroup.equals(properties.getId().getGroup()) && bannedName.equals(properties.getId().getName())) {
                    messages.add(new ValidationMessage(severity, String.format("Found banned sub-package, group: %s, name: %s", bannedGroup, bannedName)));
                }
            }
        } catch (IOException e) {
            LOGGER.warn(String.format("Could not open sub archive for file path %s", filePath), e);
        }
    }

    @Override
    public @Nullable Collection<ValidationMessage> done() {
        return null;
    }

    static boolean isSubPackage(String nodePath) {
        return (nodePath.endsWith(".zip"));
    }

}
