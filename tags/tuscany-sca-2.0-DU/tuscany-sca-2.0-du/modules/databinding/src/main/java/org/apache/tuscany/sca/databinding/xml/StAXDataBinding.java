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

package org.apache.tuscany.sca.databinding.xml;


import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.databinding.BaseDataBinding;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.util.XMLType;

/**
 * A DataBinding for the StAX
 * 
 * @version $Rev: 916888 $ $Date: 2010-02-27 00:44:05 +0000 (Sat, 27 Feb 2010) $
 */
public class StAXDataBinding extends BaseDataBinding {
    public static final String NAME = XMLStreamReader.class.getName();

    public StAXDataBinding() {
        super(NAME, XMLStreamReader.class);
    }

    @Override
    public boolean introspect(DataType type, Operation operation) {
        if (super.introspect(type, operation)) {
            type.setLogical(XMLType.UNKNOWN);
            type.setDataBinding(NAME);
            return true;
        } else {
            return false;
        }
    }

}
