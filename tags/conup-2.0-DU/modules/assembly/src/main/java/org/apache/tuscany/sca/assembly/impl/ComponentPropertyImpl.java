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

package org.apache.tuscany.sca.assembly.impl;

import javax.xml.xpath.XPathExpression;

import org.apache.tuscany.sca.assembly.ComponentProperty;
import org.apache.tuscany.sca.assembly.Property;

/**
 * Represents a component property.
 * 
 * @version $Rev: 794657 $ $Date: 2009-07-16 13:52:59 +0100 (Thu, 16 Jul 2009) $
 */
public class ComponentPropertyImpl extends PropertyImpl implements ComponentProperty, Cloneable {
    private String file;
    private Property property;
    private String source;
    private XPathExpression sourceXPathExpression;

    /**
     * Constructs a new component property.
     */
    protected ComponentPropertyImpl() {
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // FIXME getValue should not delegate to property.getValue()
    // Doing this violates the setValue/getValue semantics, as you
    // can call setValue() then get a different value from getValue()
    @Override
    public Object getValue() {
        if (super.getValue() == null && property != null) {
            return property.getValue();
        } else {
            return super.getValue();
        }
    }

    public String getFile() {
        return file;
    }

    public Property getProperty() {
        return property;
    }

    public String getSource() {
        return source;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public XPathExpression getSourceXPathExpression() {
        return sourceXPathExpression;
    }

    public void setSourceXPathExpression(XPathExpression sourceXPathExpression) {
        this.sourceXPathExpression = sourceXPathExpression;
    }
    
    public String toString() {
    	return "Property: " + getName() + " Value: " + getValue();
    } // end method toString

}
