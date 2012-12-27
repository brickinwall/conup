/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.tuscany.sca.contribution.processor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.extensibility.ServiceDeclaration;

/**
 * Default implementation of an extension point for XML schemas.
 *
 * @version $Rev: 909678 $ $Date: 2010-02-13 01:19:32 +0000 (Sat, 13 Feb 2010) $
 */
public class DefaultValidationSchemaExtensionPoint implements ValidationSchemaExtensionPoint {
    private boolean enabled = true;
    private List<String> schemas = new ArrayList<String>();
    private boolean loaded;
    private ExtensionPointRegistry registry;
    
    public DefaultValidationSchemaExtensionPoint(ExtensionPointRegistry registry, Map<String, String> attributes) {
        super();
        this.registry = registry;
        if (attributes != null) {
            String attr = attributes.get("enabled");
            if (attr != null) {
                enabled = Boolean.parseBoolean(attr);
            }
        }
    }
    
    public void addSchema(String uri) {
        schemas.add(uri);
    }

    public void removeSchema(String uri) {
        schemas.remove(uri);
    }

    /**
     * Load schema declarations from META-INF/services/
     * org.apache.tuscany.sca.contribution.processor.ValidationSchema files
     */
    private synchronized void loadSchemas() {
        if (loaded || (!enabled))
            return;

        // Get the schema declarations
        Collection<ServiceDeclaration> schemaDeclarations;
        try {
            schemaDeclarations =
                registry.getServiceDiscovery()
                    .getServiceDeclarations("org.apache.tuscany.sca.contribution.processor.ValidationSchema");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // TODO - temp fix to ensure that the schema tuscany-sca.xsd always comes first
        String tuscanyScaXsd = null;

        // Find each schema
        for (ServiceDeclaration schemaDeclaration : schemaDeclarations) {
            URL url = schemaDeclaration.getResource(schemaDeclaration.getClassName());
            if (url == null) {
                throw new IllegalArgumentException(new FileNotFoundException(schemaDeclaration.getClassName()));
            }

            if (url.toString().contains("tuscany-sca-1.1.xsd")) {
                tuscanyScaXsd = url.toString();
            } else {
                schemas.add(url.toString());
            }
        }

        if (tuscanyScaXsd != null) {
            schemas.add(0, tuscanyScaXsd);
        }

        loaded = true;
    }

    public List<String> getSchemas() {
        if (enabled) {
            loadSchemas();
            return schemas;
        } else {
            return Collections.emptyList();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
