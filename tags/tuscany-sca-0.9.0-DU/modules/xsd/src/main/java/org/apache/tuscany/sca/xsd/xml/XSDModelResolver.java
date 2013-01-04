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

package org.apache.tuscany.sca.xsd.xml;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.sca.assembly.xsd.Constants;
import org.apache.tuscany.sca.common.xml.XMLDocumentHelper;
import org.apache.tuscany.sca.contribution.Artifact;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.DefaultImport;
import org.apache.tuscany.sca.contribution.Import;
import org.apache.tuscany.sca.contribution.namespace.NamespaceImport;
import org.apache.tuscany.sca.contribution.processor.ContributionRuntimeException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.xsd.DefaultXSDFactory;
import org.apache.tuscany.sca.xsd.XSDFactory;
import org.apache.tuscany.sca.xsd.XSDefinition;
import org.apache.tuscany.sca.xsd.impl.XSDefinitionImpl;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.xml.sax.InputSource;

/**
 * A Model Resolver for XSD models.
 * 
 * @version $Rev: 1332565 $ $Date: 2012-05-01 09:15:15 +0100 (Tue, 01 May 2012) $
 */
public class XSDModelResolver implements ModelResolver {
    private static final String AGGREGATED_XSD = "http://tuscany.apache.org/aggregated.xsd";
    private XSDFactory factory;
    private Contribution contribution;
    private Map<String, List<XSDefinition>> map = new HashMap<String, List<XSDefinition>>();
    private XmlSchemaCollection schemaCollection;
    
    private static final byte[] schemaCollectionReadLock = new byte[0];

    public XSDModelResolver(Contribution contribution, FactoryExtensionPoint modelFactories) {
        this.contribution = contribution;
        this.schemaCollection = new XmlSchemaCollection();
        this.factory = new DefaultXSDFactory();
    }

    public void addModel(Object resolved, ProcessorContext context) {
        XSDefinition definition = (XSDefinition)resolved;
        List<XSDefinition> list = map.get(definition.getNamespace());
        if (list == null) {
            list = new ArrayList<XSDefinition>();
            map.put(definition.getNamespace(), list);
        }
        list.add(definition);
    }

    public Object removeModel(Object resolved, ProcessorContext context) {
        XSDefinition definition = (XSDefinition)resolved;
        List<XSDefinition> list = map.get(definition.getNamespace());
        if (list == null) {
            return null;
        } else {
            return list.remove(definition);
        }
    }

    public <T> T resolveModel(Class<T> modelClass, T unresolved, ProcessorContext context) {
        schemaCollection.setSchemaResolver(new URIResolverImpl(contribution, context));
    	XSDefinition definition = (XSDefinition)unresolved;        
        String namespace = definition.getNamespace();
        XSDefinition resolved = null;
        
        // Lookup a definition for the given namespace, within the contribution
        List<XSDefinition> list = map.get(namespace);
        
        if (list == null ||
            (list != null && list.size() == 0)){
            // if no schema is found locally delegate to other
            // contributions via the imports
            resolved = resolutionDelegation(namespace, context);
            return modelClass.cast(resolved);
        }
        
        XSDefinition modelXSD = null;
        if (list != null && definition.getDocument() != null) {
            // Set the document for the inline schema
            int index = list.indexOf(definition);
            if (index != -1) {  // a matching (not identical) document was found
                modelXSD = list.get(index);
                modelXSD.setDocument(definition.getDocument());
            }
        }
        if (list == null && definition.getDocument() != null) {
            // Hit for the 1st time
            list = new ArrayList<XSDefinition>();
            list.add(definition);
            map.put(namespace, list);
        }        
        try {
            resolved = aggregate(list);
        } catch (IOException e) {
            throw new ContributionRuntimeException(e);
        }
        if (resolved != null && !resolved.isUnresolved()) {
            if (definition.isUnresolved() && definition.getSchema() == null && modelXSD != null) {
                // Update the unresolved model with schema information and mark it
                // resolved.  This information in the unresolved model is needed when
                // this method is called by WSDLModelResolver.readInlineSchemas().
                definition.setSchema(modelXSD.getSchema());
                definition.setSchemaCollection(modelXSD.getSchemaCollection());
                definition.setUnresolved(false);
            }
            return modelClass.cast(resolved);
        }
        
        return modelClass.cast(unresolved);
    }

