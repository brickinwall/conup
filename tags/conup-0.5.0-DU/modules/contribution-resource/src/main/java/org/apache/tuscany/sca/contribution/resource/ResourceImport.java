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

package org.apache.tuscany.sca.contribution.resource;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.contribution.Import;

/**
 * The representation of an resource import.
 *
 * @version $Rev: 993091 $ $Date: 2010-09-06 17:49:51 +0100 (Mon, 06 Sep 2010) $
 */
public interface ResourceImport extends Import {
	QName TYPE = new QName(SCA11_TUSCANY_NS, "import.resource");

    /**
     * Get the location used to resolve the definitions for this import
     *
     * @return The import location
     */
    String getLocation();

    /**
     * Set the location used to resolve the definitions for this import
     *
     * @param location The import location
     */
    void setLocation(String location);

    /**
     * Get URI that identifies the resource import
     *
     * @return The URI
     */
    String getURI();

    /**
     * Set URI that identifies the resource import
     *
     * @param uri The resource URI
     */
    void setURI(String uri);

}