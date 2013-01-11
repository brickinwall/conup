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
package org.apache.tuscany.sca.core.context.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentProperty;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.CompositeService;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.Multiplicity;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.context.CompositeContext;
import org.apache.tuscany.sca.context.PropertyValueFactory;
import org.apache.tuscany.sca.context.RequestContextFactory;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceContract;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.runtime.CompositeActivator;
import org.apache.tuscany.sca.runtime.EndpointReferenceBinder;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentContext;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;
import org.apache.tuscany.sca.runtime.TuscanyServiceReference;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * Implementation of ComponentContext that delegates to a ComponentContextProvider.
 *
 * @version $Rev: 1213617 $ $Date: 2011-12-13 17:17:42 +0800 (周二, 13 十二月 2011) $
 */
public class ComponentContextImpl implements RuntimeComponentContext {
    private final RuntimeComponent component;

    private final CompositeContext compositeContext;
    private final CompositeActivator compositeActivator;
    private final RequestContextFactory requestContextFactory;
    private final ProxyFactory proxyFactory;
    private final AssemblyFactory assemblyFactory;
    private final JavaInterfaceFactory javaInterfaceFactory;
    private final PropertyValueFactory propertyFactory;
    private final EndpointReferenceBinder eprBinder;
    private final ExtensionPointRegistry registry;

    public ComponentContextImpl(ExtensionPointRegistry registry,
                                AssemblyFactory assemblyFactory,
                                JavaInterfaceFactory javaInterfaceFactory,
                                CompositeActivator compositeActivator,
                                RequestContextFactory requestContextFactory,
                                PropertyValueFactory propertyFactory,
                                EndpointReferenceBinder eprBinder,
                                ProxyFactory proxyFactory,
                                CompositeContext compositeContext,
                                RuntimeComponent component) {
        this.registry = registry;
        this.assemblyFactory = assemblyFactory;
        this.javaInterfaceFactory = javaInterfaceFactory;
        this.compositeActivator = compositeActivator;
        this.requestContextFactory = requestContextFactory;
        this.propertyFactory = propertyFactory;
        this.eprBinder = eprBinder;
        this.proxyFactory = proxyFactory;
        this.compositeContext = compositeContext;
        this.component = component;
    }

    public String getURI() {
        return component.getURI();
    }

    public <B> ServiceReference<B> cast(B target) throws IllegalArgumentException {
        return proxyFactory.cast(target);
    }

    public <B> B getService(Class<B> businessInterface, String referenceName) throws IllegalArgumentException {
        B service = null;

        ServiceReference<B> serviceRef = getServiceReference(businessInterface, referenceName);
        if (serviceRef != null) {
            service = serviceRef.getService();
        }

        return service;
    }

    public <B> TuscanyServiceReference<B> getServiceReference(Class<B> businessInterface, String referenceName)
        throws IllegalArgumentException {

        for (ComponentReference ref : component.getReferences()) {
            if (referenceName.equals(ref.getName())) {
                Multiplicity multiplicity = ref.getMultiplicity();
                if (multiplicity == Multiplicity.ZERO_N || multiplicity == Multiplicity.ONE_N) {
                    throw new IllegalArgumentException("Reference " + referenceName
                        + " has multiplicity "
                        + multiplicity);
                }
                if (ref.getEndpointReferences().size() < 1) {
                    return null;
                }
                TuscanyServiceReference<B> sr =
                    getServiceReference(businessInterface, (RuntimeEndpointReference)getEndpointReference(ref));
                if (sr == null) {
                    throw new IllegalArgumentException("Reference " + referenceName + " is null");
                }
                return sr;
            }
        }
        throw new IllegalArgumentException("[JCA80011] Reference not found: " + referenceName);

    }

    /**
     * Select an endpoint reference from the component reference
     * @param ref
     * @return
     */
    private EndpointReference getEndpointReference(ComponentReference ref) {
        List<EndpointReference> eprs = ref.getEndpointReferences();
        if (eprs.size() == 1) {
            // Return 1st one
            return eprs.get(0);
        } else {
            for (EndpointReference epr : eprs) {
                // Try to see if there is an EPR using binding.sca
                if (epr.getBinding().getType().equals(SCABinding.TYPE)) {
                    return epr;
                }
            }
            return eprs.get(0);
        }
    }

