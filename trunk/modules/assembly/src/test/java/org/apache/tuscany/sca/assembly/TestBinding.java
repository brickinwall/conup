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
package org.apache.tuscany.sca.assembly;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.impl.BindingImpl;

/**
 * A test interface model.
 *
 * @version $Rev: 886122 $ $Date: 2009-12-02 11:49:16 +0000 (Wed, 02 Dec 2009) $
 */
public class TestBinding extends BindingImpl implements Binding {
    private final static QName TYPE = new QName("http://test", "binding.test");

    public TestBinding(AssemblyFactory factory) {
        super(TYPE);
    }

    public String getName() {
        return null;
    }

    public String getURI() {
        return "http://test";
    }

    public boolean isUnresolved() {
        return false;
    }
    
    public WireFormat getRequestWireFormat() {
        return null;
    }
    
    public void setRequestWireFormat(WireFormat wireFormat) {  
    }
    
    public WireFormat getResponseWireFormat() {
        return null;
    }
    
    public void setResponseWireFormat(WireFormat wireFormat) {
    }
    
    public OperationSelector getOperationSelector() {
        return null;
    }
    
    public void setOperationSelector(OperationSelector operationSelector) {
    }    

}
