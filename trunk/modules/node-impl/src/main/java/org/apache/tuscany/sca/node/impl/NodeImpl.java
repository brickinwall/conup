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

package org.apache.tuscany.sca.node.impl;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.context.CompositeContext;
import org.apache.tuscany.sca.context.ThreadMessageContext;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.configuration.NodeConfiguration;
import org.apache.tuscany.sca.node.extensibility.NodeActivator;
import org.apache.tuscany.sca.node.extensibility.NodeActivatorExtensionPoint;
import org.apache.tuscany.sca.node.extensibility.NodeExtension;
import org.apache.tuscany.sca.runtime.ActivationException;
import org.apache.tuscany.sca.runtime.CompositeActivator;
import org.apache.tuscany.sca.runtime.DomainRegistry;
import org.apache.tuscany.sca.runtime.DomainRegistryFactory;
import org.apache.tuscany.sca.runtime.ExtensibleDomainRegistryFactory;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;

/**
 * An SCA Node that is managed by the NodeManager
 */
public class NodeImpl implements Node, NodeExtension {
    private static final Logger logger = Logger.getLogger(NodeImpl.class.getName());
    private ProxyFactory proxyFactory;
    private CompositeActivator compositeActivator;
    private CompositeContext compositeContext;
    private Composite domainComposite;
    private NodeConfiguration configuration;
    private NodeFactoryImpl nodeFactory;
    private List<Contribution> contributions;
    private NodeActivatorExtensionPoint nodeActivators;
    // private NodeManager mbean;

    /**
     * Create a node from the configuration
     * @param manager
     * @param configuration
     */
    public NodeImpl(NodeFactoryImpl nodeFactory, NodeConfiguration configuration) {
        super();
        this.configuration = configuration;
        this.nodeFactory = nodeFactory;
        this.nodeActivators = nodeFactory.getExtensionPointRegistry().getExtensionPoint(NodeActivatorExtensionPoint.class);
    }

    /**
     * Create a node from the configuration and loaded contributions
     * @param manager
     * @param configuration
     * @param contributions
     */
    public NodeImpl(NodeFactoryImpl manager, NodeConfiguration configuration, List<Contribution> contributions) {
        super();
        this.configuration = configuration;
        this.nodeFactory = manager;
        this.nodeActivators = nodeFactory.getExtensionPointRegistry().getExtensionPoint(NodeActivatorExtensionPoint.class);
        this.contributions = new ArrayList<Contribution>(contributions);
    }

    public String getURI() {
        return getConfiguration().getURI();
    }

    public String getDomainURI() {
        return getConfiguration().getDomainURI();
    }

