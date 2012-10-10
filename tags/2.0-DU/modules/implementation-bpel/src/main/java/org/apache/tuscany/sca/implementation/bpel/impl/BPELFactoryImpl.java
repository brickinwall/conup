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

package org.apache.tuscany.sca.implementation.bpel.impl;

import org.apache.tuscany.sca.implementation.bpel.BPELFactory;
import org.apache.tuscany.sca.implementation.bpel.BPELImplementation;
import org.apache.tuscany.sca.implementation.bpel.BPELProcessDefinition;

/**
 * A factory for the BPEL implementation model.
 * 
 * @version $Rev: 826907 $ $Date: 2009-10-20 01:17:14 +0100 (Tue, 20 Oct 2009) $
 */
public class BPELFactoryImpl implements BPELFactory {
    
    public BPELFactoryImpl() {
    }

    public BPELImplementation createBPELImplementation() {
        return new BPELImplementationImpl();
    }

    public BPELProcessDefinition createBPELProcessDefinition() {
        return new BPELProcessDefinitionImpl();
    }

}
