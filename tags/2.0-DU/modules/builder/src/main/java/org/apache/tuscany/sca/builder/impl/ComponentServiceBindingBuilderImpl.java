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
import org.apache.tuscany.sca.assembly.ComponentService;
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
 * component service bindings may need.  Used for WSDL generation.
 *
 * @version $Rev: 955601 $ $Date: 2010-06-17 14:55:03 +0100 (Thu, 17 Jun 2010) $
 */
public class ComponentServiceBindingBuilderImpl implements CompositeBuilder {
    private BuilderExtensionPoint builders;

    public ComponentServiceBindingBuilderImpl(ExtensionPointRegistry registry) {
        this.builders = registry.getExtensionPoint(BuilderExtensionPoint.class);
    }

    public String getID() {
        return "org.apache.tuscany.sca.assembly.builder.ComponentServiceBindingBuilder";
    }

    public Composite build(Composite composite, BuilderContext context)
        throws CompositeBuilderException {
        buildServiceBindings(composite, context);
        return composite;
    }

    private void buildServiceBindings(Composite composite, BuilderContext context) {

        // build bindings recursively
        for (Component component : composite.getComponents()) {
            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {
                buildServiceBindings((Composite)implementation, context);
            }
        }

        // find all the component service bindings     
        for (Component component : composite.getComponents()) {
            for (ComponentService componentService : component.getServices()) {
                for (Binding binding : componentService.getBindings()) {
                    BindingBuilder builder = builders.getBindingBuilder(binding.getType());
                    if (builder != null) {
                        builder.build(component, componentService, binding, context, false);
                    }
                }
            }
        }
    }

}
