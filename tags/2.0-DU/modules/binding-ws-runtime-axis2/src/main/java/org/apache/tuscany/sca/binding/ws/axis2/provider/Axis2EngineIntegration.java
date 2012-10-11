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

package org.apache.tuscany.sca.binding.ws.axis2.provider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.Types;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.ModuleBuilder;
import org.apache.axis2.deployment.URLBasedAxisConfigurator;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.Version;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.WSDLToAxisServiceBuilder;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.tuscany.sca.assembly.AbstractContract;
import org.apache.tuscany.sca.binding.ws.WebServiceBinding;
import org.apache.tuscany.sca.common.xml.XMLDocumentHelper;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.extensibility.ClassLoaderContext;
import org.apache.tuscany.sca.extensibility.ServiceDiscovery;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.apache.tuscany.sca.xsd.XSDefinition;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.oasisopen.sca.ServiceRuntimeException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Axis2EngineIntegration {
 
    //=========================================================
    // most of the following is related to rewriting WSDL imports 
    // I'd like to move this but don't know where to yet. 
    
    public static final String IMPORT_TAG = "import";
    public static final String INCLUDE_TAG = "include";

    public static final QName QNAME_WSA_ADDRESS =
        new QName(AddressingConstants.Final.WSA_NAMESPACE, AddressingConstants.EPR_ADDRESS);
    public static final QName QNAME_WSA_FROM =
        new QName(AddressingConstants.Final.WSA_NAMESPACE, AddressingConstants.WSA_FROM);
    public static final QName QNAME_WSA_REFERENCE_PARAMETERS =
        new QName(AddressingConstants.Final.WSA_NAMESPACE, AddressingConstants.EPR_REFERENCE_PARAMETERS);

    //Schema element names
    public static final String ELEM_SCHEMA = "schema";

    //Schema URI
    public static final String NS_URI_XSD_1999 = "http://www.w3.org/1999/XMLSchema";
    public static final String NS_URI_XSD_2000 = "http://www.w3.org/2000/10/XMLSchema";
    public static final String NS_URI_XSD_2001 = "http://www.w3.org/2001/XMLSchema";

    //Schema QNames
    public static final QName Q_ELEM_XSD_1999 = new QName(NS_URI_XSD_1999, ELEM_SCHEMA);
    public static final QName Q_ELEM_XSD_2000 = new QName(NS_URI_XSD_2000, ELEM_SCHEMA);
    public static final QName Q_ELEM_XSD_2001 = new QName(NS_URI_XSD_2001, ELEM_SCHEMA);
    public static final List<QName> XSD_QNAME_LIST =
        Arrays.asList(new QName[] {Q_ELEM_XSD_1999, Q_ELEM_XSD_2000, Q_ELEM_XSD_2001});
    
    //=========================================================  
    
    /*
     * Create the whole configuration context for the Axis engine
     */
    private static class Axis2Config {
        private ClassLoaderContext classLoaderContext;
        private URL axis2xmlURL;
        private URL repositoryURL;
    }

    // Cache the discovered axis2 configuration but we need to create a new instance of ConfigurationContext every time
    private static Axis2Config axis2Config;

    public synchronized static ConfigurationContext getAxisConfigurationContext(final ServiceDiscovery serviceDiscovery) {

        // get the axis configuration context from the Tuscany axis2.xml file
        // Allow privileged access to read properties. Requires PropertyPermission read in
        // security policy.
        if (axis2Config == null) {
            try {
                axis2Config = AccessController.doPrivileged(new PrivilegedExceptionAction<Axis2Config>() {
                    public Axis2Config run() throws AxisFault, MalformedURLException {
                        // collect together the classloaders that Axis2 requires in order to load
                        // pluggable items such as the Tuscany MessageReceivers and the Xerces 
                        // document builder. 
                        ClassLoader wsBindingCL = getClass().getClassLoader();
                        ClassLoader axis2CL = URLBasedAxisConfigurator.class.getClassLoader();
                        ClassLoaderContext classLoaderContext = new ClassLoaderContext(wsBindingCL, axis2CL);

                        classLoaderContext = new ClassLoaderContext(classLoaderContext.getClassLoader(), 
                                                                    serviceDiscovery,
                                                                    XMLInputFactory.class, 
                                                                    DocumentBuilderFactory.class);

                        URL axis2xmlURL = wsBindingCL.getResource("org/apache/tuscany/sca/binding/ws/axis2/engine/conf/tuscany-axis2.xml");
                        
                        if (axis2xmlURL != null) {
                            URL repositoryURL = new URL(axis2xmlURL, "../repository/");
                            Axis2Config config = new Axis2Config();
                            config.classLoaderContext = classLoaderContext;
                            config.axis2xmlURL = axis2xmlURL;
                            config.repositoryURL = repositoryURL;
                            return config;
                        } else {
                            return null;
                        }
                    }
                });
            } catch (PrivilegedActionException e) {
                throw new ServiceRuntimeException(e.getException());
            }
        }

        if (axis2Config == null) {
            return null;
        }

        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<ConfigurationContext>() {
                public ConfigurationContext run() throws AxisFault {
                    ClassLoader oldTCCL = axis2Config.classLoaderContext.setContextClassLoader();
                    try {
                        ConfigurationContext configurationContext =
                            ConfigurationContextFactory.createConfigurationContextFromURIs(axis2Config.axis2xmlURL,
                                                                                           axis2Config.repositoryURL);
                        return configurationContext;
                    } finally {
                        if (oldTCCL != null) {
                            Thread.currentThread().setContextClassLoader(oldTCCL);
                        }
                    }
                }
            });
        } catch (PrivilegedActionException e) {
            throw new ServiceRuntimeException(e.getException());
        }
    }
    
    // Some code to programatically load an Axis2 module only if we need it
    // currently hard coded to load rampart only. Needs generalization
    public synchronized static void loadRampartModule(ConfigurationContext axis2ConfigContext) {
        try {
            final AxisConfiguration axisConfiguration = axis2ConfigContext.getAxisConfiguration();
            final URL rampartURL = new URL(axis2Config.repositoryURL.toString() + "modules/rampart-1.4.mar");
            
            ClassLoader deploymentClassLoader = org.apache.axis2.deployment.util.Utils.createClassLoader(
                    new URL[]{rampartURL},
                    axisConfiguration.getModuleClassLoader(),
                    true,
                    (File) axisConfiguration.getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR));
            
            final AxisModule module = new AxisModule();
            module.setModuleClassLoader(deploymentClassLoader);
            module.setParent(axisConfiguration);

            if (module.getName() == null) {
                module.setName("rampart-1.4");
                module.setVersion(new Version("1.4"));
            }
            
            populateModule(axis2ConfigContext, module, rampartURL);
            module.setFileName(rampartURL);
            
            // Allow privileged access to read properties. Requires PropertiesPermission read in
            // security policy.
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                    public Object run() throws IOException {
                        DeploymentEngine.addNewModule(module, axisConfiguration);
                        return null;
                    }
                });
            } catch (PrivilegedActionException e) {
                throw (AxisFault)e.getException();
            }            
           
            org.apache.axis2.util.Utils.calculateDefaultModuleVersion(axisConfiguration.getModules(), 
                                                                      axisConfiguration);
            axisConfiguration.validateSystemPredefinedPhases();
        } catch (IOException e) {
            throw new ServiceRuntimeException(e);
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }   
    
    private static void populateModule(ConfigurationContext axis2ConfigContext, AxisModule module, URL moduleUrl) throws DeploymentException {
        try {
            ClassLoader classLoader = module.getModuleClassLoader();
            InputStream moduleStream = classLoader.getResourceAsStream("META-INF/module.xml");
            if (moduleStream == null) {
                moduleStream = classLoader.getResourceAsStream("meta-inf/module.xml");
            }
            if (moduleStream == null) {
                throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.MODULE_XML_MISSING, moduleUrl.toString()));
            }
            ModuleBuilder moduleBuilder = new ModuleBuilder(moduleStream, module, axis2ConfigContext.getAxisConfiguration());
            moduleBuilder.populateModule();
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
    }    
    
    //=========================================================  
    
    /**
     * Create an AxisService from the Java interface class of the SCA service interface
     */
    public static AxisService createJavaAxisService(String endpointURL, 
                                                    ConfigurationContext configContext, 
                                                    AbstractContract contract) throws AxisFault {
        AxisService axisService = new AxisService();
        String path = URI.create(endpointURL).getPath();
        axisService.setName(path);
        axisService.setServiceDescription("Tuscany configured AxisService for service: " + endpointURL);
        axisService.setClientSide(false);
        Parameter classParam =
            new Parameter(Constants.SERVICE_CLASS, 
                          ((JavaInterface)contract.getInterfaceContract().getInterface()).getJavaClass().getName());
        axisService.addParameter(classParam);
        try {
            Utils.fillAxisService(axisService, configContext.getAxisConfiguration(), null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return axisService;
    }

    //=========================================================
    
    /**
     * Create an AxisService from the WSDL doc used by ws binding
     */
    public static AxisService createWSDLAxisService(String endpointURL, 
                                                    Port port, 
                                                    WebServiceBinding wsBinding) throws AxisFault {

        Definition definition = wsBinding.getGeneratedWSDLDocument();
        QName serviceQName = wsBinding.getService().getQName();
        Definition def = getDefinition(definition, serviceQName);

        ClassLoader oldTCCL = axis2Config.classLoaderContext.setContextClassLoader();
        final WSDLToAxisServiceBuilder builder;
        try {
        	builder = new WSDL11ToAxisServiceBuilder(def, serviceQName, port.getName());
        } finally {
            if (oldTCCL != null) {
                Thread.currentThread().setContextClassLoader(oldTCCL);
            }
        }
        //final WSDLToAxisServiceBuilder builder = new WSDL11ToAxisServiceBuilder(def, serviceQName, port.getName());
        
        
        builder.setServerSide(true);
        // [rfeng] Add a custom resolver to work around WSCOMMONS-228
        // TODO - 228 is resolved, is this still required
        builder.setCustomResolver(new URIResolverImpl(def));
        builder.setBaseUri(def.getDocumentBaseURI());
        // [rfeng]
        // AxisService axisService = builder.populateService();
        // Allow privileged access to read properties. Requires PropertiesPermission read in
        // security policy.
        AxisService axisService;
        try {
            axisService = AccessController.doPrivileged(new PrivilegedExceptionAction<AxisService>() {
                public AxisService run() throws AxisFault {
                    return builder.populateService();
                }
            });
        } catch (PrivilegedActionException e) {
            throw (AxisFault)e.getException();
        }

        String name = URI.create(endpointURL).getPath();
        //[nash] HTTP endpoints need a leading slash for WSDL imports to work with ?wsdl
        if (endpointURL.startsWith("jms")) {
            name = name.startsWith("/") ? name.substring(1) : name;
        }
        axisService.setName(name);
        axisService.setEndpointURL(endpointURL);
        axisService.setDocumentation("Tuscany configured AxisService for service: " + endpointURL);
        
        // TODO - again, do we ever have more than one endpoint
        //        on the service side?
        for (Iterator i = axisService.getEndpoints().values().iterator(); i.hasNext();) {
            AxisEndpoint ae = (AxisEndpoint)i.next();
            if (endpointURL.startsWith("jms")) {
// not in Axis2 1.5.1
//                Parameter qcf = new Parameter(JMSConstants.CONFAC_PARAM, null);
//                qcf.setValue(DEFAULT_QUEUE_CONNECTION_FACTORY);
//                axisService.addParameter(qcf);
                break;
            }
        }

        // Add schema information to the AxisService (needed for "?xsd=" support)
        addSchemas(wsBinding.getUserSpecifiedWSDLDefinition(), axisService);

        // Use the existing WSDL
        Parameter wsdlParam = new Parameter("wsdl4jDefinition", null);
        wsdlParam.setValue(definition);
        axisService.addParameter(wsdlParam);
        Parameter userWSDL = new Parameter("useOriginalwsdl", "true");
        axisService.addParameter(userWSDL);

        // Modify schema imports and includes to add "servicename?xsd=" prefix.
        // Axis2 does this for schema extensibility elements, but Tuscany has
        // overriden the WSDl4J deserializer to create UnknownExtensibilityElement
        // elements in place of these.
        modifySchemaImportsAndIncludes(definition, name);

        // Axis2 1.3 has a bug with returning incorrect values for the port
        // addresses.  To work around this, compute the values here.
        Parameter modifyAddr = new Parameter("modifyUserWSDLPortAddress", "false");
        axisService.addParameter(modifyAddr);

        return axisService;
    }
    
    
    /**
     * Workaround for https://issues.apache.org/jira/browse/AXIS2-3205
     */
    private static Definition getDefinition(Definition definition, QName serviceName) {

        if (serviceName == null) {
            return definition;
        }

        if (definition == null) {
            return null;
        }
        Object service = definition.getServices().get(serviceName);
        if (service != null) {
            return definition;
        }
        for (Object i : definition.getImports().values()) {
            List<Import> imports = (List<Import>)i;
            for (Import imp : imports) {
                Definition d = getDefinition(imp.getDefinition(), serviceName);
                if (d != null) {
                    return d;
                }
            }
        }
        return null;
    }     
    
    private static void addSchemas(WSDLDefinition wsdlDef, AxisService axisService) {
        for (XSDefinition xsDef : wsdlDef.getXmlSchemas()) {
            if (xsDef.getSchema() != null) {
                axisService.addSchema(xsDef.getSchema());
                updateSchemaRefs(xsDef.getSchema(), axisService.getName());
            }
        }
        for (WSDLDefinition impDef : wsdlDef.getImportedDefinitions()) {
            addSchemas(impDef, axisService);
        }
    }

    private static void updateSchemaRefs(XmlSchema parentSchema, String name) {
        for (Iterator iter = parentSchema.getIncludes().getIterator(); iter.hasNext();) {
            Object obj = iter.next();
            if (obj instanceof XmlSchemaExternal) {
                XmlSchemaExternal extSchema = (XmlSchemaExternal)obj;
                String location = extSchema.getSchemaLocation();
                if (location.length() > 0 && location.indexOf(":/") < 0 && location.indexOf("?xsd=") < 0) {
                    extSchema.setSchemaLocation(name + "?xsd=" + location);
                }
                if (extSchema.getSchema() != null) {
                    updateSchemaRefs(extSchema.getSchema(), name);
                }
            }
        }
    }    
    
    private static void modifySchemaImportsAndIncludes(Definition definition, String name) {
        // adjust the schema locations in types section
        Types types = definition.getTypes();
        if (types != null) {
            for (Iterator iter = types.getExtensibilityElements().iterator(); iter.hasNext();) {
                Object ext = iter.next();
                if (ext instanceof UnknownExtensibilityElement && XSD_QNAME_LIST
                    .contains(((UnknownExtensibilityElement)ext).getElementType())) {
                    changeLocations(((UnknownExtensibilityElement)ext).getElement(), name);
                }
            }
        }
        for (Iterator iter = definition.getImports().values().iterator(); iter.hasNext();) {
            Vector values = (Vector)iter.next();
            for (Iterator valuesIter = values.iterator(); valuesIter.hasNext();) {
                Import wsdlImport = (Import)valuesIter.next();
                modifySchemaImportsAndIncludes(wsdlImport.getDefinition(), name);
            }
        }
    }

    private static void changeLocations(Element element, String name) {
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            String tagName = nodeList.item(i).getLocalName();
            if (IMPORT_TAG.equals(tagName) || INCLUDE_TAG.equals(tagName)) {
                processImport(nodeList.item(i), name);
            }
        }
    }

    private static void processImport(Node importNode, String name) {
        NamedNodeMap nodeMap = importNode.getAttributes();
        for (int i = 0; i < nodeMap.getLength(); i++) {
            Node attribute = nodeMap.item(i);
            if (attribute.getNodeName().equals("schemaLocation")) {
                String location = attribute.getNodeValue();
                if (location.indexOf(":/") < 0 & location.indexOf("?xsd=") < 0) {
                    attribute.setNodeValue(name + "?xsd=" + location);
                }
            }
        }
    }    
    
    //=========================================================  
    
    /*
     * Create the service message receivers and the service provider that will push 
     * messages out onto the binding wire
     */
    public static void createAxisServiceProviders(AxisService axisService, 
                                                  RuntimeEndpoint endpoint,
                                                  WebServiceBinding wsBinding,
                                                  ExtensionPointRegistry extensionPoints) {
        for (Iterator<?> i = axisService.getOperations(); i.hasNext();) {
            AxisOperation axisOp = (AxisOperation)i.next();
            Operation op = getOperation(axisOp, wsBinding);
            if (op != null) {

                if (op.isNonBlocking()) {
                    axisOp.setMessageExchangePattern(WSDL2Constants.MEP_URI_IN_ONLY);
                } else {
                    axisOp.setMessageExchangePattern(WSDL2Constants.MEP_URI_IN_OUT);
                }

                MessageReceiver msgrec = null;
                TuscanyServiceProvider serviceProvider = new TuscanyServiceProvider(extensionPoints, endpoint, wsBinding, op);
                if (op.isNonBlocking()) {
                    msgrec = new Axis2ServiceInMessageReceiver(serviceProvider);
                } else {
                    msgrec = new Axis2ServiceInOutSyncMessageReceiver(serviceProvider);
                }
                axisOp.setMessageReceiver(msgrec);
            }
        }
    }    
    
    private static Operation getOperation(AxisOperation axisOp,WebServiceBinding wsBinding) {
        String operationName = axisOp.getName().getLocalPart();
        Interface iface = wsBinding.getBindingInterfaceContract().getInterface();
        for (Operation op : iface.getOperations()) {
            if (op.getName().equalsIgnoreCase(operationName)) {
                return op;
            }
        }
        return null;
    }  
    
    //========================================================= 
    
    public static String getPortAddress(Port port) {
        List<?> es = port.getExtensibilityElements();
        if (es != null && es.size() > 0) {
            Object ext = es.get(0);
            if (ext instanceof SOAPAddress) {
                return ((SOAPAddress)ext).getLocationURI();
            }
            if (ext instanceof SOAP12Address) {
                return ((SOAP12Address)ext).getLocationURI();
            }
        }
        return null;
    }

    public static void setPortAddress(Port port, String locationURI) {
        Object ext = port.getExtensibilityElements().get(0);
        if (ext instanceof SOAPAddress) {
            ((SOAPAddress)ext).setLocationURI(locationURI);
        }
        if (ext instanceof SOAP12Address) {
            ((SOAP12Address)ext).setLocationURI(locationURI);
        }
    } 
    
    /**
     * This method is copied from AxisService.createClientSideAxisService to
     * work around http://issues.apache.org/jira/browse/WSCOMMONS-228
     * 
     * @param wsdlDefinition
     * @param wsdlServiceName
     * @param portName
     * @param options
     * @return
     * @throws AxisFault
     */
    @Deprecated
    public static AxisService createClientSideAxisService(Definition definition,
                                                          QName serviceName,
                                                          String portName,
                                                          Options options) throws AxisFault {
        Definition def = getDefinition(definition, serviceName);
        final WSDL11ToAxisServiceBuilder serviceBuilder = new WSDL11ToAxisServiceBuilder(def, serviceName, portName);
        serviceBuilder.setServerSide(false);
        // [rfeng] Add a custom resolver to work around WSCOMMONS-228
        serviceBuilder.setCustomResolver(new URIResolverImpl(def));
        serviceBuilder.setBaseUri(def.getDocumentBaseURI());
        // [rfeng]
        // Allow access to read properties. Requires PropertiesPermission in security policy.
        AxisService axisService;         
        try {        
            axisService = AccessController.doPrivileged(new PrivilegedExceptionAction<AxisService>() {
                public AxisService run() throws AxisFault {
                    return serviceBuilder.populateService();
                }
            });
            } catch ( PrivilegedActionException e ) {
               throw (AxisFault) e.getException();
            }

        AxisEndpoint axisEndpoint = (AxisEndpoint)axisService.getEndpoints().get(axisService.getEndpointName());
        options.setTo(new EndpointReference(axisEndpoint.getEndpointURL()));
        if (axisEndpoint != null) {
            options.setSoapVersionURI((String)axisEndpoint.getBinding().getProperty(WSDL2Constants.ATTR_WSOAP_VERSION));
        }
        return axisService;
    }
    
    /**
     * URI resolver implementation for XML schema
     */
    public static class URIResolverImpl implements URIResolver {
        private Definition definition;

        public URIResolverImpl(Definition definition) {
            this.definition = definition;
        }

        public org.xml.sax.InputSource resolveEntity(java.lang.String targetNamespace,
                                                     java.lang.String schemaLocation,
                                                     java.lang.String baseUri) {
            try {
                if (baseUri == null) {
                    baseUri = definition.getDocumentBaseURI();
                }
                URL url = new URL(new URL(baseUri), schemaLocation);
                return XMLDocumentHelper.getInputSource(url); 
            } catch (IOException e) {
                return null;
            }
        }
    }    
   
}
