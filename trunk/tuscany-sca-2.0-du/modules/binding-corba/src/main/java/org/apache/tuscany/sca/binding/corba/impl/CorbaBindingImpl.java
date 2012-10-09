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

package org.apache.tuscany.sca.binding.corba.impl;

import org.apache.tuscany.sca.assembly.impl.BindingImpl;
import org.apache.tuscany.sca.binding.corba.CorbaBinding;
import org.apache.tuscany.sca.host.corba.CorbaHostUtils;

/**
 * @version $Rev: 924158 $ $Date: 2010-03-17 05:21:11 +0000 (Wed, 17 Mar 2010) $
 */
public class CorbaBindingImpl extends BindingImpl implements CorbaBinding {
    private String host;
    private int port;
    private String id;
    
    /**
     * @param type
     */
    public CorbaBindingImpl() {
        super(CorbaBinding.BINDING_CORBA_QNAME);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCorbaname() {
        return CorbaHostUtils.isValidCorbanameURI(getURI()) ? getURI(): CorbaHostUtils.createCorbanameURI(getHost(), getPort(), getName());
    }

}
