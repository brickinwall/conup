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

package org.apache.tuscany.sca.binding.rest.provider;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.binding.rest.RESTBinding;
import org.apache.tuscany.sca.common.http.HTTPCacheContext;
import org.apache.tuscany.sca.common.http.HTTPContext;
import org.apache.tuscany.sca.common.http.HTTPHeader;
import org.apache.tuscany.sca.common.http.cors.CORSHeaderProcessor;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.MessageFactory;

/**
 * Servlet responsible for dispatching HTTP service requests to the
 * target component implementation.
 *
 * @version $Rev: 1341952 $ $Date: 2012-05-23 18:12:46 +0100 (Wed, 23 May 2012) $
 */
public class RESTServiceListenerServlet extends HttpServlet implements Servlet {
    
    private static final long serialVersionUID = -5543706958107836637L;
    
    transient private RESTBinding binding;
    transient private ServletConfig config;
    transient private MessageFactory messageFactory;
    transient private Invoker serviceInvoker;
    
    /**
     * Constructs a new HTTPServiceListenerServlet.
     */
    public RESTServiceListenerServlet(Binding binding, Invoker serviceInvoker, MessageFactory messageFactory) {
        this.binding = (RESTBinding) binding;
        this.serviceInvoker = serviceInvoker;
        this.messageFactory = messageFactory;
    }

    public ServletConfig getServletConfig() {
        return config;
    }

    public String getServletInfo() {
        return "";
    }

    public void init(ServletConfig config) throws ServletException {
        this.config = config;
    }

    public void destroy() {
        
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (binding.isCORS()) {
            CORSHeaderProcessor.processCORS(binding.getCORSConfiguration(), request, response);
        }
        HTTPContext bindingContext = new HTTPContext();
        bindingContext.setHttpRequest(request);
        bindingContext.setHttpResponse(response);
        
        // Dispatch the service interaction to the service invoker
        Message requestMessage = messageFactory.createMessage();
        requestMessage.setBindingContext(bindingContext);
        requestMessage.setBody(new Object[]{request, response});
        Message responseMessage = serviceInvoker.invoke(requestMessage);
        if (responseMessage.isFault()) {            
            // Turn a fault into an exception
            //throw new ServletException((Throwable)responseMessage.getBody());
            Throwable e = (Throwable)responseMessage.getBody();
            ((HttpServletResponse)response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
        } else {
            //handles declarative headers configured on the composite
            for(HTTPHeader header : binding.getHttpHeaders()) {
                //treat special headers that need to be calculated
                if(header.getName().equalsIgnoreCase("Expires")) {
                    GregorianCalendar calendar = new GregorianCalendar();
                    calendar.setTime(new Date());

                    calendar.add(Calendar.HOUR, Integer.parseInt(header.getValue()));

                    response.setHeader("Expires", HTTPCacheContext.RFC822DateFormat.format( calendar.getTime() ));
                } else {
                    //default behaviour to pass the header value to HTTP response
                    response.setHeader(header.getName(), header.getValue());
                }

            }
        }
    }
}
