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

package org.apache.tuscany.sca.web.javascript;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.extensibility.ServiceDeclaration;
import org.apache.tuscany.sca.extensibility.ServiceDiscovery;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.MonitorFactory;

/**
 * Default extension point for javascript proxy factories
 * 
 * @version $Rev: 916838 $ $Date: 2010-02-26 22:09:55 +0000 (Fri, 26 Feb 2010) $
 */
public class DefaultJavascriptProxyFactoryExtensionPoint implements JavascriptProxyFactoryExtensionPoint {
    private final Map<QName, JavascriptProxyFactory> factoriesByQName = new HashMap<QName, JavascriptProxyFactory>();
    private final Map<Class<?>, JavascriptProxyFactory> factoriesByType = new HashMap<Class<?>, JavascriptProxyFactory>();
    
    private Monitor monitor = null;
    private ExtensionPointRegistry registry;
    private boolean loaded = false;
    
    public DefaultJavascriptProxyFactoryExtensionPoint(ExtensionPointRegistry extensionPoints) {
        this.registry = extensionPoints;
        UtilityExtensionPoint utilities = extensionPoints.getExtensionPoint(UtilityExtensionPoint.class);
        MonitorFactory monitorFactory = utilities.getUtility(MonitorFactory.class);
        if (monitorFactory != null) {
                this.monitor = monitorFactory.createMonitor();
        }
    }

    /**
     * Report a exception.
     * 
     * @param problems
     * @param message
     * @param model
    */
    private void error(String message, Object model, Exception ex) {
        /*
        if (monitor != null) {
            Problem problem =
                monitor.createProblem(this.getClass().getName(),
                                      Messages.RESOURCE_BUNDLE,
                                      Severity.WARNING,
                                      model,
                                      message,
                                      (Object[])messageParameters);
            monitor.problem(problem);
        } 
        */     
    }
    
    public void addProxyFactory(JavascriptProxyFactory javascriptProxyfactory) {
        if (javascriptProxyfactory.getModelType() != null) {
            factoriesByType.put(javascriptProxyfactory.getModelType(), javascriptProxyfactory);
        }
        if (javascriptProxyfactory.getQName() != null) {
            factoriesByQName.put(javascriptProxyfactory.getQName(), javascriptProxyfactory);
        }
    }

    public void removeProxyFactory(JavascriptProxyFactory javascriptProxyfactory) {
        if (javascriptProxyfactory.getModelType() != null) {
            factoriesByType.remove(javascriptProxyfactory.getModelType());
        }
        if (javascriptProxyfactory.getQName() != null) {
            factoriesByQName.remove(javascriptProxyfactory.getQName());
        }
    }

    public JavascriptProxyFactory getProxyFactory(QName bindingName) {
        loadFactories();
        return factoriesByQName.get(bindingName);
    }

    public JavascriptProxyFactory getProxyFactory(Class<?> bindingType) {
        loadFactories();
        Class<?>[] classes = bindingType.getInterfaces();
        for (Class<?> c : classes) {
            JavascriptProxyFactory proxyFactory = factoriesByType.get(c);
            if (proxyFactory != null) {
                return proxyFactory;
            }
        }

        //here we didn't find the proxy factory for the biding
        JavascriptProxyFactory factory = null;
        if (bindingType.isInterface()) {
            // Dynamically load a factory class declared under META-INF/services 
            try {
                Class<?> factoryClass = registry.getServiceDiscovery().getServiceDeclaration(bindingType).getClass();
                if (factoryClass != null) {

                    try {
                        // Default empty constructor
                        Constructor<?> constructor = factoryClass.getConstructor();
                        factory = (JavascriptProxyFactory) constructor.newInstance();
                    } catch (NoSuchMethodException e) {

                        // Constructor taking the model factory extension point
                        Constructor<?> constructor = factoryClass.getConstructor(FactoryExtensionPoint.class);
                        factory = (JavascriptProxyFactory) constructor.newInstance(this);
                    }

                    // Cache the loaded factory
                    addProxyFactory(factory);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        } else {

            // Call the newInstance static method on the factory abstract class
            try {
                factory = (JavascriptProxyFactory) ServiceDiscovery.getInstance().getServiceDeclaration(bindingType);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }

            // Cache the factory
            addProxyFactory(factory);
        }
    
        return factory;
    }
    
    /**
     * Private Utility methods
     */
    
    /**
     * Returns a QName object from a QName expressed as {ns}name
     * or ns#name.
     * 
     * @param qname
     * @return
     */
    private static QName getQName(String qname) {
        if (qname == null) {
            return null;
        }
        qname = qname.trim();
        if (qname.startsWith("{")) {
            int h = qname.indexOf('}');
            if (h != -1) {
                return new QName(qname.substring(1, h), qname.substring(h + 1));
            }
        } else {
            int h = qname.indexOf('#');
            if (h != -1) {
                return new QName(qname.substring(0, h), qname.substring(h + 1));
            }
        }
        throw new IllegalArgumentException("Invalid qname: "+qname);
    }    
    
    /**
     * Lazily load artifact processors registered in the extension point.
     */
    private synchronized void loadFactories() {
        if (loaded) {
            return;
        }

        // Get the proxy factories declarations
        Collection<ServiceDeclaration> factoryDeclarations = null;
        try {
            factoryDeclarations = ServiceDiscovery.getInstance().getServiceDeclarations(JavascriptProxyFactory.class);
        } catch (IOException e) {
            IllegalStateException ie = new IllegalStateException(e);
            error("IllegalStateException", factoryDeclarations, ie);
            throw ie;
        }

        for (ServiceDeclaration processorDeclaration : factoryDeclarations) {
            Map<String, String> attributes = processorDeclaration.getAttributes();

            // Load a StAX artifact processor

            // Get the model QName
            QName artifactType = getQName(attributes.get("qname"));

            // Get the model class name
            String modelTypeName = attributes.get("model");

            // Create a factory, and register it
            JavascriptProxyFactory proxyFactory = null;
            try {
                proxyFactory = (JavascriptProxyFactory) processorDeclaration.loadClass().newInstance();
            } catch (Exception e) {
                IllegalStateException ie = new IllegalStateException(e);
                error("IllegalStateException", proxyFactory, ie);
                throw ie;
            }

            addProxyFactory(proxyFactory);
        }

        loaded = true;
    }

}
