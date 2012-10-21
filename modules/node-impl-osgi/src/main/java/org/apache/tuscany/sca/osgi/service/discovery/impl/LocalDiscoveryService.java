/**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
package org.apache.tuscany.sca.osgi.service.discovery.impl;

import static org.apache.tuscany.sca.osgi.remoteserviceadmin.impl.OSGiHelper.getConfiguration;
import static org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID;
import static org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID;
import static org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.deployment.Deployer;
import org.apache.tuscany.sca.implementation.osgi.SCAConfig;
import org.apache.tuscany.sca.implementation.osgi.ServiceDescription;
import org.apache.tuscany.sca.implementation.osgi.ServiceDescriptions;
import org.apache.tuscany.sca.osgi.remoteserviceadmin.impl.OSGiHelper;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicySet;
import org.oasisopen.sca.ServiceRuntimeException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.osgi.util.tracker.ServiceTracker;

public class LocalDiscoveryService extends AbstractDiscoveryService implements BundleTrackerCustomizer {
    private Deployer deployer;
    private BundleTracker bundleTracker;
    private Collection<ExtenderConfiguration> extenders = new ArrayList<ExtenderConfiguration>();

    public LocalDiscoveryService(BundleContext context) {
        super(context);
    }

    public void start() {
        super.start();

        UtilityExtensionPoint utilities = this.registry.getExtensionPoint(UtilityExtensionPoint.class);
        this.deployer = utilities.getUtility(Deployer.class);
        bundleTracker = new BundleTracker(context, Bundle.ACTIVE | Bundle.STARTING, this);
        bundleTracker.open();
    }

    public static ServiceTracker getTracker(BundleContext context) {
        Filter filter = null;
        try {
            filter =
                context.createFilter("(& (" + Discovery.SUPPORTED_PROTOCOLS
                    + "=local) ("
                    + Constants.OBJECTCLASS
                    + "="
                    + Discovery.class.getName()
                    + "))");
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return new ServiceTracker(context, filter, null);
    }

    private EndpointDescription createEndpointDescription(ServiceDescription sd) {
        Map<String, Object> props = new HashMap<String, Object>(sd.getProperties());
        props.put(Constants.OBJECTCLASS, sd.getInterfaces().toArray(new String[sd.getInterfaces().size()]));
        if (!props.containsKey(ENDPOINT_SERVICE_ID)) {
            props.put(ENDPOINT_SERVICE_ID, Long.valueOf(System.currentTimeMillis()));
        }
        if (!props.containsKey(ENDPOINT_FRAMEWORK_UUID)) {
            props.put(ENDPOINT_FRAMEWORK_UUID, OSGiHelper.getFrameworkUUID(context));
        }
        if (!props.containsKey(ENDPOINT_ID)) {
            props.put(ENDPOINT_ID, UUID.randomUUID().toString());
        }

        EndpointDescription sed = new EndpointDescription(props);
        return sed;
    }

    private void removeServicesDeclaredInBundle(Bundle bundle) {
        for (Iterator<Map.Entry<EndpointDescription, Bundle>> i = endpointDescriptions.entrySet().iterator(); i.hasNext();) {
            Entry<EndpointDescription, Bundle> entry = i.next();
            if (entry.getValue().equals(bundle)) {
                serviceDescriptionRemoved(entry.getKey());
                i.remove();
            }
        }
    }

    private void serviceDescriptionAdded(EndpointDescription endpointDescription) {
        endpointChanged(endpointDescription, ADDED);
    }

    private void serviceDescriptionRemoved(EndpointDescription endpointDescription) {
        endpointChanged(endpointDescription, REMOVED);
    }

    public void stop() {
        if (bundleTracker != null) {
            bundleTracker.close();
        }
        super.stop();
    }

    public Object addingBundle(Bundle bundle, BundleEvent event) {
        if (bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null || bundle.getBundleId() == 0) {
            // Ignore fragments
            return null;
        }
        Collection<URL> scaConfigs = getConfiguration(bundle, "SCA-Configuration", "OSGI-INF/sca-config/*.xml");
        Collection<URL> descriptions = getConfiguration(bundle, "Remote-Service", null);
        if (scaConfigs.isEmpty() && descriptions.isEmpty()) {
            return null;
        }
        ExtenderConfiguration extender = new ExtenderConfiguration();
        for (URL url : scaConfigs) {
            try {
                SCAConfig scaConfig = deployer.loadXMLDocument(url, deployer.createMonitor());
                extender.scaConfigs.add(scaConfig);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new ServiceRuntimeException(e);
            }
        }
        for (URL url : descriptions) {
            try {
                ServiceDescriptions sds = deployer.loadXMLDocument(url, deployer.createMonitor());
                extender.remoteServiceDescriptions.add(sds);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                // throw new ServiceRuntimeException(e);
            }
        }
        
        // Add to the extenders before notifying the listeners (the endpoints may references to the config)
        ExtenderConfiguration.validate(extenders, extender);
        this.extenders.add(extender);

        // Notify
        for (ServiceDescriptions sds : extender.getRemoteServiceDescriptions()) {
            for (ServiceDescription sd : sds) {
                EndpointDescription sed = createEndpointDescription(sd);
                endpointDescriptions.put(sed, bundle);
                serviceDescriptionAdded(sed);
            }
        }

        return extender;
    }

    public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
        // STARTING --> ACTIVE
    }

    public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
        if (object instanceof ExtenderConfiguration) {
            extenders.remove((ExtenderConfiguration)object);
            removeServicesDeclaredInBundle(bundle);
        }
    }

    public Collection<ExtenderConfiguration> getConfigurations() {
        return extenders;
    }

    public static class ExtenderConfiguration {
        private Collection<SCAConfig> scaConfigs = new ArrayList<SCAConfig>();
        private Collection<ServiceDescriptions> remoteServiceDescriptions = new ArrayList<ServiceDescriptions>();

        public Collection<ServiceDescriptions> getRemoteServiceDescriptions() {
            return remoteServiceDescriptions;
        }

        public Collection<SCAConfig> getSCAConfigs() {
            return scaConfigs;
        }

        public static void validate(Collection<ExtenderConfiguration> configs, ExtenderConfiguration newConfig) {
            Map<QName, Binding> bindings = new HashMap<QName, Binding>();
            Map<QName, Intent> intents = new HashMap<QName, Intent>();
            Map<QName, PolicySet> policySets = new HashMap<QName, PolicySet>();

            for (ExtenderConfiguration config : configs) {
                for (SCAConfig c : config.getSCAConfigs()) {
                    addBindings(bindings, c);
                    addIntents(intents, c);
                    addPolicySets(policySets, c);
                }
            }
            for (SCAConfig c : newConfig.getSCAConfigs()) {
                addBindings(bindings, c);
                addIntents(intents, c);
                addPolicySets(policySets, c);
            }
        }

        private static void addIntents(Map<QName, Intent> intents, SCAConfig c) {
            for (Intent i: c.getIntents()) {
                if (intents.put(i.getName(), i) != null) {
                    throw new ServiceRuntimeException("Duplicate intent: " + i.getName());
                }
            }
        }
        
        private static void addPolicySets(Map<QName, PolicySet> policySets, SCAConfig c) {
            for (PolicySet ps: c.getPolicySets()) {
                if (policySets.put(ps.getName(), ps) != null) {
                    throw new ServiceRuntimeException("Duplicate policySet: " + ps.getName());
                }
            }
        }
        
        private static void addBindings(Map<QName, Binding> bindings, SCAConfig c) {
            for (Binding b : c.getBindings()) {
                QName name = new QName(c.getTargetNamespace(), b.getName());
                if (bindings.put(name, b) != null) {
                    throw new ServiceRuntimeException("Duplicate binding: " + name);
                }
            }
        }

    }

    @Override
    protected Dictionary<String, Object> getProperties() {
        Dictionary<String, Object> props = super.getProperties();
        props.put(SUPPORTED_PROTOCOLS, new String[] {"local"});
        return props;
    }

}
