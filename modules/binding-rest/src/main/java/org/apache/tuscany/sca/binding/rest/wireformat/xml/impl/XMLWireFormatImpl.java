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

package org.apache.tuscany.sca.binding.rest.wireformat.xml.impl;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.binding.rest.wireformat.xml.XMLWireFormat;

/**
 * XML Wireformat implementation for REST Binding
 *
 * @version $Rev: 990515 $ $Date: 2010-08-29 09:53:46 +0100 (Sun, 29 Aug 2010) $
 */
public class XMLWireFormatImpl implements XMLWireFormat {

    public QName getSchemaName() {
        return XMLWireFormat.REST_WIREFORMAT_XML_QNAME;
    }

    public boolean isUnresolved() {
        return false;
    }

    public void setUnresolved(boolean unresolved) {
        // no op
    }

    public String toString() {
        return "application/xml";
    }
}