    public Node start() {
        logger.log(nodeFactory.quietLogging? Level.FINE : Level.INFO, "Starting node: " + configuration.getURI() + " domain: " + configuration.getDomainURI());

        try {
            load();

            this.proxyFactory = nodeFactory.proxyFactory;

            // Set up the node context
            UtilityExtensionPoint utilities = nodeFactory.registry.getExtensionPoint(UtilityExtensionPoint.class);
            this.compositeActivator = utilities.getUtility(CompositeActivator.class);

            DomainRegistryFactory domainRegistryFactory =
                ExtensibleDomainRegistryFactory.getInstance(nodeFactory.registry);
            DomainRegistry domainRegistry =
                domainRegistryFactory.getEndpointRegistry(configuration.getDomainRegistryURI(),
                                                          configuration.getDomainURI());

            this.compositeContext =
                new CompositeContext(nodeFactory.registry, domainRegistry, domainComposite,
                                     configuration.getDomainURI(), configuration.getURI(), nodeFactory
                                         .getDeployer().getSystemDefinitions());
            // Pass down the context attributes
            compositeContext.getAttributes().putAll(configuration.getAttributes());
            
            // Add endpoint descriptions from the node configuration if the domain registry is local
            if (!domainRegistry.isDistributed()) {
                for (Endpoint e : configuration.getEndpointDescriptions()) {
                    domainRegistry.addEndpoint(e);
                }
            }
            // Activate the composite
            compositeActivator.activate(compositeContext, domainComposite);

            // Start the composite
            compositeActivator.start(compositeContext, domainComposite);

            // FIXME: [rfeng] We should turn the management capability into a system utility.
            // In certain environment such as Google App Engine, the JMX API is not allowed
            try {
                /*
                MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
                mbean = new NodeManager(this);
                mBeanServer.registerMBean(mbean, mbean.getName());
                */
                /*
                LocateRegistry.createRegistry(9999);
                JMXServiceURL url =
                    new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
                JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mBeanServer);
                connectorServer.start();
                */
            } catch (Throwable e) {
                // Ignore the error for now
                // mbean = null;
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

            for(NodeActivator activator : nodeActivators.getNodeActivators()) {
                activator.nodeStarted(this);
            }
            return this;

        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }

    }

    public void load() throws Throwable {
        nodeFactory.init();
        
        nodeFactory.addNode(configuration, this);

        Monitor monitor = nodeFactory.monitorFactory.createMonitor();
        ProcessorContext context = new ProcessorContext(monitor);

        // Set up the thead context monitor
        Monitor tcm = nodeFactory.monitorFactory.setContextMonitor(monitor);
        try {
            // Use the lack of the contributions collection as an indicator for when the node
            // is being started for the first time. If it is the first time do all the work
            // to read the contributions and create the domain composite
            if (contributions == null) {
                contributions = nodeFactory.loadContributions(configuration, context);
            }

            if (domainComposite == null) {
                domainComposite = nodeFactory.configureNode(configuration, contributions, context);
            }


        } finally {
            // Reset the thread context monitor
            nodeFactory.monitorFactory.setContextMonitor(tcm);
        }
    }

    public void stop() {
        logger.log(nodeFactory.quietLogging? Level.FINE : Level.INFO, "Stopping node: " + configuration.getURI());

        try {
            if (compositeActivator == null) {
                return;
            }

            /*
            if (mbean != null) {
                try {
                    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
                    mBeanServer.unregisterMBean(mbean.getName());
                } catch (Throwable e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                } finally {
                    mbean = null;
                }
            }
            */

            if( domainComposite != null ) {

                // Stop the composite
                compositeActivator.stop(compositeContext, domainComposite);

                // Deactivate the composite
                compositeActivator.deactivate(domainComposite);

            } // end if
            
            // Remove the external endpoint descriptions from node.xml
            DomainRegistry domainRegistry = compositeContext.getEndpointRegistry();
            if (!domainRegistry.isDistributed()) {
                for (Endpoint e : configuration.getEndpointDescriptions()) {
                    domainRegistry.removeEndpoint(e);
                }
            }

            nodeFactory.removeNode(configuration);
/*
            this.compositeActivator = null;
            this.proxyFactory = null;
            this.domainComposite = null;
            this.compositeContext = null;
*/

            for(NodeActivator activator : nodeActivators.getNodeActivators()) {
                activator.nodeStopped(this);
            }

            ThreadMessageContext.removeMessageContext();

        } catch (ActivationException e) {
            throw new IllegalStateException(e);
        }

    }

    @SuppressWarnings("unchecked")
    public <B, R extends ServiceReference<B>> R cast(B target) throws IllegalArgumentException {
        return (R)proxyFactory.cast(target);
    }

    public <B> B getService(Class<B> businessInterface, String serviceName) {

        ServiceReference<B> serviceReference = getServiceReference(businessInterface, serviceName);
        if (serviceReference == null) {
            throw new ServiceRuntimeException("Service not found: " + serviceName);
        }
        return serviceReference.getService();
    }

    public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, String name) {

        // Extract the component name
        String componentName = null;
        String serviceName = null;
        if (name != null) {
            int i = name.indexOf('/');
            if (i != -1) {
                componentName = name.substring(0, i);
                serviceName = name.substring(i + 1);

            } else {
                componentName = name;
                serviceName = null;
            }
        }

        // Lookup the component
        Component component = null;

        for (Component compositeComponent : domainComposite.getComponents()) {
            if (componentName == null) {
                for (ComponentService service : compositeComponent.getServices()) {
                    Interface intf = service.getInterfaceContract().getInterface();
                    if (intf instanceof JavaInterface) {
                        JavaInterface ji = (JavaInterface)intf;
                        if (ji.getJavaClass() == businessInterface) {
                            return ((RuntimeComponent)compositeComponent).getComponentContext()
                                .createSelfReference(businessInterface, service);
                        }
                    }
                }
            }
            if (compositeComponent.getName().equals(componentName)) {
                component = compositeComponent;
                break;
            }
        }

        if (component == null) {
            throw new ServiceUnavailableException("The service " + name + " has not been contributed to the domain");
        }

        return ((RuntimeComponent)component).getServiceReference(businessInterface, serviceName);
    }

