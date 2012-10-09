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
package org.apache.tuscany.sca.implementation.java.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.apache.tuscany.sca.implementation.java.IntrospectionException;
import org.apache.tuscany.sca.implementation.java.JavaConstructorImpl;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaParameterImpl;
import org.apache.tuscany.sca.implementation.java.introspect.JavaClassVisitor;
import org.apache.tuscany.sca.implementation.java.introspect.JavaIntrospectionHelper;

/**
 * An extensible Java class introspector implementation.
 * 
 * @version $Rev: 957343 $ $Date: 2010-06-23 22:07:57 +0100 (Wed, 23 Jun 2010) $
 */
public class JavaClassIntrospectorImpl {
    
    private List<JavaClassVisitor> visitors;

    public JavaClassIntrospectorImpl(List<JavaClassVisitor> visitors) {
        this.visitors = visitors;
    }

    /**
     * JSR-250 PFD recommends the following guidelines for how annotations
     * interact with inheritance in order to keep the resulting complexity in
     * control:
     * <ol>
     * <li>Class-level annotations only affect the class they annotate and
     * their members, that is, its methods and fields. They never affect a
     * member declared by a superclass, even if it is not hidden or overridden
     * by the class in question.
     * <li>In addition to affecting the annotated class, class-level
     * annotations may act as a shorthand for member-level annotations. If a
     * member carries a specific member-level annotation, any annotations of the
     * same type implied by a class-level annotation are ignored. In other
     * words, explicit member-level annotations have priority over member-level
     * annotations implied by a class-level annotation.
     * <li>The interfaces implemented by a class never contribute annotations
     * to the class itself or any of its members.
     * <li>Members inherited from a superclass and which are not hidden or
     * overridden maintain the annotations they had in the class that declared
     * them, including member-level annotations implied by class-level ones.
     * <li>Member-level annotations on a hidden or overridden member are always
     * ignored.
     * </ol>
     */
    public void introspectClass(JavaImplementation type, Class<?> clazz)
        throws IntrospectionException {
        for (JavaClassVisitor visitor : visitors) {
            visitor.visitClass(clazz, type);
            for (Constructor<?> constructor : clazz.getConstructors()) {
                visitor.visitConstructor(constructor, type);
                // Assuming the visitClass or visitConstructor will populate the
                // type.getConstructors
                JavaConstructorImpl<?> definition = type.getConstructors().get(constructor);
                if (definition != null) {
                    for (JavaParameterImpl p : definition.getParameters()) {
                        visitor.visitConstructorParameter(p, type);
                    }
                }
            }

            Set<Field> fields = JavaIntrospectionHelper.getInjectableFields(clazz, true);
            for (Field field : fields) {
                visitor.visitField(field, type);
            }

            Set<Method> methods = JavaIntrospectionHelper.getAllUniquePublicProtectedMethods(clazz, true);
            for (Method method : methods) {
                visitor.visitMethod(method, type);
            }

            // Check if any private methods have illegal annotations that should be raised as errors
            Set<Method> privateMethods = JavaIntrospectionHelper.getPrivateMethods(clazz);
            for (Method method : privateMethods) {
                visitor.visitMethod(method, type);
            }

            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                visitSuperClass(superClass, type, visitor);
            }

            visitor.visitEnd(clazz, type);

        }

    }

    private void visitSuperClass(Class<?> clazz, JavaImplementation type, JavaClassVisitor visitor)
        throws IntrospectionException {
        if (!Object.class.equals(clazz)) {
            visitor.visitSuperClass(clazz, type);
            clazz = clazz.getSuperclass();
            if (clazz != null) {
                visitSuperClass(clazz, type, visitor);
            }
        }
    }

}
