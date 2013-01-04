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

package org.apache.tuscany.sca.binding.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.OperationSelector;
import org.apache.tuscany.sca.assembly.WireFormat;
import org.apache.tuscany.sca.binding.rest.RESTBinding;
import org.apache.tuscany.sca.common.http.HTTPHeader;
import org.apache.tuscany.sca.common.http.cors.CORSConfiguration;


/**
 * Implementation of the HTTP binding model.
 *
 * @version $Rev: 1294279 $ $Date: 2012-02-27 19:08:32 +0000 (Mon, 27 Feb 2012) $
 */
class RESTBindingImpl implements RESTBinding {

    private String name;
    private String uri;

    private int readTimeout = 60000;
    
    private boolean isCORS = false;
    private CORSConfiguration corsConfig;
    
    private List<HTTPHeader> httpHeaders = new ArrayList<HTTPHeader>();

    private WireFormat requestWireFormat;
    private WireFormat responseWireFormat;
    private OperationSelector operationSelector;

    public QName getType() {
        return TYPE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public boolean isCORS() {
        return isCORS;
    }

    public void setCORS(boolean isCORS) {
        this.isCORS = isCORS;
    }
    
    public CORSConfiguration getCORSConfiguration() {
        return corsConfig;
    }

    public void setCORSConfiguration(CORSConfiguration corsConfig) {
        this.corsConfig = corsConfig;
    }

    public List<HTTPHeader> getHttpHeaders() {
        return this.httpHeaders;
    }

    public boolean isUnresolved() {
        return false;
    }

    public void setUnresolved(boolean unresolved) {
        // The sample binding is always resolved
    }

    // Wireformat and Operation selection

    public WireFormat getRequestWireFormat() {
        return requestWireFormat;
    }

    public void setRequestWireFormat(WireFormat wireFormat) {
        this.requestWireFormat = wireFormat;
    }

    public WireFormat getResponseWireFormat() {
        return responseWireFormat;
    }

    public void setResponseWireFormat(WireFormat wireFormat) {
        this.responseWireFormat = wireFormat;
    }

    public OperationSelector getOperationSelector() {
        return operationSelector;
    }

    public void setOperationSelector(OperationSelector operationSelector) {
        this.operationSelector = operationSelector;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
