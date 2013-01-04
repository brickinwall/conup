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
package org.apache.tuscany.sca.binding.http.wireformat;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.WireFormat;
import org.apache.tuscany.sca.assembly.xml.Constants;

/**
 * Default wire format for http binding
 *
 * @version $Rev: 1200669 $ $Date: 2011-11-11 00:53:16 +0000 (Fri, 11 Nov 2011) $
 */
public class HTTPDefaultWireFormat implements WireFormat {
    public static final QName HTTP_WIRE_FORMAT_DEFAULT_QNAME = new QName(Constants.SCA11_TUSCANY_NS, "wireFormat.default");
    
    public QName getSchemaName() {
        return HTTP_WIRE_FORMAT_DEFAULT_QNAME;
    }

    public boolean isUnresolved() {
        return false;
    }

    public void setUnresolved(boolean unresolved) {
    }
}
