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

package org.apache.tuscany.sca.osgi.remoteserviceadmin.impl;

import static org.apache.tuscany.sca.osgi.remoteserviceadmin.impl.EndpointHelper.createEndpointDescription;
import static org.apache.tuscany.sca.osgi.remoteserviceadmin.impl.OSGiHelper.createOSGiProperty;
import static org.apache.tuscany.sca.osgi.remoteserviceadmin.impl.OSGiHelper.getFrameworkUUID;
import static org.apache.tuscany.sca.osgi.remoteserviceadmin.impl.OSGiHelper.getOSGiProperties;
import static org.osgi.framework.Constants.SERVICE_ID;
import static org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID;
import static org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.implementation.osgi.OSGiProperty;
import org.apache.tuscany.sca.implementation.osgi.SCAConfig;
import org.apache.tuscany.sca.implementation.osgi.ServiceDescriptionsFactory;
import org.apache.tuscany.sca.node.configuration.NodeConfiguration;
import org.apache.tuscany.sca.node.impl.NodeImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ExportRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Watching and exporting OSGi services 
 */
public class OSGiServiceExporter extends AbstractOSGiServiceHandler implements ServiceTrackerCustomizer {
    private final static Logger logger = Logger.getLogger(OSGiServiceExporter.class.getName());

    private Map<EndpointDescription, ExportReferenceImpl> exportReferences =
        new ConcurrentHashMap<EndpointDescription, ExportReferenceImpl>();

    /**
     * @param context
     * @param clazz
     * @param customizer
     */
    public OSGiServiceExporter(BundleContext context) {
        super(context);
    }

    public void start() {
        init();
    }

    public void stop() {
        exportReferences.clear();
        super.stop();
    }

    public Object addingService(ServiceReference reference) {
        return exportService(reference, null);
    }

    public List<ExportRegistration> exportService(ServiceReference reference, Map<String, Object> properties) {
        // FIXME: [rfeng] We need to check if a corresponding endpoint has been exported
        try {
            Contribution contribution = introspector.introspect(reference, properties);
            if (contribution != null) {

                NodeConfiguration configuration = nodeFactory.createNodeConfiguration();
                if (domainRegistry != null) {
                    configuration.setDomainRegistryURI(domainRegistry);
                }
                if (domainURI != null) {
                    configuration.setDomainURI(domainURI);
                }
                configuration.setURI(contribution.getURI());
                // configuration.getExtensions().add(reference.getBundle());
                Component component = contribution.getDeployables().get(0).getComponents().get(0);
                ComponentService service = component.getServices().get(0);
                service.getExtensions().addAll(getOSGiProperties(registry, reference));
                service.getExtensions().add(createOSGiProperty(registry,
                                                               ENDPOINT_FRAMEWORK_UUID,
                                                               getFrameworkUUID(reference.getBundle()
                                                                   .getBundleContext())));
                service.getExtensions().add(createOSGiProperty(registry, ENDPOINT_SERVICE_ID, reference
                    .getProperty(SERVICE_ID)));
                
                // create the org.osgi.sca.config.xml property  
                service.getExtensions().add(createSCAConfigXMLProperty(reference, properties, service));

                // FIXME: Configure the domain and node URI
                NodeImpl node = new NodeImpl(nodeFactory, configuration, Collections.singletonList(contribution));
                node.start();
                List<ExportRegistration> exportedServices = new ArrayList<ExportRegistration>();
                for (Endpoint endpoint : service.getEndpoints()) {
                    EndpointDescription endpointDescription = createEndpointDescription(context, endpoint);
                    synchronized (this) {
                        ExportReferenceImpl exportReference = exportReferences.get(endpointDescription);
                        if (exportReference == null) {
                            exportReference = new ExportReferenceImpl(node, reference, endpointDescription);
                        }
                        ExportRegistration exportRegistration = exportReference.register();
                        exportedServices.add(exportRegistration);
                    }
                }
                return exportedServices;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            ExportRegistration exportRegistration = new ExportRegistrationImpl(null, e);
            return Collections.singletonList(exportRegistration);
        }
    }

    public void modifiedService(ServiceReference reference, Object service) {
        removedService(reference, service);
        exportService(reference, null);
    }

    public void removedService(ServiceReference reference, Object service) {
        List<ExportRegistration> exportedServices = (List<ExportRegistration>)service;
        for (ExportRegistration exportRegistration : exportedServices) {
            exportRegistration.close();
        }
    }
    
    private OSGiProperty createSCAConfigXMLProperty(ServiceReference reference, Map<String, Object> props, ComponentService service){
    	
        // create the <sca-config> element
        String scaConfigXMLString = introspector.instrospectSCAConfig(reference, props, service);
        
        // wrap the <sca-config> element in an <xml> element
        String scaConfigXMLStringWithoutXMLHeader = scaConfigXMLString.substring(scaConfigXMLString.indexOf("><") + 1);
        scaConfigXMLString = "<xml>" + scaConfigXMLStringWithoutXMLHeader + "</xml>";
    	
    	// create and return the OSGI property
        return createOSGiProperty(registry, "org.osgi.sca.config.xml", scaConfigXMLString);
    }
}
