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
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.implementation.java.IntrospectionException;
import org.apache.tuscany.sca.implementation.java.JavaConstructorImpl;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaParameterImpl;
import org.apache.tuscany.sca.implementation.java.introspect.BaseJavaClassVisitor;

/**
 * Handles processing of a constructor decorated with
 * {@link org.oasisopen.sca.annotation.Constructor}
 * 
 * @version $Rev: 1174086 $ $Date: 2011-09-22 13:22:20 +0100 (Thu, 22 Sep 2011) $
 */
@SuppressWarnings("unchecked")
public class ConstructorProcessor extends BaseJavaClassVisitor {
    
    public ConstructorProcessor(AssemblyFactory factory) {
        super(factory);
    }
    
    public ConstructorProcessor(ExtensionPointRegistry registry) {
        super(registry);
    }

    @Override
    public <T> void visitClass(Class<T> clazz, JavaImplementation type) throws IntrospectionException {
        Constructor[] ctors = clazz.getConstructors();
        boolean found = false;
        for (Constructor constructor : ctors) {
            JavaConstructorImpl<?> definition = new JavaConstructorImpl(constructor);
            type.getConstructors().put(constructor, definition);
            if (constructor.getAnnotation(org.oasisopen.sca.annotation.Constructor.class) != null) {
                if (found) {
                    throw new DuplicateConstructorException("[JCI50002] Multiple constructors marked with @Constructor", constructor);
                }
                found = true;
                type.setConstructor(definition);
            }
        }
    }

    @Override
    public <T> void visitConstructor(Constructor<T> constructor, JavaImplementation type)
        throws IntrospectionException {
        org.oasisopen.sca.annotation.Constructor annotation = constructor
            .getAnnotation(org.oasisopen.sca.annotation.Constructor.class);
        if (annotation == null) {
            return;
        }
        JavaConstructorImpl<?> definition = type.getConstructor();
        if (definition == null) {
            definition = new JavaConstructorImpl(constructor);
            type.setConstructor(definition);
        }
        JavaParameterImpl[] parameters = definition.getParameters();
        
        for (JavaParameterImpl p : parameters) {
            if (!hasAnnotation(p)) {
                throw new InvalidConstructorException("JCA90003 constructor parameters for class " + type.getName() + " must have @Property or @Reference annotation");
            }
        }

        type.setConstructor(definition);
    }

    private boolean hasAnnotation(JavaParameterImpl p) {
        if (p.getAnnotations() != null && p.getAnnotations().length > 0) {
            return true;
        }
// TODO: need to verify JCA90003 as it seems like any annotation should be ok not just SCA ref or prop        
//        if (p.getAnnotation(Reference.class) != null) {
//            return true;
//        }
//        if (p.getAnnotation(Property.class) != null) {
//            return true;
//        }
        return false;
    }
}
