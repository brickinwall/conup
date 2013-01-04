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
package org.apache.tuscany.sca.assembly;

/**
 * A factory for the assembly model
 * 
 * @version $Rev: 937291 $ $Date: 2010-04-23 14:41:24 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public interface AssemblyFactory {

    /**
     * Create a new abstract property.
     * 
     * @return a new abstract property
     */
    AbstractProperty createAbstractProperty();

    /**
     * Create a new abstract reference.
     * 
     * @return a new abstract reference
     */
    AbstractReference createAbstractReference();

    /**
     * Create a new abstract service.
     * 
     * @return a new abstract service
     */
    AbstractService createAbstractService();

    /**
     * Create a new callback.
     * 
     * @return
     */
    Callback createCallback();

    /**
     * Create a new component.
     * 
     * @return a new component
     */
    Component createComponent();

    /**
     * Create a new component property.
     * 
     * @return a new component property
     */
    ComponentProperty createComponentProperty();

    /**
     * Create a new component reference.
     * 
     * @return a new component reference
     */
    ComponentReference createComponentReference();

    /**
     * Create a new component service.
     * 
     * @return a new component service
     */
    ComponentService createComponentService();

    /**
     * Create a new component type
     * 
     * @return a new component type
     */
    ComponentType createComponentType();

    /**
     * Create a new composite.
     * 
     * @return a new composite
     */
    Composite createComposite();

    /**
     * Create a new composite reference.
     * 
     * @return a new composite reference
     */
    CompositeReference createCompositeReference();

    /**
     * Create a new composite service.
     * 
     * @return a new composite service
     */
    CompositeService createCompositeService();

    /**
     * Create a new property.
     * 
     * @return a new property
     */
    Property createProperty();

    /**
     * Create a new reference.
     * 
     * @return a new reference
     */
    Reference createReference();

    /**
     * Create a new service.
     * 
     * @return a new service
     */
    Service createService();

    /**
     * Create a new wire.
     * 
     * @return a new wire
     */
    Wire createWire();

    /**
     * Create an Extension
     * @return
     */
    Extension createExtension();

    /**
     * Create a new endpoint
     * 
     * @return a new endpoint
     */
    Endpoint createEndpoint();

    /**
     * Create a new endpoint reference
     * 
     * @return a new endpoint reference
     */
    EndpointReference createEndpointReference();

    /** 
     * Create a new configured operation.
     * 
     * @return a new ConfiguredOperation 
     */
    ConfiguredOperation createConfiguredOperation();
}
