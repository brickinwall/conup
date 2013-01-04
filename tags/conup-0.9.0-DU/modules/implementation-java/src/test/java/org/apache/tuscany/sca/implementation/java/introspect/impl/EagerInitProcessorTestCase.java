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

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.implementation.java.DefaultJavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.IntrospectionException;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaImplementationFactory;
import org.junit.Test;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * @version $Rev: 738490 $ $Date: 2009-01-28 14:07:54 +0000 (Wed, 28 Jan 2009) $
 */
public class EagerInitProcessorTestCase {

    private AssemblyFactory assemblyFactory = new DefaultAssemblyFactory();
    private JavaImplementationFactory javaImplementationFactory = new DefaultJavaImplementationFactory();
    
    @Test
    public void testNoLevel() throws IntrospectionException {
        EagerInitProcessor processor = new EagerInitProcessor(assemblyFactory);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitClass(Level.class, type);
    }

    @Test
    public void testSubclass() throws IntrospectionException {
        EagerInitProcessor processor = new EagerInitProcessor(assemblyFactory);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitClass(SubClass.class, type);
    }

    @EagerInit
    private class Level {
    }

    private class SubClass extends Level {

    }

}
