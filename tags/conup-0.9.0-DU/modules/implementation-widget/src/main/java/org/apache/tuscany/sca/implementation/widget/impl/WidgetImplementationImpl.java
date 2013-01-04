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
package org.apache.tuscany.sca.implementation.widget.impl;

import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.impl.ImplementationImpl;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.implementation.widget.WidgetImplementation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;


/**
 * The model representing a widget implementation in an SCA assembly model.
 *
 * @version $Rev: 1350973 $ $Date: 2012-06-16 19:13:53 +0100 (Sat, 16 Jun 2012) $
 */
public class WidgetImplementationImpl extends ImplementationImpl implements WidgetImplementation {
    //private Service widgetService;

    private String location;
    private URL locationUrl;
    
    private String widgetUri;

    /**
     * Constructs a new resource implementation.
     */
    WidgetImplementationImpl(ExtensionPointRegistry registry) {
        
        super(TYPE);
        
        FactoryExtensionPoint modelFactories = registry.getExtensionPoint(FactoryExtensionPoint.class);
        AssemblyFactory assemblyFactory = modelFactories.getFactory(AssemblyFactory.class);
        JavaInterfaceFactory javaFactory = modelFactories.getFactory(JavaInterfaceFactory.class);
        
        /*
        // Resource implementation always provide a single service exposing
        // the Resource interface, and have no references and properties
        widgetService = assemblyFactory.createService();
        widgetService.setName("Widget");
        
        // Create the Java interface contract for the Resource service
        JavaInterface javaInterface;
        try {
            javaInterface = javaFactory.createJavaInterface(Widget.class);
        } catch (InvalidInterfaceException e) {
            throw new IllegalArgumentException(e);
        }
        JavaInterfaceContract interfaceContract = javaFactory.createJavaInterfaceContract();
        interfaceContract.setInterface(javaInterface);
        widgetService.setInterfaceContract(interfaceContract);
        
        this.getServices().add(widgetService);
        */
    }

    public QName getType() {
        return TYPE;
    }
    
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public URL getLocationURL() {
        return locationUrl;
    }
    
    public void setLocationURL(URL url) {
        this.locationUrl = url;
    }

    public String getWidgetUri() {
        return this.widgetUri;
    }
    
    public void setWidgetUri(String widgetUri) {
        this.widgetUri = widgetUri;
    }
    
    @Override
    public String toString() {
        return "WidgetImplementationImpl [location=" + location + ", widgetUri=" + widgetUri + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof WidgetImplementationImpl)) {
            return false;
        }
        WidgetImplementationImpl other = (WidgetImplementationImpl)obj;
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        return true;
    }
}
