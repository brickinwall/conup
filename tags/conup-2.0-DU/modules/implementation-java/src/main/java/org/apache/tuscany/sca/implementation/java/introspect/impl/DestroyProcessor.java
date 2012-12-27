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

import java.lang.reflect.Method;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.implementation.java.IntrospectionException;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.introspect.BaseJavaClassVisitor;
import org.oasisopen.sca.annotation.Destroy;

/**
 * Processes the {@link @Destroy} annotation on a component implementation and
 * updates the component type with the decorated destructor method
 * 
 * @version $Rev: 942716 $ $Date: 2010-05-10 13:02:56 +0100 (Mon, 10 May 2010) $
 */
public class DestroyProcessor extends BaseJavaClassVisitor {
    
    public DestroyProcessor(AssemblyFactory factory) {
        super(factory);
    }
    
    public DestroyProcessor(ExtensionPointRegistry registry) {
        super(registry);
    }    

    @Override
    public void visitMethod(Method method, JavaImplementation type) throws IntrospectionException {
        Destroy annotation = method.getAnnotation(Destroy.class);
        if (annotation == null) {
            return;
        }
        if (method.getParameterTypes().length != 0) {
            throw new IllegalDestructorException("[JCA90004] Destructor must not have arguments", method);
        }
        if(!method.getReturnType().equals(void.class)) {
            throw new IllegalDestructorException("Destructor must return void.", method);
        }
        if (type.getDestroyMethod() != null) {
            throw new DuplicateDestructorException("More than one destructor found on implementation");
        }
        type.setDestroyMethod(method);
    }
}
