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

package org.apache.tuscany.sca.node.configuration;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * Configuration for bindings used by an SCA node
 */
public interface BindingConfiguration {
    /**
     * Get the QName of the binding type
     * @return the QName of the binding type
     */
    QName getBindingType();

    /**
     * Set the type of the binding
     * @param type The QName of the binding type
     */
    BindingConfiguration setBindingType(QName type);

    /**
     * Get a list of base URIs for the binding. For each protocol supported by the binding,
     * one base URI can be configured
     * @return A list of base URIs
     */
    List<String> getBaseURIs();

    /**
     * Add a base URI
     * @param baseURI
     * @return
     */
    BindingConfiguration addBaseURI(String baseURI);
}
