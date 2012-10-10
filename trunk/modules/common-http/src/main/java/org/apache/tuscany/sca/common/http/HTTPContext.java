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

package org.apache.tuscany.sca.common.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP Context used as binding context in HTTP related bindings
 * 
 * @version $Rev: 942273 $ $Date: 2010-05-08 00:48:23 +0100 (Sat, 08 May 2010) $
 */
public class HTTPContext {
    private HttpServletRequest request;
    private HttpServletResponse response;

    public HttpServletRequest getHttpRequest() {
        return request;
    }
    public void setHttpRequest(HttpServletRequest request) {
        this.request = request;
    }
    public HttpServletResponse getHttpResponse() {
        return response;
    }
    public void setHttpResponse(HttpServletResponse response) {
        this.response = response;
    }
}
