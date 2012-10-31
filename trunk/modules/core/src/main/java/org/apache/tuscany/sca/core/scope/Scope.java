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
package org.apache.tuscany.sca.core.scope;

/**
 * The default implementation scopes supported by assemblies.
 *
 * @version $Rev: 937310 $ $Date: 2010-04-23 15:27:50 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public class Scope {
    public static final Scope STATELESS = new Scope("STATELESS");
    public static final Scope COMPOSITE = new Scope("COMPOSITE");
    public static final Scope INVALID = new Scope("INVALID");

    private String scope;

    public Scope(String scope) {
        this.scope = scope.toUpperCase().intern();
    }

    public String getScope() {
        return scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Scope scope1 = (Scope) o;
        return !(scope != null ? scope != scope1.scope.intern() : scope1.scope != null);
    }

    @Override
    public int hashCode() {
        return scope != null ? scope.hashCode() : 0;
    }

    @Override
    public String toString() {
        return scope;
    }
}
