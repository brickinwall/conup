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

import java.lang.reflect.Constructor;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.implementation.java.IntrospectionException;
import org.apache.tuscany.sca.implementation.java.JavaConstructorImpl;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaParameterImpl;
import org.apache.tuscany.sca.interfacedef.java.DefaultJavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;


/**
 * Base class to simulate the processor sequences
 * 
 * @version $Rev: 826907 $ $Date: 2009-10-20 01:17:14 +0100 (Tue, 20 Oct 2009) $
 */
public abstract class AbstractProcessorTest {
    protected AssemblyFactory factory;
    protected JavaInterfaceFactory javaFactory;
    protected ConstructorProcessor constructorProcessor;
    protected ReferenceProcessor referenceProcessor;
    protected PropertyProcessor propertyProcessor;
    private ResourceProcessor resourceProcessor;


    protected AbstractProcessorTest() {
        ExtensionPointRegistry registry = new DefaultExtensionPointRegistry();
        factory = new DefaultAssemblyFactory(registry);
        javaFactory = new DefaultJavaInterfaceFactory(registry);
        referenceProcessor = new ReferenceProcessor(factory, javaFactory);
        propertyProcessor = new PropertyProcessor(registry);
        resourceProcessor = new ResourceProcessor(factory);
        constructorProcessor = new ConstructorProcessor(factory);
        referenceProcessor = new ReferenceProcessor(factory, javaFactory);
        propertyProcessor = new PropertyProcessor(registry);
    }

    protected <T> void visitConstructor(Constructor<T> constructor,
                                        JavaImplementation type) throws IntrospectionException {
        constructorProcessor.visitConstructor(constructor, type);
        JavaConstructorImpl<?> definition = type.getConstructor();
        if (definition == null) {
            definition = new JavaConstructorImpl<T>(constructor);
            type.getConstructors().put(constructor, definition);
        }
        
        JavaParameterImpl[] parameters = definition.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            referenceProcessor.visitConstructorParameter(parameters[i], type);
            propertyProcessor.visitConstructorParameter(parameters[i], type);
            resourceProcessor.visitConstructorParameter(parameters[i], type);
            // monitorProcessor.visitConstructorParameter(parameters[i], type);
        }
    }
    
 
}
