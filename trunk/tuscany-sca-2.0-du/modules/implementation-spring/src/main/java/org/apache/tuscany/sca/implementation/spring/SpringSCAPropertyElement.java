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

/**
 * Represents an <sca:property> element in a Spring application-context
 * - this has name and type attributes
 * @version $Rev: 987670 $ $Date: 2010-08-21 00:42:07 +0100 (Sat, 21 Aug 2010) $ 
 */
public class SpringSCAPropertyElement {

    private String name;
    private String type;

    public SpringSCAPropertyElement() {
        super();
    }

    public SpringSCAPropertyElement(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SpringSCAPropertyElement [name=").append(name).append(", type=").append(type).append("]");
        return builder.toString();
    }

} // end class SpringPropertyElement
