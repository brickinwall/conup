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
package org.apache.tuscany.sca.implementation.widget.provider;

import java.net.URI;

import javax.servlet.Servlet;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Base;
import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.host.http.ServletHost;
import org.apache.tuscany.sca.implementation.widget.WidgetImplementation;
import org.apache.tuscany.sca.implementation.widget.javascript.WidgetImplementationJavascriptProvider;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.web.javascript.ComponentJavaScriptGenerator;


/**
 * The model representing a resource implementation in an SCA assembly model.
 *
 * @version $Rev: 951242 $ $Date: 2010-06-04 03:38:54 +0100 (Fri, 04 Jun 2010) $
 */
class WidgetImplementationProvider implements ImplementationProvider {
    private static final QName BINDING_HTTP = new QName(Base.SCA11_TUSCANY_NS, "binding.http");
    
    private RuntimeComponent component;
    
    private WidgetImplementationJavascriptProvider javascriptProvider;
    private ComponentJavaScriptGenerator javaScriptGenerator;
    private ServletHost servletHost;
    
    private String widgetLocationURL;
    private String widgetFolderURL;
    private String widgetName;
    
    private String scriptURI;

    /**
     * Constructs a new resource implementation provider.
     */
    WidgetImplementationProvider(RuntimeComponent component, 
                                 WidgetImplementation implementation,
                                 WidgetImplementationJavascriptProvider javascriptProvider,
                                 ComponentJavaScriptGenerator javaScriptGenerator, 
                                 ServletHost servletHost) {
        
        this.component = component;
        
        this.javaScriptGenerator = javaScriptGenerator;
        this.servletHost = servletHost;
        
        widgetLocationURL = implementation.getLocationURL().toString();
        int s = widgetLocationURL.lastIndexOf('/');
        widgetFolderURL = widgetLocationURL.substring(0, s);
        widgetName = widgetLocationURL.substring(s +1);
        widgetName = widgetName.substring(0, widgetName.lastIndexOf('.'));
    }

    public Invoker createInvoker(RuntimeComponentService service, Operation operation) {
        WidgetImplementationInvoker invoker = new WidgetImplementationInvoker(component, javaScriptGenerator, widgetName, widgetFolderURL, widgetLocationURL);
        return invoker;
    }
    
    public boolean supportsOneWayInvocation() {
        return false;
    }

    public void start() {
        String baseURI = getBaseURI();

        // this uses removeServletMapping / addServletMapping as there is no getServletMapping facility
        scriptURI = URI.create(baseURI + "/" + this.widgetName + ".js").toString();
        Servlet servlet = servletHost.getServletMapping(scriptURI);
        if (servlet == null /*|| servlet instanceof HTTPGetListenerServlet*/) {
            WidgetComponentScriptServlet widgetScriptServlet;
            widgetScriptServlet = new WidgetComponentScriptServlet(this.component, javaScriptGenerator);
            servletHost.addServletMapping(scriptURI, widgetScriptServlet);
        }     

        // If added to the class path, start dojo provider
        if(javascriptProvider != null) {
            javascriptProvider.start();
        }
        
    }

    public void stop() {
        // Unregister the component client script Servlet
        WidgetComponentScriptServlet widgetScriptServlet = (WidgetComponentScriptServlet) servletHost.getServletMapping(scriptURI);
        if (widgetScriptServlet != null) {
            // Remove the Servlet mapping
            servletHost.removeServletMapping(scriptURI);
        }
        
        if(javascriptProvider != null) {
            javascriptProvider.stop();
        }
    }
    

    /**
     * Get the contextRoot considering the HTTP Binding URI when in a embedded environment
     * @return
     */
    private String getBaseURI() {
        String baseURI = null;
        String contextPath = "/";
        if (servletHost != null) {
            contextPath = servletHost.getContextPath();
        }
        if (!contextPath.endsWith("/")) {
            contextPath = contextPath + "/";
        }

        ComponentService service = component.getService("Widget");
        if (service != null) {
            for (Binding binding : service.getBindings()) {
                if (binding.getType().equals(BINDING_HTTP)) {
                    String bindingURI = binding.getURI();
                    URI uri = URI.create(bindingURI);
                    if (uri.isAbsolute()) {
                        return bindingURI;
                    }
                    if (bindingURI.startsWith("/")) {
                        bindingURI = bindingURI.substring(1);
                    }
                    baseURI = contextPath + bindingURI;
                }
            }
        }

        return baseURI == null ? "" : baseURI;
    }

}