    public NodeConfiguration getConfiguration() {
        return configuration;
    }

    public ExtensionPointRegistry getExtensionPointRegistry() {
        return nodeFactory.getExtensionPointRegistry();
    }

    /**
     * Get the service endpoints in this Node
     * TODO: needs review, works for the very simple testcase but i expect there are
     *    other endpoints to be included
     */
    public List<Endpoint> getServiceEndpoints() {
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        if (compositeActivator != null) {
            if (domainComposite != null) {
                for (Component component : domainComposite.getComponents()) {
                    for (Service service : component.getServices()) {
                           // MJE 28/05/2009 - changed to RuntimeComponentService from RuntimeComponentServiceImpl
                           // - no need to access the Impl directly here
                        if (service instanceof RuntimeComponentService) {
                            endpoints.addAll(((RuntimeComponentService)service).getEndpoints());
                        }
                    }
                }
            }
        }
        return endpoints;
    }

    public Composite getDomainComposite() {
        return domainComposite;
    }

    public String dumpDomainComposite() {

        StAXArtifactProcessorExtensionPoint xmlProcessors =
            getExtensionPointRegistry().getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        StAXArtifactProcessor<Composite>  compositeProcessor =
            xmlProcessors.getProcessor(Composite.class);

        return writeComposite(getDomainComposite(), compositeProcessor);
    }

    private String writeComposite(Composite composite, StAXArtifactProcessor<Composite> compositeProcessor){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLOutputFactory outputFactory =
            nodeFactory.getExtensionPointRegistry().getExtensionPoint(FactoryExtensionPoint.class)
                .getFactory(XMLOutputFactory.class);

        try {
            XMLStreamWriter xmlStreamWriter = outputFactory.createXMLStreamWriter(bos);
            compositeProcessor.write(composite, xmlStreamWriter, new ProcessorContext(nodeFactory.registry));
            xmlStreamWriter.flush();
        } catch(Exception ex) {
            return ex.toString();
        }

        
        String result = bos.toString();

        // write out and nested composites
        for (Component component : composite.getComponents()) {
            if (component.getImplementation() instanceof Composite) {
                result += "\n<!-- XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX -->\n" +
                           writeComposite((Composite)component.getImplementation(),
                                          compositeProcessor);
            }
        }

        return result;
    }

    public List<Contribution> getContributions() {
        return contributions;
    }

    @Override
    public String getEndpointAddress(String serviceBindingName) {
        if (serviceBindingName == null) {
            throw new IllegalArgumentException("Service binding name cannot be null");
        }
        
        // Calculate the names for compoment/service/binding
        String[] parts = serviceBindingName.split("/");
        String componentName = parts[0];
        String serviceName = parts.length >= 2 ? parts[1] : null;
        String bindingName = parts.length >= 3 ? parts[2] : serviceName;

        if (domainComposite != null) {
            for (Component component : domainComposite.getComponents()) {
                if (!component.getName().equals(componentName)) {
                    continue;
                }
                for (Service service : component.getServices()) {
                    if (serviceName != null && !service.getName().equals(serviceName)) {
                        continue;
                    }
                    if (service instanceof RuntimeComponentService) {
                        for (Endpoint ep : ((RuntimeComponentService)service).getEndpoints()) {
                            if (bindingName == null || bindingName.equals(ep.getBinding().getName())) {
                                return ep.getDeployedURI();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

}
