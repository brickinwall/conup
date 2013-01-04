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
package org.apache.tuscany.sca.implementation.java.introspect.impl;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.oasisopen.sca.annotation.Property;

/**
 * Processes an {@link @Property} annotation, updating the component type with
 * corresponding {@link JavaMappedProperty}
 * 
 * @version $Rev: 826907 $ $Date: 2009-10-20 01:17:14 +0100 (Tue, 20 Oct 2009) $
 */
public class PropertyProcessor extends AbstractPropertyProcessor<Property> {

    public PropertyProcessor(ExtensionPointRegistry registry) {
        super(registry, Property.class);
    }
    
    @Override
    protected String getName(Property annotation) {
        return annotation.name();
    }

    @Override
    protected void initProperty(org.apache.tuscany.sca.assembly.Property property, Property annotation) {
        property.setMustSupply(annotation.required());
    }

    @Override
    protected boolean getRequired(Property annotation) {
        return annotation.required();
    }
}
