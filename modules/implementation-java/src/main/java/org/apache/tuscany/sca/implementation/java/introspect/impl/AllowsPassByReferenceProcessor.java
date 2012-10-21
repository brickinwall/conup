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
import org.oasisopen.sca.annotation.AllowsPassByReference;

/**
 * Processes {@link AllowsPassByReference} on an implementation
 * 
 * @version $Rev: 826907 $ $Date: 2009-10-20 01:17:14 +0100 (Tue, 20 Oct 2009) $
 */
public class AllowsPassByReferenceProcessor extends BaseJavaClassVisitor {
    
    public AllowsPassByReferenceProcessor(AssemblyFactory factory) {
        super(factory);
    }

    public AllowsPassByReferenceProcessor(ExtensionPointRegistry registry) {
        super(registry);
    }
                                          
    @Override
    public <T> void visitClass(Class<T> clazz, JavaImplementation type) throws IntrospectionException {
        type.setAllowsPassByReference(clazz.isAnnotationPresent(AllowsPassByReference.class));
    }

    @Override
    public void visitMethod(Method method, JavaImplementation type) throws IntrospectionException {
        boolean pbr = method.isAnnotationPresent(AllowsPassByReference.class);
        if (pbr) {
            type.getAllowsPassByReferenceMethods().add(method);
        }
    }
}
