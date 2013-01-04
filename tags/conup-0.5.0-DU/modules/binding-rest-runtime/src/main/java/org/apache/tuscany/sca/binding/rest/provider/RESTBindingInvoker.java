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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.client.HttpClient;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.WireFormat;
import org.apache.tuscany.sca.binding.rest.RESTBinding;
import org.apache.tuscany.sca.binding.rest.wireformat.json.JSONWireFormat;
import org.apache.tuscany.sca.binding.rest.wireformat.xml.XMLWireFormat;
import org.apache.tuscany.sca.common.http.HTTPCacheContext;
import org.apache.tuscany.sca.common.http.HTTPHeader;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaOperation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.wink.client.ApacheHttpClientConfig;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.BasicAuthSecurityHandler;

/**
 * 
 */
public class RESTBindingInvoker implements Invoker {
    private ExtensionPointRegistry registry;
    private EndpointReference endpointReference;
    private RESTBinding binding;
    private Operation operation;
    private RestClient restClient;
    private String httpMethod;
    private Class<?> responseType;

    public RESTBindingInvoker(ExtensionPointRegistry registry, EndpointReference endpointReference, RESTBinding binding, Operation operation, HttpClient httpClient) {
        super();
        this.registry = registry;
        this.endpointReference = endpointReference;
        this.binding = binding;
        this.operation = operation;
        this.restClient = createRestClient(httpClient);
    }

    private static Map<Class<?>, String> mapping = new HashMap<Class<?>, String>();
    static {
        mapping.put(GET.class, HttpMethod.GET);
        mapping.put(POST.class, HttpMethod.POST);
        mapping.put(PUT.class, HttpMethod.PUT);
        mapping.put(DELETE.class, HttpMethod.DELETE);
        mapping.put(HEAD.class, HttpMethod.HEAD);
        mapping.put(OPTIONS.class, HttpMethod.OPTIONS);
    }

    private static <T extends Annotation> T getAnnotation(Annotation[] annotations, Class<T> type) {
        for (Annotation a : annotations) {
            if (a.annotationType() == type) {
                return type.cast(a);
            }
        }
        return null;
    }

    private RestClient createRestClient(HttpClient httpClient) {
        ClientConfig config = new ApacheHttpClientConfig(httpClient);

        // configureBasicAuth(config, userName, password);

        config.applications(new Application() {

            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                Set<Object> providers = new HashSet<Object>();
                providers.add(new DataBindingJAXRSReader(registry));
                providers.add(new DataBindingJAXRSWriter(registry));
                return providers;
            }

        });
        
        config.readTimeout(binding.getReadTimeout());
        RestClient client = new RestClient(config);
        
        // Default to GET for RPC
        httpMethod = HttpMethod.GET;

        for (Map.Entry<Class<?>, String> e : mapping.entrySet()) {
            if (operation.getAttributes().get(e.getKey()) != null) {
                httpMethod = e.getValue();
                break;
            }
        }

