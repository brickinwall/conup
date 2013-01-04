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

package org.apache.tuscany.sca.core.assembly.impl;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.context.CompositeContext;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.core.scope.ScopeContainer;
import org.apache.tuscany.sca.core.scope.ScopeRegistry;
import org.apache.tuscany.sca.core.scope.ScopedRuntimeComponent;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.MonitorFactory;
import org.apache.tuscany.sca.provider.EndpointReferenceAsyncProvider;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.provider.ImplementationProviderFactory;
import org.apache.tuscany.sca.provider.PolicyProvider;
import org.apache.tuscany.sca.provider.PolicyProviderFactory;
import org.apache.tuscany.sca.provider.ProviderFactoryExtensionPoint;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.provider.RuntimeProvider;
import org.apache.tuscany.sca.provider.ServiceBindingProvider;
import org.apache.tuscany.sca.runtime.ActivationException;
import org.apache.tuscany.sca.runtime.CompositeActivator;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;

/**
 * @version $Rev: 1178311 $ $Date: 2011-10-03 08:17:31 +0100 (Mon, 03 Oct 2011) $
 */
public class CompositeActivatorImpl implements CompositeActivator {
    final Logger logger = Logger.getLogger(CompositeActivatorImpl.class.getName());

    private final ScopeRegistry scopeRegistry;
    private final ProviderFactoryExtensionPoint providerFactories;
	private Monitor monitor;

    public CompositeActivatorImpl(ExtensionPointRegistry extensionPoints) {
        UtilityExtensionPoint utilities = extensionPoints.getExtensionPoint(UtilityExtensionPoint.class);
        this.scopeRegistry = utilities.getUtility(ScopeRegistry.class);
        this.providerFactories = extensionPoints.getExtensionPoint(ProviderFactoryExtensionPoint.class);
        this.monitor = utilities.getUtility(MonitorFactory.class).createMonitor();
    }

    //=========================================================================
    // Activation
    //=========================================================================

    // Composite activation/deactivation

