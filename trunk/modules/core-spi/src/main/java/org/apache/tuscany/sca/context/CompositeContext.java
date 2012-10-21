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

package org.apache.tuscany.sca.context;

import java.util.HashMap;
import java.util.Map;

import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.definitions.Definitions;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.DomainRegistry;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentContext;

/**
 * The context associated with the Node that provides access to ExtensionPointRegistry and DomainRegistry
 * 
 * @version $Rev: 1103629 $ $Date: 2011-05-16 08:21:58 +0100 (Mon, 16 May 2011) $
 */
public class CompositeContext {
    protected ExtensionPointRegistry extensionPointRegistry;
    protected DomainRegistry domainRegistry;
    protected ComponentContextFactory componentContextFactory;
    protected Composite domainComposite;
    protected String nodeURI;
    protected String domainURI;
    protected Definitions systemDefinitions;

    protected Map<String, Object> attributes = new HashMap<String, Object>();

    public CompositeContext(ExtensionPointRegistry registry,
                            DomainRegistry domainRegistry,
                            Composite domainComposite,
                            String domainURI,
                            String nodeURI,
                            Definitions systemDefinitions) {
        this.extensionPointRegistry = registry;
        this.domainRegistry = domainRegistry;
        ContextFactoryExtensionPoint contextFactories = registry.getExtensionPoint(ContextFactoryExtensionPoint.class);
        this.componentContextFactory = contextFactories.getFactory(ComponentContextFactory.class);
        this.domainComposite = domainComposite;
        this.domainURI = domainURI;
        this.nodeURI = nodeURI;
        this.systemDefinitions = systemDefinitions;
    }

    public CompositeContext(ExtensionPointRegistry registry, DomainRegistry domainRegistry) {
        this(registry, domainRegistry, null, "default", "default", null);
    }

    /**
     * @return
     */
    public static RuntimeComponent getCurrentComponent() {
        Message message = ThreadMessageContext.getMessageContext();
        if (message != null) {
            Endpoint to = message.getTo();
            if (to == null) {
                return null;
            }
            RuntimeComponent component = (RuntimeComponent)message.getTo().getComponent();
            return component;
        }
        return null;
    }

    /**
     * @return
     */
    public static CompositeContext getCurrentCompositeContext() {
        RuntimeComponent component = getCurrentComponent();
        if (component != null) {
            RuntimeComponentContext componentContext = component.getComponentContext();
            return componentContext.getCompositeContext();
        }
        return null;
    }

    public void bindComponent(RuntimeComponent runtimeComponent) {
        RuntimeComponentContext componentContext =
            (RuntimeComponentContext)componentContextFactory.createComponentContext(this, runtimeComponent);
        runtimeComponent.setComponentContext(componentContext);
    }

    /**
     * 
     * @param endpointReference
     */
    public void bindEndpointReference(EndpointReference endpointReference) {

    }

    /**
     * Get the ExtensionPointRegistry for this node
     * @return The ExtensionPointRegistry
     */
    public ExtensionPointRegistry getExtensionPointRegistry() {
        return extensionPointRegistry;
    }

    /**
     * Get the DomainRegistry
     * @return The DomainRegistry for this node
     */
    public DomainRegistry getEndpointRegistry() {
        return domainRegistry;
    }

    public Composite getDomainComposite() {
        return domainComposite;
    }

    public String getNodeURI() {
        return nodeURI;
    }

    public String getDomainURI() {
        return domainURI;
    }

    /**
     * The system definitions that result from starting the runtime. 
     * TODO - these can be null when the SCAClient starts the runtime
     * 
     * @return systemDefinitions
     */
    public Definitions getSystemDefinitions() {
        return systemDefinitions;
    }

    /**
     * Look up an attribute value by name
     * @param <T>
     * @param name The name of the attribute
     * @return The value of the attribute
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)attributes.get(name);
    }

    /**
     * Set the value of an attribute
     * @param name The name of the attribute 
     * @param value
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