    /**
     * Select an endpoint reference from the component reference
     * @param ref
     * @return
     */
    private Endpoint getEndpoint(ComponentService service, String bindingName) {
        if (bindingName == null) {
            // The default binding name is the name of the promoted service
            bindingName = getPromotedService(service).getName();
        }
        Endpoint returnEp = null;
        List<Endpoint> eps = service.getEndpoints();
        for (Endpoint ep : eps) {
            Binding binding = ep.getBinding();
            if (bindingName.equals(binding.getName()) || binding.getName() == null) {
                returnEp = ep;
                break;
            }
        }
        //TUSCANY-3543
        if (returnEp == null) {
            returnEp = eps.get(0);
        }

        return returnEp;
    }

    private ComponentService getPromotedService(ComponentService componentService) {
        Service service = componentService.getService();
        if (service instanceof CompositeService) {
            return getPromotedService(((CompositeService)service).getPromotedService());
        } else {
            return componentService;
        }

    }

    /**
     * Gets the value for the specified property with the specified type.
     * 
     * @param type The type of the property value we are getting
     * @param propertyName The name of the property we are getting
     * @param B The class of the property value we are getting
     * 
     * @throws ServiceRuntimeException If a Property for the specified propertyName
     *         is not found 
     *         
     * @see #setPropertyValueFactory(PropertyValueFactory)         
     */
    public <B> B getProperty(Class<B> type, String propertyName) {
        for (ComponentProperty p : component.getProperties()) {
            if (propertyName.equals(p.getName())) {
                return propertyFactory.createPropertyValue(p, type);
            }
        }
        throw new IllegalArgumentException("Property not found: " + propertyName);
    }

