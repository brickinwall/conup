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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.implementation.java.DefaultJavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaImplementationFactory;
import org.apache.tuscany.sca.interfacedef.java.DefaultJavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.impl.PolicyJavaInterfaceVisitor;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicySubject;
import org.junit.Before;
import org.junit.Test;
import org.oasisopen.sca.annotation.Requires;
import org.oasisopen.sca.annotation.Service;

/**
 * @version $Rev: 827831 $ $Date: 2009-10-21 00:08:05 +0100 (Wed, 21 Oct 2009) $
 */
public class PolicyProcessorTestCase {
    private ServiceProcessor serviceProcessor;
    private PolicyProcessor policyProcessor;
    private PolicyJavaInterfaceVisitor visitor;
    private JavaImplementation type;

    // This actually is a test for PolicyJavaInterfaceProcessor. It will get
    // invoked via the call to ImplementationProcessorServiceImpl.createService in
    // ServiceProcessor. Of course ServiceProcessor class has to be working.
    @Test
    public void testSingleInterfaceWithIntentsOnInterfaceAtInterfaceLevel() throws Exception {
        serviceProcessor.visitClass(Service1.class, type);
        visitor.visitInterface((JavaInterface)type.getServices().get(0).getInterfaceContract().getInterface());
        policyProcessor.visitClass(Service1.class, type);
        verifyIntents(Service1.class, type);
    }

    @Test
    public void testMultipleInterfacesWithIntentsOnInterfaceAtInterfaceLevel() throws Exception {
        serviceProcessor.visitClass(Service2.class, type);
        visitor.visitInterface((JavaInterface)type.getServices().get(0).getInterfaceContract().getInterface());
        visitor.visitInterface((JavaInterface)type.getServices().get(1).getInterfaceContract().getInterface());
        policyProcessor.visitClass(Service2.class, type);
        verifyIntents(Service2.class, type);
    }

    @Test
    public void testSingleInterfaceWithIntentsOnImplAtClassLevel() throws Exception {
        serviceProcessor.visitClass(Service3.class, type);
        visitor.visitInterface((JavaInterface)type.getServices().get(0).getInterfaceContract().getInterface());
        policyProcessor.visitClass(Service3.class, type);
        verifyIntents(Service3.class, type);
    }

    @Test
    public void testMultipleInterfacesWithIntentsOnImplAtClassLevel() throws Exception {
        serviceProcessor.visitClass(Service4.class, type);
        visitor.visitInterface((JavaInterface)type.getServices().get(0).getInterfaceContract().getInterface());
        policyProcessor.visitClass(Service4.class, type);
        verifyIntents(Service4.class, type);
    }

    public void stestSingleInterfaceWithIntentsOnInterfaceAtMethodLevel() throws Exception {
        serviceProcessor.visitClass(Service5.class, type);
        visitor.visitInterface((JavaInterface)type.getServices().get(0).getInterfaceContract().getInterface());
        policyProcessor.visitClass(Service5.class, type);
        verifyIntents(Service5.class, type);
    }

    @Test
    public void testSingleInterfaceWithIntentsOnServiceAndInterfaceAtImplAndInertfaceAndMethodLevel() throws Exception {
        serviceProcessor.visitClass(Service6.class, type);
        visitor.visitInterface((JavaInterface)type.getServices().get(0).getInterfaceContract().getInterface());
        policyProcessor.visitClass(Service6.class, type);
        for (Method method : Service6.class.getDeclaredMethods()) {
            policyProcessor.visitMethod(method, type);
        }
        verifyIntents(Service6.class, type);
    }