        if (operation.getOutputType() != null && !operation.getOutputType().getLogical().isEmpty()) {
            responseType = operation.getOutputType().getLogical().get(0).getPhysical();
        } else {
            responseType = null;
        }
        return client;
    }

    private void configureBasicAuth(ClientConfig config, String userName, String password) {
        BasicAuthSecurityHandler basicAuthSecurityHandler = new BasicAuthSecurityHandler();
        basicAuthSecurityHandler.setUserName(userName);
        basicAuthSecurityHandler.setPassword(password);
        config.handlers(basicAuthSecurityHandler);
    }

    public Message invoke(Message msg) {

        Object entity = null;
        Object[] args = msg.getBody();

        URI uri = URI.create(endpointReference.getDeployedURI());
        UriBuilder uriBuilder = UriBuilder.fromUri(uri);

        Method method = ((JavaOperation)operation).getJavaMethod();

        if (method.isAnnotationPresent(Path.class)) {
            // Only for resource method
            uriBuilder.path(method);
        }

        if (!JAXRSHelper.isResourceMethod(method)) {
            // This is RPC over GET
            uriBuilder.replaceQueryParam("method", method.getName());
        }

        Map<String, Object> pathParams = new HashMap<String, Object>();
        Map<String, Object> matrixParams = new HashMap<String, Object>();
        Map<String, Object> queryParams = new HashMap<String, Object>();
        Map<String, Object> headerParams = new HashMap<String, Object>();
        Map<String, Object> formParams = new HashMap<String, Object>();
        Map<String, Object> cookieParams = new HashMap<String, Object>();

        for (int i = 0; i < method.getParameterTypes().length; i++) {
            boolean isEntity = true;
            Annotation[] annotations = method.getParameterAnnotations()[i];
            PathParam pathParam = getAnnotation(annotations, PathParam.class);
            if (pathParam != null) {
                isEntity = false;
                pathParams.put(pathParam.value(), args[i]);
            }
            MatrixParam matrixParam = getAnnotation(annotations, MatrixParam.class);
            if (matrixParam != null) {
                isEntity = false;
                matrixParams.put(matrixParam.value(), args[i]);
            }
            QueryParam queryParam = getAnnotation(annotations, QueryParam.class);
            if (queryParam != null) {
                isEntity = false;
                queryParams.put(queryParam.value(), args[i]);
            }
            HeaderParam headerParam = getAnnotation(annotations, HeaderParam.class);
            if (headerParam != null) {
                isEntity = false;
                headerParams.put(headerParam.value(), args[i]);
            }
            FormParam formParam = getAnnotation(annotations, FormParam.class);
            if (formParam != null) {
                isEntity = false;
                formParams.put(formParam.value(), args[i]);
            }
            CookieParam cookieParam = getAnnotation(annotations, CookieParam.class);
            if (cookieParam != null) {
                isEntity = false;
                cookieParams.put(cookieParam.value(), args[i]);
            }
            isEntity = (getAnnotation(annotations, Context.class) == null);
            if (isEntity) {
                entity = args[i];
            }
        }

        for (Map.Entry<String, Object> p : queryParams.entrySet()) {
            uriBuilder.replaceQueryParam(p.getKey(), p.getValue());
        }
        for (Map.Entry<String, Object> p : matrixParams.entrySet()) {
            uriBuilder.replaceMatrixParam(p.getKey(), p.getValue());
        }

        uri = uriBuilder.buildFromMap(pathParams);
        Resource resource = restClient.resource(uri);

        for (Map.Entry<String, Object> p : headerParams.entrySet()) {
            resource.header(p.getKey(), String.valueOf(p.getValue()));
        }

        for (Map.Entry<String, Object> p : cookieParams.entrySet()) {
            Cookie cookie = new Cookie(p.getKey(), String.valueOf(p.getValue()));
            resource.cookie(cookie);
        }

        resource.contentType(getContentType());
        resource.accept(getAccepts());

        //handles declarative headers configured on the composite
        for (HTTPHeader header : binding.getHttpHeaders()) {
            //treat special headers that need to be calculated
            if (header.getName().equalsIgnoreCase("Expires")) {
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTime(new Date());

                calendar.add(Calendar.HOUR, Integer.parseInt(header.getValue()));

                resource.header("Expires", HTTPCacheContext.RFC822DateFormat.format(calendar.getTime()));
            } else {
                //default behaviour to pass the header value to HTTP response
                resource.header(header.getName(), header.getValue());
            }
        }

        Object result = resource.invoke(httpMethod, responseType, entity);
        msg.setBody(result);
        return msg;
    }

    private String getContentType() {
        String contentType = MediaType.APPLICATION_OCTET_STREAM;
        Consumes consumes = ((JavaOperation)operation).getJavaMethod().getAnnotation(Consumes.class);
        if (consumes != null && consumes.value().length > 0) {
            contentType = consumes.value()[0];
        }
        WireFormat wf = binding.getRequestWireFormat();
        if (wf != null) {
            if (XMLWireFormat.REST_WIREFORMAT_XML_QNAME.equals(wf.getSchemaName())) {
                contentType = MediaType.APPLICATION_XML;
            } else if (JSONWireFormat.REST_WIREFORMAT_JSON_QNAME.equals(wf.getSchemaName())) {
                contentType = MediaType.APPLICATION_JSON;
            }
        }
        return contentType;
    }

    private String[] getAccepts() {
        String accepts[] = {MediaType.APPLICATION_OCTET_STREAM};
        Produces produces = ((JavaOperation)operation).getJavaMethod().getAnnotation(Produces.class);
        if (produces != null) {
            accepts = produces.value();
        }
        WireFormat wf = binding.getResponseWireFormat();
        if (wf != null) {
            if (XMLWireFormat.REST_WIREFORMAT_XML_QNAME.equals(wf.getSchemaName())) {
                accepts = new String[] {MediaType.APPLICATION_XML};
            } else if (JSONWireFormat.REST_WIREFORMAT_JSON_QNAME.equals(wf.getSchemaName())) {
                accepts = new String[] {MediaType.APPLICATION_JSON};
            }
        }
        return accepts;
    }
}
