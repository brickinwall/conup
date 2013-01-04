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

package org.apache.tuscany.sca.binding.rmi.impl;

import org.apache.tuscany.sca.binding.rmi.RMIBinding;
import org.apache.tuscany.sca.binding.rmi.RMIBindingFactory;

/**
 * Factory implementation to create RMI Models
 * 
 * @version $Rev: 748977 $ $Date: 2009-03-01 08:44:32 +0000 (Sun, 01 Mar 2009) $
 */
public class RMIBindingFactoryImpl implements RMIBindingFactory {
    public RMIBinding createRMIBinding() {
        return new RMIBindingImpl();
    }

}
