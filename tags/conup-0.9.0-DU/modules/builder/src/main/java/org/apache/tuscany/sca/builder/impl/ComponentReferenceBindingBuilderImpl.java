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

package org.apache.tuscany.sca.builder.impl;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.builder.BindingBuilder;
import org.apache.tuscany.sca.assembly.builder.BuilderContext;
import org.apache.tuscany.sca.assembly.builder.BuilderExtensionPoint;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilder;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilderException;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;

/**
 * A composite builder that performs any additional building steps that
 * component reference bindings may need.  Used for WSDL generation.
 *
 * @version $Rev: 955601 $ $Date: 2010-06-17 14:55:03 +0100 (Thu, 17 Jun 2010) $
 */
public class ComponentReferenceBindingBuilderImpl implements CompositeBuilder {

    private BuilderExtensionPoint builders;

    public ComponentReferenceBindingBuilderImpl(ExtensionPointRegistry registry) {
        this.builders = registry.getExtensionPoint(BuilderExtensionPoint.class);
    }

    public Composite build(Composite composite, BuilderContext context)
        throws CompositeBuilderException {
        buildReferenceBindings(composite, context);
        return composite;
    }

    public String getID() {
        return "org.apache.tuscany.sca.assembly.builder.ComponentReferenceBindingBuilder";
    }

    private void buildReferenceBindings(Composite composite, BuilderContext context) {

        // find all the component reference bindings (starting at top level)     
        for (Component component : composite.getComponents()) {
            for (ComponentReference componentReference : component.getReferences()) {
                for (Binding binding : componentReference.getBindings()) {
                    BindingBuilder builder = builders.getBindingBuilder(binding.getType());
                    if (builder != null) {
                        builder.build(component, componentReference, binding, context, false);
                    }
                }
            }
        }

        // build bindings recursively
        for (Component component : composite.getComponents()) {
            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {
                buildReferenceBindings((Composite)implementation, context);
            }
        }
    }

}
