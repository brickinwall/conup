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
import java.lang.reflect.Modifier;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.implementation.java.IntrospectionException;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.introspect.BaseJavaClassVisitor;
import org.oasisopen.sca.annotation.Init;

/**
 * Processes the {@link @Init} annotation on a component implementation and
 * updates the component type with the decorated initializer method
 * 
 * @version $Rev: 942716 $ $Date: 2010-05-10 13:02:56 +0100 (Mon, 10 May 2010) $
 */
public class InitProcessor extends BaseJavaClassVisitor {
    
    public InitProcessor(AssemblyFactory factory) {
        super(factory);
    }

    public InitProcessor(ExtensionPointRegistry registry) {
        super(registry);
    }    

    @Override
    public void visitMethod(Method method, JavaImplementation type) throws IntrospectionException {
        Init annotation = method.getAnnotation(Init.class);
        if (annotation == null) {
            return;
        }
        if (method.getParameterTypes().length != 0) {
            throw new IllegalInitException("[JCA90008] Initializer must not have argments", method);
        }
        if(!method.getReturnType().equals(void.class)) {
            throw new IllegalInitException("Initializer must return void.", method);
        }
        if (type.getInitMethod() != null) {
            throw new DuplicateInitException("More than one initializer found on implementaton");
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalInitException("Initializer must be a public method. Invalid annotation @Init found on "+method);
        }
        type.setInitMethod(method);
    }
}
