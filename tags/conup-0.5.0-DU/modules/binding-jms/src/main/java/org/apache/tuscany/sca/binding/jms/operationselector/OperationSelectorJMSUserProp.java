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
package org.apache.tuscany.sca.binding.jms.operationselector;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.OperationSelector;
import org.apache.tuscany.sca.assembly.xml.Constants;

/**
 * Model of user property based operation selection
 * 
 * <operationSelector.jmsUser propertName="MyHeaderProperty"/>
 *
 * @version $Rev: 813442 $ $Date: 2009-09-10 14:56:17 +0100 (Thu, 10 Sep 2009) $
 */
public class OperationSelectorJMSUserProp implements OperationSelector {
    public static final QName OPERATION_SELECTOR_JMS_USERPROP_QNAME = new QName(Constants.SCA11_TUSCANY_NS, "operationSelector.jmsUserProp");
    public static final String OPERATION_SELECTOR_JMS_USERPROP_ATTR  = "propertyName";
    
    private String propertyName;
    
    public QName getSchemaName() {
        return OPERATION_SELECTOR_JMS_USERPROP_QNAME;
    }

    public boolean isUnresolved() {
        return false;
    }

    public void setUnresolved(boolean unresolved) {
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