    private void loadOnDemand(XSDefinition definition) throws IOException {
        
        // It might be possible to use a per-XSDModelResolver-instance lock instead of the singleton lock,
        // since for a deadlock to occur it would seem to require something along the lines of A imports B imports A. 
        // Since I'm not sure precisely what the restriction against circular imports is, and since I don't think it's too bad
        // to use the singleton lock (after all, loading is, in general, a one-time thing), I'll just use the singleton lock.
        synchronized (schemaCollectionReadLock) {

            if (definition.getSchema() != null) {
                return;
            }
            if (definition.getDocument() != null) {
                String uri = null;
                if (definition.getLocation() != null) {
                    uri = definition.getLocation().toString();
                }
                XmlSchema schema = null;
                try {
                    final XSDefinition finaldef = definition;
                    final String finaluri = uri;
                    try {
                        schema = (XmlSchema) AccessController.doPrivileged(new PrivilegedExceptionAction<XmlSchema>() {
                            public XmlSchema run() throws IOException {
                                return schemaCollection.read(finaldef.getDocument(), finaluri, null);
                            }
                        });
                    } catch (PrivilegedActionException e) {
                        throw (IOException) e.getException();
                    }
                } catch (IOException e) {
                    throw new ContributionRuntimeException(e);
                } catch (RuntimeException e) {
                    // find original cause of the problem
                    Throwable cause = e;
                    while (cause.getCause() != null && cause != cause.getCause()) {
                        cause = cause.getCause();
                    }
                    throw new ContributionRuntimeException(cause);
                }
                definition.setSchemaCollection(schemaCollection);
                definition.setSchema(schema);
                definition.setUnresolved(false);
            } else if (definition.getLocation() != null) {
                if (definition.getLocation().getFragment() != null) {
                    // It's an inline schema
                    return;
                }
                // Read an XSD document
                XmlSchema schema = null;
                for (XmlSchema d : schemaCollection.getXmlSchemas()) {
                    if (isSameNamespace(d.getTargetNamespace(), definition.getNamespace()))  {
                        if (d.getSourceURI().equals(definition.getLocation().toString())) {
                            schema = d;
                            break;
                        }
                    }
                }
                if (schema == null) {
                    InputSource xsd = null;
                    final XSDefinition finaldef = definition;
                    try {
                        try {
                            xsd = (InputSource) AccessController.doPrivileged(new PrivilegedExceptionAction<InputSource>() {
                                public InputSource run() throws IOException {
                                    return XMLDocumentHelper.getInputSource(finaldef.getLocation().toURL());
                                }
                            });
                        } catch (PrivilegedActionException e) {
                            throw (IOException) e.getException();
                        }
                    } catch (IOException e) {
                        throw new ContributionRuntimeException(e);
                    }
    
                    try {
                        final InputSource finalxsd = xsd;
                        try {
                            schema = (XmlSchema) AccessController.doPrivileged(new PrivilegedExceptionAction<XmlSchema>() {
                                public XmlSchema run() throws IOException {
                                    return schemaCollection.read(finalxsd, null);
                                }
                            });
                        } catch (PrivilegedActionException e) {
                            throw (IOException) e.getException();
                        }
    
                    } catch (IOException e) {
                        throw new ContributionRuntimeException(e);
                    } catch (RuntimeException e) {
                        // find original cause of the problem
                        Throwable cause = e;
                        while (cause.getCause() != null && cause != cause.getCause()) {
                            cause = cause.getCause();
                        }
                        throw new ContributionRuntimeException(cause);
                    }
                }
                definition.setSchemaCollection(schemaCollection);
                definition.setSchema(schema);
            }
        }
    }