    public void activate(CompositeContext compositeContext, Composite composite) throws ActivationException {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Activating composite: " + composite.getName());
            }
            for (Component component : composite.getComponents()) {
                activateComponent(compositeContext, component);
            }
        } catch (Exception e) {
            throw new ActivationException(e);
        }
    }

    public void deactivate(Composite composite) throws ActivationException {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Deactivating composite: " + composite.getName());
            }
            for (Component component : composite.getComponents()) {
                deactivateComponent(component);
            }
        } catch (Exception e) {
            throw new ActivationException(e);
        }
    }

    // Component activation/deactivation

    public void activateComponent(CompositeContext compositeContext, Component component)
            throws ActivationException {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Activating component: " + component.getURI());
            }

            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {
                activate(compositeContext, (Composite) implementation);
            } else if (implementation != null) {
                addImplementationProvider((RuntimeComponent) component,
                        implementation);
                addScopeContainer(component);
            }

            for (ComponentService service : component.getServices()) {
                activate(compositeContext,
                        (RuntimeComponent) component, (RuntimeComponentService) service);
            }

            for (ComponentReference reference : component.getReferences()) {
                activate(compositeContext,
                        (RuntimeComponent) component, (RuntimeComponentReference) reference);
            }
        } catch (Exception e) {
            throw new ActivationException(e);
        }
    }

    public void deactivateComponent(Component component)
            throws ActivationException {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Deactivating component: " + component.getURI());
            }
            for (ComponentService service : component.getServices()) {
                deactivate((RuntimeComponent) component,
                        (RuntimeComponentService) service);
            }

            for (ComponentReference reference : component.getReferences()) {
                deactivate((RuntimeComponent) component,
                        (RuntimeComponentReference) reference);
            }

            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {
                deactivate((Composite) implementation);
            } else if (implementation != null) {
                removeImplementationProvider((RuntimeComponent) component);
                removeScopeContainer(component);
            }
        } catch (Exception e) {
            throw new ActivationException(e);
        }
    }

    // add/remove artifacts required to get the implementation going

    private void addImplementationProvider(RuntimeComponent component, Implementation implementation) {
        ImplementationProviderFactory providerFactory =
            (ImplementationProviderFactory)providerFactories.getProviderFactory(implementation.getClass());
        if (providerFactory != null) {
            @SuppressWarnings("unchecked")
            ImplementationProvider implementationProvider =
                providerFactory.createImplementationProvider(component, implementation);
            if (implementationProvider != null) {
                component.setImplementationProvider(implementationProvider);
            }
        } else {
            throw new IllegalStateException("Provider factory not found for class: " + implementation.getClass()
                .getName());
        }
        for (PolicyProviderFactory f : providerFactories.getPolicyProviderFactories()) {
            PolicyProvider policyProvider = f.createImplementationPolicyProvider(component);
            if (policyProvider != null) {
                component.addPolicyProvider(policyProvider);
            }
        }

    }

    private void removeImplementationProvider(RuntimeComponent component) {
        component.setImplementationProvider(null);
        component.getPolicyProviders().clear();
    }

    private void addScopeContainer(Component component) {
        if (!(component instanceof ScopedRuntimeComponent)) {
            return;
        }
        ScopedRuntimeComponent runtimeComponent = (ScopedRuntimeComponent)component;
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(runtimeComponent);
        runtimeComponent.setScopeContainer(scopeContainer);
    }

    private void removeScopeContainer(Component component) {
        if (!(component instanceof ScopedRuntimeComponent)) {
            return;
        }
        ScopedRuntimeComponent runtimeComponent = (ScopedRuntimeComponent)component;
        ScopeContainer scopeContainer = runtimeComponent.getScopeContainer();
        runtimeComponent.setScopeContainer(null);
    }


    // Service activation/deactivation

    public void activate(CompositeContext compositeContext, RuntimeComponent component, RuntimeComponentService service) {
        if (service.getService() == null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Skipping component service not defined in the component type: " + component.getURI()
                    + "#"
                    + service.getName());
            }
            return;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Activating component service: " + component.getURI() + "#" + service.getName());
        }

        // Add a wire for each service Endpoint
        for ( Endpoint endpoint : service.getEndpoints()){
            RuntimeEndpoint ep = (RuntimeEndpoint) endpoint;
            activate(compositeContext, ep);

            // create the interface contract for the binding and service ends of the wire
            // that are created as forward only contracts
            // FIXME: [rfeng] We might need a better way to get the impl interface contract
            Service targetService = service.getService();
            if (targetService == null) {
                targetService = service;
            }
            // endpoint.setInterfaceContract(targetService.getInterfaceContract().makeUnidirectional(false));
        }
    }

    public void activate(CompositeContext compositeContext, RuntimeEndpoint ep) {
        ep.bind(compositeContext);
        
        // Check that the service binding interface is compatible with the 
        // service interface
        ep.validateServiceInterfaceCompatibility();
    }

    public void deactivate(RuntimeComponent component, RuntimeComponentService service) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Deactivating component service: " + component.getURI() + "#" + service.getName());
        }
        for(Endpoint ep: service.getEndpoints()) {
            if(ep instanceof RuntimeEndpoint) {
                deactivate((RuntimeEndpoint) ep);
            }
        }
    }

    public void deactivate(RuntimeEndpoint ep) {
        ep.unbind();
    }

    // Reference activation/deactivation

    public void activate(CompositeContext compositeContext, RuntimeComponent component, RuntimeComponentReference reference) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Activating component reference: " + component.getURI() + "#" + reference.getName());
        }

        // set the parent component onto the reference. It's used at start time when the
        // reference is asked to return it's runtime wires. If there are none the reference
        // asks the component context to start the reference which creates the wires
        reference.setComponent(component);
        for(EndpointReference epr: reference.getEndpointReferences()) {
            activate(compositeContext, (RuntimeEndpointReference) epr);
        }

    }

    public void deactivate(RuntimeComponent component, RuntimeComponentReference reference) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Deactivating component reference: " + component.getURI() + "#" + reference.getName());
        }
        for(EndpointReference endpointReference: reference.getEndpointReferences()) {
            deactivate((RuntimeEndpointReference)endpointReference);
        }
    }
    
    public void activate(CompositeContext compositeContext, RuntimeEndpointReference epr) {
        // create the wire
        // null endpoint passed in here as the endpoint reference may
        // not be resolved yet
        epr.bind(compositeContext);

        ComponentReference reference = epr.getReference(); 
        InterfaceContract sourceContract = epr.getComponentTypeReferenceInterfaceContract();

        // TODO - EPR - interface contract seems to be null in the implementation.web
        //              case. Not introspecting the CT properly?
        if (sourceContract == null){
            // TODO - Can't do this with move of matching to wire
            // take the contract from the service to which the reference is connected
            sourceContract = ((RuntimeEndpoint) epr.getTargetEndpoint()).getComponentTypeServiceInterfaceContract();
            reference.setInterfaceContract(sourceContract);
        }

        // endpointReference.setInterfaceContract(sourceContract.makeUnidirectional(false));
        
        // if the reference already has a binding we can check the reference binding interface
        // and reference interfaces for compatibility. If we can't check now compatibility 
        // will be checked when the endpoint reference is resolved. 
        if (epr.getStatus() == EndpointReference.Status.RESOLVED_BINDING){
            epr.validateReferenceInterfaceCompatibility();
        }
    }    

    public void deactivate(RuntimeEndpointReference endpointReference) {
        endpointReference.unbind();
    }

    //=========================================================================
    // Start
    //=========================================================================

    // Composite start/stop

    public void start(CompositeContext compositeContext, Composite composite) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Starting composite: " + composite.getName());
        }
        for (Component component : composite.getComponents()) {
            start(compositeContext, component);
        }
        
        for (Component component : composite.getComponents()) {
            if (component instanceof ScopedRuntimeComponent) {
                start(compositeContext, (ScopedRuntimeComponent)component);
            }
        }
        
        // start reference last. In allowing references to start at "start" time
        // as well as when they are first used (for late bound references) we need
        // to make sure that all potential target services and component implementations 
        // are started first to take account of the default binding optimization case
        for (Component component : composite.getComponents()) {
            for (ComponentReference reference : component.getReferences()) {        
                start(compositeContext, 
                      (RuntimeComponent)component, 
                      (RuntimeComponentReference)reference);
            }
        }
    }

    public void stop(CompositeContext compositeContext, Composite composite) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Stopping composite: " + composite.getName());
        }
        for (final Component component : composite.getComponents()) {
            stop(compositeContext, component);
        }
    }

    // Component start/stop

    public void start(CompositeContext compositeContext, Component component) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Starting component: " + component.getURI());
        }
        RuntimeComponent runtimeComponent = ((RuntimeComponent)component);
        if(runtimeComponent.isStarted()) {
            return;
        }

        compositeContext.bindComponent(runtimeComponent);
        Implementation implementation = component.getImplementation();
        
        List<RuntimeProvider> providers = new ArrayList<RuntimeProvider>();
        try {

            if (implementation instanceof Composite) {
                try {
                    start(compositeContext, (Composite)implementation);
                } catch (Throwable e) {
                    try {
                        stop(compositeContext, (Composite) implementation);
                    } catch (Throwable e1) {
                        Monitor.error(monitor, this, "core-messages", "StopException", e1);
                    }
                    rethrow(e);
                }
            } else {
                for (PolicyProvider policyProvider : runtimeComponent.getPolicyProviders()) {
                    policyProvider.start();
                    providers.add(policyProvider);
                }
                ImplementationProvider implementationProvider = runtimeComponent.getImplementationProvider();
                if (implementationProvider != null) {
                    implementationProvider.start();
                    providers.add(implementationProvider);
                }
            }

            for (ComponentService service : component.getServices()) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Starting component service: " + component.getURI() + "#" + service.getName());
                }
                for (Endpoint endpoint : service.getEndpoints()) {
                    RuntimeEndpoint ep = (RuntimeEndpoint)endpoint;
                    startEndpoint(compositeContext, ep, providers);
                }
            }
            
            // Reference start is done after all components have been started to make sure everything
            // is up and running before we try and connect references to services
            
        } catch (Throwable e) {
            // any providers (binding, implementation, policy) that were started
            // before the error occured are stopped here
            for (int i = providers.size() - 1; i >= 0; i--) {
                try {
                    providers.get(i).stop();
                } catch (Throwable e1) {
                    Monitor.error(monitor, this, "core-messages", "StopException", e1);
                }
            }
            rethrow(e);
        } finally {
            providers.clear();
        }

        // mark a successful start
        runtimeComponent.setStarted(true);
    }

    private void rethrow(Throwable e) throws Error {
        if(e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else if(e instanceof Error) {
            throw (Error) e;
        }
    }

    public void stop(CompositeContext compositeContext, Component component) {
        if (!((RuntimeComponent)component).isStarted()) {
            return;
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Stopping component: " + component.getURI());
        }
        for (ComponentService service : component.getServices()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Stopping component service: " + component.getURI() + "#" + service.getName());
            }
            for (Endpoint endpoint : service.getEndpoints()) {
                RuntimeEndpoint ep = (RuntimeEndpoint) endpoint;
                stop(ep);
            }
        }
        for (ComponentReference reference : component.getReferences()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Stopping component reference: " + component.getURI() + "#" + reference.getName());
            }

            for (EndpointReference endpointReference : reference.getEndpointReferences()) {
                RuntimeEndpointReference epr = (RuntimeEndpointReference) endpointReference;
                stop(epr);
            }
        }
        Implementation implementation = component.getImplementation();
        if (implementation instanceof Composite) {
            stop(compositeContext, (Composite)implementation);
        } else {
            final ImplementationProvider implementationProvider = ((RuntimeComponent)component).getImplementationProvider();
            if (implementationProvider != null) {
                try {
                    // Allow bindings to read properties. Requires PropertyPermission read in security policy.
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            implementationProvider.stop();
                            return null;
                          }
                    });
                } catch (Throwable ex){
                    Monitor.error(monitor, this, "core-messages", "StopException", ex);
                }                  
            }
            for (PolicyProvider policyProvider : ((RuntimeComponent)component).getPolicyProviders()) {
                try {
                    policyProvider.stop();
                } catch (Throwable ex){
                    Monitor.error(monitor, this, "core-messages", "StopException", ex);
                }  
            }
        }

        if (component instanceof ScopedRuntimeComponent) {
            ScopedRuntimeComponent runtimeComponent = (ScopedRuntimeComponent)component;
            if (runtimeComponent.getScopeContainer() != null &&
                    runtimeComponent.getScopeContainer().getLifecycleState() != ScopeContainer.STOPPED) {
                try {
                    runtimeComponent.getScopeContainer().stop();
                } catch (Throwable ex){
                    Monitor.error(monitor, this, "core-messages", "StopException", ex);
                }                      
            }
        }

        ((RuntimeComponent)component).setStarted(false);
    }


    // Scope container start/stop
    // separate off from component start that all endpoints are 
    // registered before any @EagerInit takes place
    public void start(CompositeContext compositeContext, ScopedRuntimeComponent scopedRuntimeComponent) {
        if (scopedRuntimeComponent.getScopeContainer() != null) {
            try {
                scopedRuntimeComponent.getScopeContainer().start();
            } catch (Throwable ex){
                Monitor.error(monitor, this, "core-messages", "StartException", ex);
                rethrow(ex);
            }             
        }
    }
    
    // Service start/stop

    public void start(CompositeContext compositeContext, RuntimeEndpoint ep) {
        startEndpoint(compositeContext, ep, null);
    }

    private void startEndpoint(CompositeContext compositeContext, RuntimeEndpoint ep, final List<RuntimeProvider> providers) {
        // FIXME: Should the policy providers be started before the endpoint is started?
        for (PolicyProvider policyProvider : ep.getPolicyProviders()) {
            policyProvider.start();
            if (providers != null) {
                try {
                    providers.add(policyProvider);
                } catch (Throwable ex){
                    Monitor.error(monitor, this, "core-messages", "StartException", ex);
                    rethrow(ex);
                }                     
            }
        }

        final ServiceBindingProvider bindingProvider = ep.getBindingProvider();
        if (bindingProvider != null) {
            try {
                // bindingProvider.start();
                // Allow bindings to add shutdown hooks. Requires RuntimePermission shutdownHooks in policy.
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        bindingProvider.start();
                        if (providers != null) {
                            providers.add(bindingProvider);
                        }
                        return null;
                      }
                });
                compositeContext.getEndpointRegistry().addEndpoint(ep);
            } catch (Throwable ex){
                Monitor.error(monitor, this, "core-messages", "StartException", ex);
                rethrow(ex);
            }  
        }
    }
    
    public void stop(RuntimeEndpoint ep) {
        ep.getCompositeContext().getEndpointRegistry().removeEndpoint(ep);
        final ServiceBindingProvider bindingProvider = ep.getBindingProvider();
        if (bindingProvider != null) {
            try {
                // Allow bindings to read properties. Requires PropertyPermission read in security policy.
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        bindingProvider.stop();
                        return null;
                      }
                });
            } catch (Throwable ex){
                Monitor.error(monitor, this, "core-messages", "StopException", ex);
            }  
        }
        for (PolicyProvider policyProvider : ep.getPolicyProviders()) {
            try {
                policyProvider.stop();
            } catch (Throwable ex){
                Monitor.error(monitor, this, "core-messages", "StopException", ex);
            }                  
        }
    }


    // Reference start/stop

    public void start(CompositeContext compositeContext, RuntimeComponent component, RuntimeComponentReference reference) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Starting component reference: " + component.getURI() + "#" + reference.getName());
        }
        
        for (EndpointReference endpointReference : reference.getEndpointReferences()){
            RuntimeEndpointReference epr = (RuntimeEndpointReference)endpointReference;

            // If the reference is already resolved then start it now. This currently 
            // important for async references which have native async bindings as the 
            // reference provider has to register a response listener regardless of 
            // whether the reference has been used or not. 
            if (epr.getStatus() == EndpointReference.Status.WIRED_TARGET_FOUND_AND_MATCHED ||
                epr.getStatus() == EndpointReference.Status.RESOLVED_BINDING){
                
                // As we only care about starting references at build time in the
                // async case at the moment check that the binding supports native async
                // and that the reference is an async reference
                ReferenceBindingProvider bindingProvider = epr.getBindingProvider();
                if (bindingProvider instanceof EndpointReferenceAsyncProvider &&
                    ((EndpointReferenceAsyncProvider)bindingProvider).supportsNativeAsync() &&
                    epr.isAsyncInvocation()){
                    // it's resolved so start it now
                    try {
                        // The act of getting invocation chains starts the reference in the late binding case
                        // so just use that here
                        epr.getInvocationChains();
                    } catch (Throwable ex){
                        Monitor.error(monitor, this, "core-messages", "StartException", ex);
                        rethrow(ex);
                    }  
                }
            }
        }
    }

    public void stop(Component component, ComponentReference reference) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Stopping component reference: " + component.getURI() + "#" + reference.getName());
        }
        RuntimeComponentReference runtimeRef = ((RuntimeComponentReference)reference);
        for ( EndpointReference endpointReference : runtimeRef.getEndpointReferences()){
            RuntimeEndpointReference epr = (RuntimeEndpointReference) endpointReference;
            stop(epr);
        }
    }

    @Deprecated    
    public void start(CompositeContext compositeContext, RuntimeEndpointReference endpointReference) {
        compositeContext.getEndpointRegistry().addEndpointReference(endpointReference);
        
        // The act of getting invocation chains starts the reference in the late binding case
        // so just use that here
        endpointReference.getInvocationChains();
    }

    public void stop(RuntimeEndpointReference epr) {
        if (epr.isStarted()) {
            CompositeContext compositeContext = epr.getCompositeContext();
            if (compositeContext == null) {
                throw new IllegalStateException("The endpoint reference is not bound");
            }
            compositeContext.getEndpointRegistry().removeEndpointReference(epr);
            ReferenceBindingProvider bindingProvider = epr.getBindingProvider();
            if (bindingProvider != null) {
                try {
                    bindingProvider.stop();
                } catch (Throwable ex){
                    Monitor.error(monitor, this, "core-messages", "StopException", ex);
                }  
            }
            for (PolicyProvider policyProvider : epr.getPolicyProviders()) {
                try {
                    policyProvider.stop();
                } catch (Throwable ex){
                    Monitor.error(monitor, this, "core-messages", "StopException", ex);
                }                      
            }
        }
    }
}
