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
package org.apache.tuscany.sca.databinding.axiom;

import org.apache.axiom.om.OMElement;
import org.apache.tuscany.sca.databinding.impl.SimpleType2JavaTransformer;

/**
 * Transformer to convert data from a simple java Object to OMElement.
 *
 * @version $Rev: 656164 $ $Date: 2008-05-14 10:04:28 +0100 (Wed, 14 May 2008) $
 */
public class OMElement2Object extends SimpleType2JavaTransformer<OMElement> {

    /**
     * @see org.apache.tuscany.sca.databinding.impl.SimpleType2JavaTransformer#close(java.lang.Object)
     */
    @Override
    protected void close(OMElement source) {
        if (source != null) {
            AxiomHelper.completeAndClose(source);
        }
    }

    @Override
    protected String getText(OMElement source) {
        return source.getText();
    }

    @Override
    public Class getSourceType() {
        return OMElement.class;
    }
}