    private boolean isSameNamespace(String ns1, String ns2) {
        if (ns1 == null) {
            return ns2 == null;
        } else {
            return ns1.equals(ns2);
        }
    }
    /**
     * Create a facade XmlSchema which includes all the definitions
     * 
     * @param definitions A list of the XmlSchema under the same target
     *                namespace
     * @return The aggregated XmlSchema
     */
    private XSDefinition aggregate(List<XSDefinition> definitions) throws IOException {
        if (definitions == null || definitions.size() == 0) {
            return null;
        }
        if (definitions.size() == 1) {
            XSDefinition d = definitions.get(0);
            loadOnDemand(d);
            return d;
        }
        XSDefinition aggregated = factory.createXSDefinition();
        for (XSDefinition d : definitions) {
            loadOnDemand(d);
        }
        String ns = definitions.get(0).getNamespace();
        
        XmlSchema facade = null;
        // Check if the facade XSD is already in the collection
        for (XmlSchema s : schemaCollection.getXmlSchema(AGGREGATED_XSD)) {
            if (ns.equals(s.getTargetNamespace())) {
                facade = s;
                break;
            }
        }
        if (facade == null) {
            // This will add the facade into the collection
            facade = new XmlSchema(ns, AGGREGATED_XSD, schemaCollection);
        }

        for (XmlSchema d : schemaCollection.getXmlSchemas()) {
            if (ns.equals(d.getTargetNamespace())) {
                if (d == facade) {
                    continue;
                }
                XmlSchemaInclude include = new XmlSchemaInclude();
                include.setSchema(d);
                include.setSourceURI(d.getSourceURI());
                include.setSchemaLocation(d.getSourceURI());
                facade.getIncludes().add(include);
                facade.getItems().add(include);
            }
        }
        aggregated.setUnresolved(true);
        aggregated.setSchema(facade);
        aggregated.setNamespace(ns);
        aggregated.setAggregatedDefinitions(definitions);
        aggregated.setUnresolved(false);

        // FIXME: [rfeng] This is hacky
        //definitions.clear();
        //definitions.add(aggregated);
        return aggregated;
    }

    private XSDefinition resolutionDelegation(String namespace, ProcessorContext context){
        // Delegate the resolution to namespace imports
        XSDefinition resolved = null;                
        XSDefinition unresolved = new XSDefinitionImpl();
        unresolved.setUnresolved(true);
        unresolved.setNamespace(namespace);
                        
        for (Import import_ : this.contribution.getImports()) {
            if (import_ instanceof NamespaceImport) {
                NamespaceImport namespaceImport = (NamespaceImport)import_;
                if (namespaceImport.getNamespace().equals(namespace)) {                           
                    // Delegate the resolution to the namespace import resolver
                    resolved =
                        namespaceImport.getModelResolver().resolveModel(XSDefinition.class, (XSDefinition)unresolved, context);
                    if (!resolved.isUnresolved()) {
                        return resolved;
                    }
                }
            } else if (import_ instanceof DefaultImport) {
                // Delegate the resolution to the default import resolver
                resolved =
                    import_.getModelResolver().resolveModel(XSDefinition.class, (XSDefinition)unresolved, context);
                if (!resolved.isUnresolved()) {
                    return resolved;
                }
            }
        }
        
        return resolved;
    }
    
    /**
     * URI resolver implementation for XML schema
     */
    public static class URIResolverImpl implements URIResolver {
        private Contribution contribution;
        private ProcessorContext context;

        public URIResolverImpl(Contribution contribution, ProcessorContext context) {
            this.contribution = contribution;
            this.context = context;
        }

