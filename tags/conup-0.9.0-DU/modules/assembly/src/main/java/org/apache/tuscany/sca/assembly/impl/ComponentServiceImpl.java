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

package org.apache.tuscany.sca.assembly.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.CompositeService;
import org.apache.tuscany.sca.assembly.Service;

/**
 * Represents a component service
 * 
 * @version $Rev: 791550 $ $Date: 2009-07-06 18:39:44 +0100 (Mon, 06 Jul 2009) $
 */
public class ComponentServiceImpl extends ServiceImpl implements ComponentService, Cloneable {
    private Service service;
    private List<CompositeService> promotedAs = new ArrayList<CompositeService>();
    private ComponentReference callbackReference;

    /**
     * Constructs a new component service.
     */
    protected ComponentServiceImpl() {
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public List<CompositeService> promotedAs() {
        return promotedAs;
    }

    public ComponentReference getCallbackReference() {
        return callbackReference;
    }

    public void setCallbackReference(ComponentReference callbackReference) {
        this.callbackReference = callbackReference;
    }

}