    /**
     * @param component
     */
    public static ComponentService getSingleService(Component component) {
        ComponentService targetService;
        List<ComponentService> services = component.getServices();
        List<ComponentService> regularServices = new ArrayList<ComponentService>();
        for (ComponentService service : services) {
            if (service.isForCallback()) {
                continue;
            }
            String name = service.getName();
            if (!name.startsWith("$") || name.startsWith("$dynamic$")) {
                regularServices.add(service);
            }
        }
        if (regularServices.size() == 0) {
            throw new ServiceRuntimeException("No service is declared on component " + component.getURI());
        }
        if (regularServices.size() != 1) {
            throw new ServiceRuntimeException("More than one service is declared on component " + component.getURI()
                + ". Service name is required to get the service.");
        }
        targetService = regularServices.get(0);
        return targetService;
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface) {
        ComponentService service = getSingleService(component);
        try {
            return createSelfReference(businessInterface, service);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface, String serviceName) {
        if (serviceName == null) {
            return createSelfReference(businessInterface);
        }
        try {
            String bindingName = null;
            int index = serviceName.indexOf('/');
            if (index != -1) {
                bindingName = serviceName.substring(index + 1);
                serviceName = serviceName.substring(0, index);
            }
            for (ComponentService service : component.getServices()) {
                if (serviceName.equals(service.getName())) {
                    Endpoint endpoint = getEndpoint(service, bindingName);
                    if (endpoint == null) {
                        break;
                    }
                    return getServiceReference(businessInterface, (RuntimeEndpoint)endpoint);
                }
            }
            throw new IllegalArgumentException("Service not found: " + serviceName);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (ServiceRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param <B>
     * @param businessInterface
     * @param service
     * @return
     */
    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface, ComponentService service) {
        try {
            RuntimeEndpointReference ref =
                (RuntimeEndpointReference)createEndpointReference(component, service, null, businessInterface);
            ref.setComponent(component);
            return getServiceReference(businessInterface, ref);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public RequestContext getRequestContext() {
        if (requestContextFactory != null) {
            return requestContextFactory.createRequestContext(component);
        } else {
            return new RequestContextImpl(component);
        }
    }

    /**
     * @param businessInterface
     * @param reference
     * @return
     * @throws CloneNotSupportedException
     * @throws InvalidInterfaceException
     */
    public <B> TuscanyServiceReference<B> getServiceReference(Class<B> businessInterface,
                                                       RuntimeEndpointReference endpointReference) {
        TuscanyServiceReference<B> result = null;

        try {
            InterfaceContract interfaceContract = endpointReference.getComponentTypeReferenceInterfaceContract();
            if (businessInterface == null) {
                businessInterface = (Class<B>)((JavaInterface)interfaceContract.getInterface()).getJavaClass();
            }
            RuntimeComponentReference ref = (RuntimeComponentReference)endpointReference.getReference();
            InterfaceContract refInterfaceContract = getInterfaceContract(interfaceContract, businessInterface);
            if (refInterfaceContract != null) {
                if (refInterfaceContract != interfaceContract) {
                    ref = (RuntimeComponentReference)ref.clone();
                    if (interfaceContract != null) {
                        ref.setInterfaceContract(interfaceContract);
                    } else {
                        ref.setInterfaceContract(refInterfaceContract);
                    }
                }

                ref.setComponent(component);
                result =
                    new ServiceReferenceImpl<B>(businessInterface, endpointReference, component.getComponentContext()
                        .getCompositeContext());
            }
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }

        return result;
    }

    public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, RuntimeEndpoint endpoint) {
        try {
            if (businessInterface == null) {
                InterfaceContract contract = endpoint.getBindingInterfaceContract();
                if (contract.getInterface() instanceof JavaInterface) {
                    businessInterface = (Class<B>)((JavaInterface)contract.getInterface()).getJavaClass();
                } else {
                    contract = endpoint.getComponentTypeServiceInterfaceContract();
                    if (contract.getInterface() instanceof JavaInterface) {
                        businessInterface = (Class<B>)((JavaInterface)contract.getInterface()).getJavaClass();
                    }
                } 
            }
            RuntimeEndpointReference ref =
                (RuntimeEndpointReference)createEndpointReference(endpoint, businessInterface);
            ref.setComponent(component);
            return new ServiceReferenceImpl<B>(businessInterface, ref, compositeContext);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    /**
     * Create a self-reference for a component service
     * @param component
     * @param service
     * @throws CloneNotSupportedException 
     * @throws InvalidInterfaceException 
     */
    private EndpointReference createEndpointReference(Component component,
                                                      ComponentService service,
                                                      String bindingName,
                                                      Class<?> businessInterface) throws CloneNotSupportedException,
        InvalidInterfaceException {

        Endpoint endpoint = getEndpoint(service, bindingName);
        return createEndpointReference(endpoint, businessInterface);
    }

    private EndpointReference createEndpointReference(Endpoint endpoint, Class<?> businessInterface)
        throws CloneNotSupportedException, InvalidInterfaceException {
        Component component = endpoint.getComponent();
        ComponentService service = endpoint.getService();
        ComponentReference componentReference = assemblyFactory.createComponentReference();
        componentReference.setName("$self$." + service.getName());

        componentReference.setCallback(service.getCallback());
        componentReference.getTargets().add(service);
        componentReference.getPolicySets().addAll(service.getPolicySets());
        componentReference.getRequiredIntents().addAll(service.getRequiredIntents());
        componentReference.getBindings().add(endpoint.getBinding());

        // For the self-reference, allows pass by reference
        componentReference.setAllowsPassByReference(true);

        InterfaceContract interfaceContract = service.getInterfaceContract();
        Service componentTypeService = service.getService();
        if (componentTypeService != null && componentTypeService.getInterfaceContract() != null) {
            interfaceContract = componentTypeService.getInterfaceContract();
        }
        interfaceContract = getInterfaceContract(interfaceContract, businessInterface);
        componentReference.setInterfaceContract(interfaceContract);
        componentReference.setMultiplicity(Multiplicity.ONE_ONE);
        // component.getReferences().add(componentReference);

        // create endpoint reference
        EndpointReference endpointReference = assemblyFactory.createEndpointReference();
        endpointReference.setComponent(component);
        endpointReference.setReference(componentReference);
        endpointReference.setBinding(endpoint.getBinding());
        endpointReference.setUnresolved(false);
        endpointReference.setStatus(EndpointReference.Status.WIRED_TARGET_FOUND_READY_FOR_MATCHING);

        endpointReference.setTargetEndpoint(endpoint);

        componentReference.getEndpointReferences().add(endpointReference);
        ((RuntimeComponentReference)componentReference).setComponent((RuntimeComponent)component);
        ((RuntimeEndpointReference)endpointReference).bind(compositeContext);

        return endpointReference;
    }

    /**
     * @param interfaceContract
     * @param businessInterface
     * @return
     * @throws CloneNotSupportedException
     * @throws InvalidInterfaceException
     */
    private InterfaceContract getInterfaceContract(InterfaceContract interfaceContract, Class<?> businessInterface)
        throws CloneNotSupportedException, InvalidInterfaceException {
        if (businessInterface == null) {
            return interfaceContract;
        }
        if (interfaceContract == null) {
            JavaInterfaceContract ic = javaInterfaceFactory.createJavaInterfaceContract();
            ic.setInterface(javaInterfaceFactory.createJavaInterface(businessInterface));
            return ic;
        }
        boolean compatible = false;
        if (interfaceContract != null && interfaceContract.getInterface() != null) {
            Interface interfaze = interfaceContract.getInterface();
            if (interfaze instanceof JavaInterface) {
                Class<?> cls = ((JavaInterface)interfaze).getJavaClass();
                if (businessInterface.isAssignableFrom(cls)) {
                    compatible = true;
                }
                if (!compatible) {
                    InterfaceContract biContract = javaInterfaceFactory.createJavaInterfaceContract();
                    JavaInterface callInterface = javaInterfaceFactory.createJavaInterface(businessInterface);
                    biContract.setInterface(callInterface);
                    if (callInterface.getCallbackClass() != null) {
                        biContract.setCallbackInterface(javaInterfaceFactory.createJavaInterface(callInterface
                            .getCallbackClass()));
                    }
                    InterfaceContractMapper ifcm = registry.getExtensionPoint(InterfaceContractMapper.class);
                    compatible = ifcm.isCompatibleSubset(biContract, interfaceContract);
		    // If the business interface class is not assignable from the service interface class but
		    // they are compatible, we need to return an InterfaceContract with the business interface
		    // class to store on the reference side. 
		    if ( compatible ) {
			return biContract;
		    }
                }

            }
        }

        if (!compatible) {
            // JCA-9011
            throw new IllegalArgumentException("Business interface " + businessInterface.getName()
                + " is not compatible with "
                + interfaceContract.getInterface());
        }

        return interfaceContract;
    }

    /* ******************** Contribution for issue TUSCANY-2281 ******************** */

    /**
     * @see ComponentContext#getServices(Class<B>, String)
     */
    public <B> Collection<B> getServices(Class<B> businessInterface, String referenceName) {
        ArrayList<B> services = new ArrayList<B>();
        Collection<ServiceReference<B>> serviceRefs = getServiceReferences(businessInterface, referenceName);
        for (ServiceReference<B> serviceRef : serviceRefs) {
            services.add(serviceRef.getService());
        }
        return services;
    }

    /**
     * @see ComponentContext#getServiceReferences(Class<B>, String)
     */
    public <B> Collection<ServiceReference<B>> getServiceReferences(Class<B> businessInterface, String referenceName) {
        try {
            for (ComponentReference ref : component.getReferences()) {
                if (referenceName.equals(ref.getName())) {
                    if (ref.getMultiplicity() == Multiplicity.ONE_ONE)
                        throw new IllegalArgumentException("Reference " + referenceName
                            + " is not a valid argument for getServiceReferences because it has a multiplicity of 1..1");
                    if (ref.getMultiplicity() == Multiplicity.ZERO_ONE)
                        throw new IllegalArgumentException("Reference " + referenceName
                            + " is not a valid argument for getServiceReferences because it has a multiplicity of 0..1");

                    ArrayList<ServiceReference<B>> serviceRefs = new ArrayList<ServiceReference<B>>();
                    for (EndpointReference endpointReference : ref.getEndpointReferences()) {
                        RuntimeEndpointReference epr = (RuntimeEndpointReference)endpointReference;
                        serviceRefs.add(getServiceReference(businessInterface, epr));
                    }
                    return serviceRefs;
                }
            }
            throw new IllegalArgumentException("Reference not found: " + referenceName);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (ServiceRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    /* ******************** Contribution for issue TUSCANY-2281 ******************** */

    public CompositeContext getCompositeContext() {
        return compositeContext;
    }

    public ExtensionPointRegistry getExtensionPointRegistry() {
        return getCompositeContext().getExtensionPointRegistry();
    }

}