    private void verifyIntents(Class<?> serviceImplClass, JavaImplementation type) {
        if ( !(type instanceof PolicySubject) ) {
            fail("No Intents on the service ");
        }
        Requires serviceImplIntentAnnotation = (Requires)serviceImplClass.getAnnotation(Requires.class);
        if (serviceImplIntentAnnotation != null) {
            String[] serviceImplIntents = serviceImplIntentAnnotation.value();
            List<Intent> requiredIntents = ((PolicySubject)type).getRequiredIntents();
            if (serviceImplIntents.length > 0) {
                if (requiredIntents == null || requiredIntents.size() == 0) {
                    fail("No Intents on the service ");
                }
                Map<String, Intent> intentMap = new HashMap<String, Intent>();
                for (Intent intent : requiredIntents) {
                    intentMap.put(intent.getName().getLocalPart(), intent);
                }
                for (String intent : serviceImplIntents) {
                    assertTrue("ComponentType for Service class " + serviceImplClass.getName()
                        + " did not contain Service Implementation intent "
                        + intent, intentMap.containsKey(intent));
                }
            }
        }

        // This should match what was specified on @Service for a Service Implementation
        // If we use these to get the Service names and we get a null Service
        // name then it would seem that wrong values were put on the @Service annotation
        // or the wrong interfaces were specified on the implements list of the class
        // statement?
        Map<String, org.apache.tuscany.sca.assembly.Service> serviceMap = new HashMap<String, org.apache.tuscany.sca.assembly.Service>();
        for (org.apache.tuscany.sca.assembly.Service service: type.getServices()) {
            serviceMap.put(service.getName(), service);
        }
        for (Class<?> interfaceClass : serviceImplClass.getInterfaces()) {
            Requires interfaceIntentAnnotation = (Requires)interfaceClass.getAnnotation(Requires.class);
            org.apache.tuscany.sca.assembly.Service service = serviceMap.get(interfaceClass.getSimpleName());
            if (service == null) {
                fail("No service defined for interface " + interfaceClass.getSimpleName()
                    + " on Service Implementation "
                    + serviceImplClass.getName());
            }

            if (interfaceIntentAnnotation != null) {
                String[] interfaceIntents = interfaceIntentAnnotation.value();
                List<Intent> requiredIntents = service.getInterfaceContract().getInterface().getRequiredIntents();
                if (interfaceIntents.length > 0) {
                    if (requiredIntents == null || requiredIntents.size() == 0) {
                        fail("No Intents on the service " + service.getName());
                    }
                    Map<String, Intent> intentMap = new HashMap<String, Intent>();
                    for (Intent intent : requiredIntents) {
                        intentMap.put(intent.getName().getLocalPart(), intent);
                    }
                    for (String intent : interfaceIntents) {
                        assertTrue("Interface " + service.getName()
                            + " did not contain Service Interface intent "
                            + intent, intentMap.containsKey(intent));
                    }
                }
            }

            /*
            for (Method method : interfaceClass.getDeclaredMethods()) {
                Requires methodIntentAnnotation = method.getAnnotation(Requires.class);

                // Verify that each of the Intents on each of the Service
                // Interface Methods exist on their associated operation.
                if (methodIntentAnnotation != null) {
                    String[] methodIntents = methodIntentAnnotation.value();
                    if (methodIntents.length > 0) {
                        List<Intent> requiredIntents = null;
                        for ( ConfiguredOperation confOp : service.getConfiguredOperations() ) {
                            if ( confOp.getName().equals(method.getName()) &&
                                    confOp.getContractName().equals(service.getName()) ) {
                                requiredIntents = confOp.getRequiredIntents();
                            }
                        }
                        
                        if (requiredIntents == null || requiredIntents.size() == 0) {
                            fail("No Intents on operation " + method.getName());
                        }
                        for (String intent : methodIntents) {
                            boolean found = false;
                            for (Intent requiredIntent: requiredIntents) {
                                if (requiredIntent.getName().getLocalPart().equals(intent)) {
                                    found = true;
                                    break;
                                }
                            }
                            assertTrue("Operation " + method.getName()
                                + " did not contain Service Interface method intent "
                                + intent, found);
                        }
                    }
                }
            }
            
            for (Method method : serviceImplClass.getDeclaredMethods()) {
                Requires methodIntentAnnotation = method.getAnnotation(Requires.class);

                // Verify that each of the Intents on each of the Service
                // Implementation Methods exist on their associated
                // operation.
                if (methodIntentAnnotation != null) {
                    String[] methodIntents = methodIntentAnnotation.value();
                    if (methodIntents.length > 0) {
                        List<Intent> requiredIntents = null;
                        for ( ConfiguredOperation confOp : ((OperationsConfigurator)type).getConfiguredOperations() ) {
                            if ( confOp.getName().equals(method.getName())  ) {
                                requiredIntents = confOp.getRequiredIntents();
                            }
                        }
                        
                        if (requiredIntents == null || requiredIntents.size() == 0) {
                            fail("No Intents on operation " + method.getName());
                        }
                        
                        for (String intent : methodIntents) {
                            boolean found = false;
                            for (Intent requiredIntent: requiredIntents) {
                                if (requiredIntent.getName().getLocalPart().equals(intent)) {
                                    found = true;
                                    break;
                                }
                            }
                            assertTrue("Operation " + method.getName()
                                + " did not contain Implementation method intent "
                                + intent, found);
                        }
                    }
                }
            }
            */
        }
    }

