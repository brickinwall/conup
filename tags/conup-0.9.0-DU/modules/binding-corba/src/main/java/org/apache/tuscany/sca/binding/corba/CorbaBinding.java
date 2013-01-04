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

package org.apache.tuscany.sca.binding.corba;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Base;
import org.apache.tuscany.sca.assembly.Binding;

/**
 * @version $Rev: 924158 $ $Date: 2010-03-17 05:21:11 +0000 (Wed, 17 Mar 2010) $
 */
public interface CorbaBinding extends Binding {
    String BINDING_CORBA = "binding.corba";
    QName BINDING_CORBA_QNAME = new QName(Base.SCA11_TUSCANY_NS, BINDING_CORBA);

    String getHost();

    void setHost(String host);

    int getPort();

    void setPort(int port);
    
    String getId();
    
    void setId(String id);
    
    String getCorbaname();
}
