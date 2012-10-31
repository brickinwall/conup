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

package org.apache.tuscany.sca.common.xml.stax.reader;

import java.util.Map;

import javax.xml.namespace.QName;

/**
 * A named property
 * 
 * @version $Rev: 801844 $ $Date: 2009-08-07 00:50:19 +0100 (Fri, 07 Aug 2009) $
 */
public class NamedProperty implements Map.Entry<QName, Object> {
    private QName key;

    private Object value;

    public NamedProperty(QName key, Object value) {
        this.key = key;
        this.value = value;
    }

    public NamedProperty(String key, Object value) {
        this.key = new QName(key);
        this.value = value;
    }
    
    public QName getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public Object setValue(Object value) {
        Object v = this.value;
        this.value = value;
        return v;
    }
}
