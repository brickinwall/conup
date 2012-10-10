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

package org.apache.tuscany.sca.binding.rest;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.common.http.HTTPHeader;
import org.apache.tuscany.sca.common.http.cors.CORS;

/**
 * REST binding model.
 * 
 * @version $Rev: 1294279 $ $Date: 2012-02-27 19:08:32 +0000 (Mon, 27 Feb 2012) $
 */
public interface RESTBinding extends Binding, CORS {
    QName TYPE = new QName(SCA11_TUSCANY_NS, "binding.rest");
    
    public List<HTTPHeader> getHttpHeaders();

    /**
     * Retrieve read timeout configuration for the REST binding
     * @return
     */
    public int getReadTimeout();
    
    /**
     * Configure read timeout for the REST binding
     * @param timeout
     */
    public void setReadTimeout(int timeout);
    
    /**
     * Flag to enable CORS support on the REST binding
     * @return
     */
    public boolean isCORS();
    
    /**
     * Enable/Disable CORS support for the REST binding
     * @param isCors
     * @return
     */
    public void setCORS(boolean isCors);
}
