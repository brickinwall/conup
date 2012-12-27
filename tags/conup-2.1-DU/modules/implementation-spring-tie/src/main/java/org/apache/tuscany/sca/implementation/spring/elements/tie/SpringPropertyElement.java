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
package org.apache.tuscany.sca.implementation.spring.elements.tie;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a <property> element in a Spring application-context
 * - this has name and ref attributes
 * 
 * @version $Rev: 987670 $ $Date: 2010-08-21 00:42:07 +0100 (Sat, 21 Aug 2010) $
 */
public class SpringPropertyElement {

    private String name;
    private List<String> refs = new ArrayList<String>();
    private List<String> values = new ArrayList<String>();

    public SpringPropertyElement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getRefs() {
        return this.refs;
    }

    public void addRef(String ref) {
        this.refs.add(ref);
    }

    public List<String> getValues() {
        return this.values;
    }

    public void addValue(String value) {
        this.values.add(value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SpringPropertyElement [name=").append(name).append(", refs=").append(refs).append(", values=")
            .append(values).append("]");
        return builder.toString();
    }

} // end class SpringPropertyElement
