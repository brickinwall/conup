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
package org.apache.tuscany.sca.databinding.sdo;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.apache.tuscany.sca.databinding.sdo.ImportSDO.IMPORT_SDO;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.contribution.Artifact;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ClassReference;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.apache.tuscany.sca.monitor.impl.ProblemImpl;

import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.XSDHelper;

/**
 * Loader that handles &lt;import.sdo&gt; elements.
 * 
 * @version $Rev: 926885 $ $Date: 2010-03-24 00:56:09 +0000 (Wed, 24 Mar 2010) $
 * @deprecated
 */
@Deprecated
public class ImportSDOProcessor implements StAXArtifactProcessor<ImportSDO> {
    
    private ContributionFactory contributionFactory;

    public ImportSDOProcessor(ExtensionPointRegistry registry) {
        FactoryExtensionPoint modelFactories = registry.getExtensionPoint(FactoryExtensionPoint.class);
        this.contributionFactory = modelFactories.getFactory(ContributionFactory.class);
    }
    
    /**
     * Report a error.
     * 
     * @param problems
     * @param message
     * @param model
     */
    private void error(Monitor monitor, String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem =
                new ProblemImpl(this.getClass().getName(), "databinding-sdo-validation-messages", Severity.ERROR,
                                message, model, message, (Object[])messageParameters);
            monitor.problem(problem);
        }
    }
     
     /**
      * Report a exception.
      * 
      * @param problems
      * @param message
      * @param model
      */
    private void error(Monitor monitor, String message, Object model, Exception ex) {
     	 if (monitor != null) {
     		 Problem problem = new ProblemImpl(this.getClass().getName(), "databinding-sdo-validation-messages", Severity.ERROR, message,model, message, ex);
     	     monitor.problem(problem);
     	 }        
    }

    public QName getXMLType() {
        return IMPORT_SDO;
    }

    public ImportSDO read(XMLStreamReader reader,ProcessorContext context) throws ContributionReadException, XMLStreamException {
        assert IMPORT_SDO.equals(reader.getName());

        // FIXME: How do we associate the application HelperContext with the one
        // imported by the composite
        ImportSDO importSDO = new ImportSDO(SDOContextHelper.getDefaultHelperContext());
        String factoryName = reader.getAttributeValue(null, "factory");
        if (factoryName != null) {
            importSDO.setFactoryClassName(factoryName);
        }
        String location = reader.getAttributeValue(null, "location");
        if (location != null) {
            importSDO.setSchemaLocation(location);
        }

        // Skip to end element
        while (reader.hasNext()) {
            if (reader.next() == END_ELEMENT && ImportSDO.IMPORT_SDO.equals(reader.getName())) {
                break;
            }
        }
        return importSDO;
    }

    private void importFactory(ImportSDO importSDO, ModelResolver resolver,ProcessorContext context) throws ContributionResolveException {
        String factoryName = importSDO.getFactoryClassName();
        if (factoryName != null) {
            ClassReference reference = new ClassReference(factoryName);
            ClassReference resolved = resolver.resolveModel(ClassReference.class, reference,context);
            if (resolved != null && !resolved.isUnresolved()) {
            	try {
                    Class<?> factoryClass = resolved.getJavaClass();
                    register(factoryClass, importSDO.getHelperContext());
                    importSDO.setUnresolved(false);
                } catch (Exception e) {
                	ContributionResolveException ce = new ContributionResolveException(e);
                	error(context.getMonitor(), "ContributionResolveException", resolver, ce);
                    //throw ce;
                }                
            } else {
            	error(context.getMonitor(), "FailToResolveClass", resolver, factoryName);
                //ContributionResolveException loaderException =
                    //new ContributionResolveException("Fail to resolve class: " + factoryName);
                //throw loaderException;
            }            
        }
    }

    private static void register(Class factoryClass, HelperContext helperContext) throws Exception {
        Field field = factoryClass.getField("INSTANCE");
        Object factory = field.get(null);
        Method method = factory.getClass().getMethod("register", new Class[] {HelperContext.class});
        method.invoke(factory, new Object[] {helperContext});

        //        HelperContext defaultContext = HelperProvider.getDefaultContext();
        //        method.invoke(factory, new Object[] {defaultContext});
    }

    private void importWSDL(ImportSDO importSDO, ModelResolver resolver,ProcessorContext context) throws ContributionResolveException {
        String location = importSDO.getSchemaLocation();
        if (location != null) {
            try {
                Artifact artifact = contributionFactory.createArtifact();
                artifact.setURI(location);
                artifact = resolver.resolveModel(Artifact.class, artifact,context);
                if (artifact.getLocation() != null) {
                	String wsdlURL = artifact.getLocation();
                    URLConnection connection = new URL(wsdlURL).openConnection();
                    connection.setUseCaches(false);
                    InputStream xsdInputStream = connection.getInputStream();
                    try {
                        XSDHelper xsdHelper = importSDO.getHelperContext().getXSDHelper();
                        xsdHelper.define(xsdInputStream, wsdlURL);
                    } finally {
                        xsdInputStream.close();
                    }
                    importSDO.setUnresolved(false);
                } else {
                   	error(context.getMonitor(), "FailToResolveLocation", resolver, location);
                    //ContributionResolveException loaderException = new ContributionResolveException("Fail to resolve location: " + location);
                    //throw loaderException;
                }                
            } catch (IOException e) {
            	ContributionResolveException ce = new ContributionResolveException(e);
            	error(context.getMonitor(), "ContributionResolveException", resolver, ce);
                //throw ce;
            }            
        }
    }

    public QName getArtifactType() {
        return ImportSDO.IMPORT_SDO;
    }

    public void write(ImportSDO model, XMLStreamWriter outputSource,ProcessorContext context) throws ContributionWriteException {
        // Not implemented as <import.sdo> is deprecated
    }

    public Class<ImportSDO> getModelType() {
        return ImportSDO.class;
    }

    public void resolve(ImportSDO importSDO, ModelResolver resolver,ProcessorContext context) throws ContributionResolveException {
        importFactory(importSDO, resolver,context);
        importWSDL(importSDO, resolver,context);
        if (!importSDO.isUnresolved()) {
            resolver.addModel(importSDO,context);
        }
    }

}
