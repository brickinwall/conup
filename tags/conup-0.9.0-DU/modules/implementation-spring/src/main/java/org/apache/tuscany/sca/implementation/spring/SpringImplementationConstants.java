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

package org.apache.tuscany.sca.implementation.spring;

import javax.xml.namespace.QName;

/**
 * Constants used in Spring Application Context XML files.
 */
public interface SpringImplementationConstants {

    String SCA_NS = "http://www.springframework.org/schema/sca";
    String SPRING_NS = "http://www.springframework.org/schema/beans";

    String PROPERTY = "property";
    QName SCA_PROPERTY_ELEMENT = new QName(SCA_NS, PROPERTY);
    QName PROPERTY_ELEMENT = new QName(SPRING_NS, PROPERTY);

    String SCASERVICE = "service";
    QName SCA_SERVICE_ELEMENT = new QName(SCA_NS, SCASERVICE);

    String SCAREFERENCE = "reference";
    QName SCA_REFERENCE_ELEMENT = new QName(SCA_NS, SCAREFERENCE);

    String BEANS = "beans";
    QName BEANS_ELEMENT = new QName(SPRING_NS, BEANS);

    String IMPORT = "import";
    QName IMPORT_ELEMENT = new QName(SPRING_NS, IMPORT);

    String BEAN = "bean";
    QName BEAN_ELEMENT = new QName(SPRING_NS, BEAN);

    String CONSTRUCTORARG = "constructor-arg";
    QName CONSTRUCTORARG_ELEMENT = new QName(SPRING_NS, CONSTRUCTORARG);

    String LIST = "list";
    QName LIST_ELEMENT = new QName(SPRING_NS, LIST);

    String SET = "set";
    QName SET_ELEMENT = new QName(SPRING_NS, SET);

    String MAP = "map";
    QName MAP_ELEMENT = new QName(SPRING_NS, MAP);

    String VALUE = "value";
    QName VALUE_ELEMENT = new QName(SPRING_NS, VALUE);

    String REF = "ref";
    QName REF_ELEMENT = new QName(SPRING_NS, REF);

    String ENTRY = "entry";
    QName ENTRY_ELEMENT = new QName(SPRING_NS, ENTRY);

    String APPLICATION_CONTEXT = "application-context.xml";
}