        public org.xml.sax.InputSource resolveEntity(java.lang.String targetNamespace,
                                                     java.lang.String schemaLocation,
                                                     java.lang.String baseUri) {
            try {
                if (schemaLocation == null) {
                    return null;
                }
                URL url = null;
                
                // Delegate the resolution to namespace imports
                XSDefinition resolved = null;                
                XSDefinition unresolved = new XSDefinitionImpl();
                unresolved.setUnresolved(true);
                unresolved.setLocation(new URI(schemaLocation));
                unresolved.setNamespace(targetNamespace);
                                
                for (Import import_ : this.contribution.getImports()) {
                    URL resolvedURL;
                    if (import_ instanceof NamespaceImport) {
                        NamespaceImport namespaceImport = (NamespaceImport)import_;
                        if (namespaceImport.getNamespace().equals(targetNamespace)) {                        	
                        	// Delegate the resolution to the namespace import resolver
        	                resolved =
        	                    namespaceImport.getModelResolver().resolveModel(XSDefinition.class, (XSDefinition)unresolved, context);
        	                if (!resolved.isUnresolved()) {
                                resolvedURL = resolved.getLocation().toURL();
                                return xmlDocumentHelperGetInputSource(resolvedURL);
        	                }
                        }
                    } else if (import_ instanceof DefaultImport) {
                        // Delegate the resolution to the default import resolver
                        resolved =
                            import_.getModelResolver().resolveModel(XSDefinition.class, (XSDefinition)unresolved, context);
                        if (!resolved.isUnresolved()) {
                            resolvedURL = resolved.getLocation().toURL();
                            return xmlDocumentHelperGetInputSource(resolvedURL);
                        }
                    }
                }
                
                // Not found, lookup a definition for the given namespace
                // within the current contribution.
                if (schemaLocation.startsWith("/")) {
                    // The URI is relative to the contribution
                    String uri = schemaLocation.substring(1);
                    for (Artifact a : contribution.getArtifacts()) {
                        if (a.getURI().equals(uri)) {
                            url = new URL(a.getLocation());
                            break;
                        }
                    }
                    if (url == null) {
                        // URI not found in the contribution; return a default InputSource
                        // so that the XmlSchema code will produce a useful diagnostic
                        return new InputSource(schemaLocation);
                    }
                } else {
                    url = new URL(new URL(baseUri), schemaLocation);
                    String scheme = url.getProtocol();
                    if ("file".equalsIgnoreCase(scheme) || "jar".equalsIgnoreCase(scheme)
                        || "zip".equalsIgnoreCase(scheme)
                        || "wsjar".equalsIgnoreCase(scheme)) {
                        // For local URLs, use as-is
                    } else {
                        // look to see whether Tuscany has a local version of the
                        // required schema. It can load the local version rather 
                        // than going out to the network in order to improve performance
                        URL cached = Constants.CACHED_XSDS.get(targetNamespace);
                        if (cached != null) {
                            url = cached;
                        }
                    }
                }
                return xmlDocumentHelperGetInputSource(url);
                
            } catch (IOException e) {
                // If we are not able to resolve the imports using location, then 
                // try resolving them using the namespace.
                try {
                    for (Artifact artifact : contribution.getArtifacts()) {
                        if (artifact.getModel() instanceof XSDefinitionImpl) {
                            String artifactNamespace = ((XSDefinitionImpl)artifact.getModel()).getNamespace();
                            if (targetNamespace.equals(artifactNamespace)) {
                                final URL artifactLocation = ((XSDefinitionImpl)artifact.getModel()).getLocation().toURL();
                                return xmlDocumentHelperGetInputSource(artifactLocation);
                            }
                        }
                    }
                    // add another default return statement
                    return new InputSource(schemaLocation);
                } catch (IOException ioe) {
                    // Invalid URI; return a default InputSource so that the
                    // XmlSchema code will produce a useful diagnostic
                    return new InputSource(schemaLocation);
                }
            } catch (URISyntaxException e) {
            	// Invalid URI; return a default InputSource so that the
                // XmlSchema code will produce a useful diagnostic
                return new InputSource(schemaLocation);
            }
        }
        
        private InputSource xmlDocumentHelperGetInputSource(final URL url) throws IOException {
            try {
                return (InputSource)AccessController.doPrivileged( new PrivilegedExceptionAction<InputSource>() {
                    public InputSource run() throws IOException {                                    
                        return XMLDocumentHelper.getInputSource(url);
                    }
                });
            } catch (PrivilegedActionException pae) {
                throw (IOException) pae.getException();
            }
        }
    }

}
