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
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Contract;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceContract;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;

/**
 * @version $Rev: 563019 $ $Date: 2007-08-06 04:43:59 +0100 (Mon, 06 Aug 2007) $
 */
public class ModelHelper {

    public static Property getProperty(JavaImplementation type, String name) {
        for (Property prop : type.getProperties()) {
            if (prop.getName().equals(name)) {
                return prop;
            }
        }
        return null;
    }

    public static Reference getReference(JavaImplementation type, String name) {
        for (Reference ref : type.getReferences()) {
            if (ref.getName().equals(name)) {
                return ref;
            }
        }
        return null;
    }

    public static Service getService(JavaImplementation type, String name) {
        for (Service svc : type.getServices()) {
            if (svc.getName().equals(name)) {
                return svc;
            }
        }
        return null;
    }

    public static boolean matches(Contract contract, Class<?> type) {
        Interface interface1 = contract.getInterfaceContract().getInterface();
        if (interface1 instanceof JavaInterface) {
            return type == ((JavaInterface)interface1).getJavaClass();
        } else {
            return false;
        }
    }

    public static ComponentService createService(AssemblyFactory factory,
                                                 JavaInterfaceFactory javaFactory, Class<?> type) {
        org.apache.tuscany.sca.assembly.ComponentService ref = factory.createComponentService();
        ref.setName(type.getSimpleName());
        JavaInterface i = javaFactory.createJavaInterface();
        i.setJavaClass(type);
        JavaInterfaceContract ic = javaFactory.createJavaInterfaceContract();
        ic.setInterface(i);
        ref.setInterfaceContract(ic);
        return ref;
    }

    public static Reference createReference(AssemblyFactory factory,
                                            JavaInterfaceFactory javaFactory, String name, Class<?> type) {
        org.apache.tuscany.sca.assembly.Reference ref = factory.createReference();
        ref.setName(name);
        JavaInterface i = javaFactory.createJavaInterface();
        i.setJavaClass(type);
        JavaInterfaceContract ic = javaFactory.createJavaInterfaceContract();
        ic.setInterface(i);
        ref.setInterfaceContract(ic);
        return ref;
    }

}
