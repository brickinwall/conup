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
package org.apache.tuscany.sca.implementation.java.xml;

import javax.xml.namespace.QName;

/**
 * Constants for the Java Implementation.
 *
 * @version $Rev: 889531 $ $Date: 2009-12-11 08:26:48 +0000 (Fri, 11 Dec 2009) $
 */
public interface JavaImplementationConstants {
    String SCA11_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200912";
    String IMPLEMENTATION_JAVA = "implementation.java";
    QName IMPLEMENTATION_JAVA_QNAME = new QName(SCA11_NS, "implementation.java");
    String CLASS = "class";
    String OPERATION = "operation";
    QName OPERATION_QNAME = new QName(SCA11_NS, OPERATION);
}
