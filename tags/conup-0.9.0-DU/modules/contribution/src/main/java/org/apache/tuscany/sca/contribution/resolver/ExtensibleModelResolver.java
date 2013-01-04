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

package org.apache.tuscany.sca.contribution.resolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;

/**
 * An implementation of an extensible model resolver which delegates to the
 * proper resolver extension based on the class of the model to resolve.
 * 
 * @version $Rev: 1209147 $ $Date: 2011-12-01 16:51:18 +0000 (Thu, 01 Dec 2011) $
 */
public class ExtensibleModelResolver implements ModelResolver {
    private final ModelResolverExtensionPoint resolverExtensions;
    private final FactoryExtensionPoint modelFactories;
    private final Contribution contribution;
    private ModelResolver defaultResolver;
    private final Map<Class<?>, ModelResolver> resolversByModelType = new HashMap<Class<?>, ModelResolver>();
    private final Map<Class<?>, ModelResolver> resolversByImplementationClass = new HashMap<Class<?>, ModelResolver>();
    private Map<Object, Object> map = new HashMap<Object, Object>();
    private Object lastUnresolved;

    /**
     * Constructs an extensible model resolver
     * 
     * @param resolverExtensions
     * @param contribution
     * @param modelFactories
     */
    public ExtensibleModelResolver(Contribution contribution,
                                   ModelResolverExtensionPoint resolverExtensions,
                                   FactoryExtensionPoint modelFactories) {
        this.contribution = contribution;
        this.resolverExtensions = resolverExtensions;
        this.modelFactories = modelFactories;
    }

    /**
     * Returns the proper resolver instance based on the interfaces of the model
     * If one is not available on the registry, instantiate on demand
     * 
     * @param modelType
     * @return
     */
    public ModelResolver getModelResolverInstance(Class<?> modelType) {
        // Look up a model resolver instance for the model class or
        // each implemented interface
        Class<?>[] interfaces = modelType.getInterfaces();
        Class<?>[] classes = new Class<?>[interfaces.length + 1];
        classes[0] = modelType;
        if (interfaces.length != 0) {
            System.arraycopy(interfaces, 0, classes, 1, interfaces.length);
        }
        for (Class<?> c : classes) {

            // Look up an existing model resolver instance
            ModelResolver resolverInstance = resolversByModelType.get(c);
            if (resolverInstance != null) {
                return resolverInstance;
            }

            // We don't have an instance, lookup a model resolver class
            // and instantiate it
            Class<? extends ModelResolver> resolverClass = resolverExtensions.getResolver(c);
            if (resolverClass != null) {

                // Construct the model resolver instance and cache it
                resolverInstance = resolversByImplementationClass.get(resolverClass);
                if (resolverInstance != null) {
                    resolversByModelType.put(c, resolverInstance);
                    return resolverInstance;
                }
                try {
                    final Constructor<? extends ModelResolver> constructor =
                        resolverClass
                            .getConstructor(new Class[] {Contribution.class, FactoryExtensionPoint.class});
                    if (constructor != null) {

                        resolverInstance = AccessController.doPrivileged(new PrivilegedExceptionAction<ModelResolver>() {
                            public ModelResolver run() throws Exception {
                                ModelResolver resolverInstance = constructor.newInstance(contribution, modelFactories);
                                return resolverInstance;
                            }
                        });
                        
                        resolversByImplementationClass.put(resolverClass, resolverInstance);
                        resolversByModelType.put(c, resolverInstance);
                        return resolverInstance;
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        return null;
    }

    public void addModel(Object resolved, ProcessorContext context) {
        ModelResolver resolver = getModelResolverInstance(resolved.getClass());
        if (resolver != null) {
            resolver.addModel(resolved, context);
        } else {
            map.put(resolved, resolved);
        }
    }

    public Object removeModel(Object resolved, ProcessorContext context) {
        ModelResolver resolver = getModelResolverInstance(resolved.getClass());
        if (resolver != null) {
            return resolver.removeModel(resolved, context);
        } else {
            return map.remove(resolved);
        }
    }

    public <T> T resolveModel(Class<T> modelClass, T unresolved, ProcessorContext context) {
        // Protect against dependency cycles causing infinite recursion
        // Save the current unresolved object and check later if we are trying
        // to resolve the same object again
        if (unresolved == lastUnresolved) {
            return unresolved;
        }
        lastUnresolved = unresolved;
        
        ModelResolver resolver = getModelResolverInstance(unresolved.getClass());
        if (resolver != null) {
            Object resolved = resolver.resolveModel(modelClass, unresolved, context);
            if (resolved != null && resolved != unresolved) {
                lastUnresolved = null;
                return modelClass.cast(resolved);
            }
        } else {
            //FIXME Remove this default resolver, this is currently used to resolve policy declarations
            // but they should be handled by the contribution import/export mechanism instead of this
            // defaultResolver hack.
            if (defaultResolver != null) {
                Object resolved = defaultResolver.resolveModel(modelClass, unresolved, context);
                if (resolved != null && resolved != unresolved) {
                    lastUnresolved = null;
                    return modelClass.cast(resolved);
                }
            }
            
            Object resolved = map.get(unresolved);
            if (resolved != null) {
                // Return the resolved object
                lastUnresolved = null;
                return modelClass.cast(resolved);
            }
        }

        return unresolved;
    }
    
    // FIXME: TUSCANY-2499: temporarily  give access to the defaultResolver to get the jms binding
    //  use of definitions.xml working while the definitions.xml processing is being refactored
    public ModelResolver getDefaultModelResolver() {
        return defaultResolver;
    }

}