    @Before
    public void setUp() throws Exception {
        ExtensionPointRegistry registry = new DefaultExtensionPointRegistry();
        serviceProcessor = new ServiceProcessor(new DefaultAssemblyFactory(), new DefaultJavaInterfaceFactory(registry));
        policyProcessor = new PolicyProcessor(registry);
        visitor = new PolicyJavaInterfaceVisitor(registry);
        JavaImplementationFactory javaImplementationFactory = new DefaultJavaImplementationFactory();
        type = javaImplementationFactory.createJavaImplementation();
    }

    // @Remotable
    @Requires( {"transaction.global"})
    private interface Interface1 {
        int method1();

        int method2();

        int method3();

        int method4();
    }

    @Service(Interface1.class)
    private class Service1 implements Interface1 {
        public int method1() {
            return 0;
        }

        public int method2() {
            return 0;
        }

        public int method3() {
            return 0;
        }

        public int method4() {
            return 0;
        }
    }

    // @Remotable
    @Requires( {"transaction.local"})
    private interface Interface2 {
        int method5();

        int method6();
    }

    @Service({Interface1.class, Interface2.class})
    private class Service2 implements Interface1, Interface2 {
        public int method1() {
            return 0;
        }

        public int method2() {
            return 0;
        }

        public int method3() {
            return 0;
        }

        public int method4() {
            return 0;
        }

        public int method5() {
            return 0;
        }

        public int method6() {
            return 0;
        }
    }

    // @Remotable
    private interface Interface3 {
        int method1();

        int method2();

        int method3();

        int method4();
    }

    @Service(Interface3.class)
    @Requires( {"transaction.global"})
    private class Service3 implements Interface3 {
        public int method1() {
            return 0;
        }

        public int method2() {
            return 0;
        }

        public int method3() {
            return 0;
        }

        public int method4() {
            return 0;
        }
    }

    // @Remotable
    private interface Interface4 {
        int method5();

        int method6();
    }

    @Service({Interface3.class, Interface4.class})
    @Requires( {"transaction.local"})
    private class Service4 implements Interface3, Interface4 {
        public int method1() {
            return 0;
        }

        public int method2() {
            return 0;
        }

        public int method3() {
            return 0;
        }

        public int method4() {
            return 0;
        }

        public int method5() {
            return 0;
        }

        public int method6() {
            return 0;
        }
    }

    private interface Interface5 {
        @Requires( {"transaction.global"})
        int method1();

        @Requires( {"transaction.local"})
        int method2();
    }

    @Service(Interface5.class)
    private class Service5 implements Interface5 {
        public int method1() {
            return 0;
        }

        public int method2() {
            return 0;
        }
    }

    @Requires( {"transaction.global.Interface6"})
    private interface Interface6 {
        @Requires( {"transaction.global.Interface6.method1"})
        int method1();

        @Requires( {"transaction.local.Interface6.method2"})
        int method2();
    }

    @Service(Interface6.class)
    @Requires( {"transaction.global.Service6"})
    private class Service6 implements Interface6 {
        // @Requires( {"transaction.global.Service6.method1"})
        public int method1() {
            return 0;
        }

        // @Requires( {"transaction.global.Service6.method1"})
        public int method2() {
            return 0;
        }
    }

}
