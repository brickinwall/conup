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

package org.apache.tuscany.sca.interfacedef.util;

import javax.xml.namespace.QName;

/**
 * The metadata for an XML element or type.
 *
 * @version $Rev: 937310 $ $Date: 2010-04-23 15:27:50 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public class XMLType {
    public static final XMLType UNKNOWN = new XMLType(null, null);
    protected QName element;
    protected QName type;
    protected boolean nillable = true;
    protected boolean many = false;

    /**
     * @param element
     */
    public XMLType(ElementInfo element) {
        super();
        this.element = element.getQName();
        if (element.getType() != null) {
            this.type = element.getType().getQName();
        }
    }

    /**
     * @param element
     */
    public XMLType(TypeInfo type) {
        this.element = null;
        this.type = type.getQName();
    }

    public XMLType(QName element, QName type) {
        this.element = element;
        this.type = type;
    }

    /**
     * @return the type
     */
    public QName getTypeName() {
        return type;
    }

    public boolean isElement() {
        return element != null;
    }

    public QName getElementName() {
        return element;
    }

    public void setElementName(QName element) {
        this.element = element;
    }

    public void setTypeName(QName type) {
        this.type = type;
    }

    public static XMLType getType(QName type) {
        return new XMLType(null, type);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((element == null) ? 0 : element.hashCode());
        result = PRIME * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final XMLType other = (XMLType)obj;
        if (element == null) {
            if (other.element != null) {
                return false;
            }
        } else if (!element.equals(other.element)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Element: " + element + " Type: " + type;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean niable) {
        this.nillable = niable;
    }

    public boolean isMany() {
        return many;
    }

    public void setMany(boolean many) {
        this.many = many;
    }

}
