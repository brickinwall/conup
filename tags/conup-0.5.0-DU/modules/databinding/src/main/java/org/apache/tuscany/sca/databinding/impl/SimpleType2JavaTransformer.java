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
package org.apache.tuscany.sca.databinding.impl;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.SimpleTypeMapper;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.javabeans.SimpleJavaDataBinding;
import org.apache.tuscany.sca.interfacedef.util.XMLType;

/**
 * Transformer to convert data from a databinding's representation of simple
 * types to Java Objects
 *
 * @version $Rev: 938572 $ $Date: 2010-04-27 18:14:08 +0100 (Tue, 27 Apr 2010) $
 * @tuscany.spi.extension.inheritfrom
 */
public abstract class SimpleType2JavaTransformer<T> extends BaseTransformer<T, Object> implements
    PullTransformer<T, Object> {

    protected SimpleTypeMapper mapper;

    public SimpleType2JavaTransformer() {
        this.mapper = new SimpleTypeMapperImpl();
    }

    public SimpleType2JavaTransformer(SimpleTypeMapper mapper) {
        this.mapper = (mapper != null) ? mapper : new SimpleTypeMapperImpl();
    }

    public Object transform(T source, TransformationContext context) {
        XMLType xmlType = (XMLType)context.getSourceDataType().getLogical();
        QName type = (xmlType != null) ? xmlType.getTypeName() : null;
        if (type == null) {
            xmlType = (XMLType)context.getTargetDataType().getLogical();
            type = (xmlType != null) ? xmlType.getTypeName() : null;
        }
        Object result = mapper.toJavaObject(type, getText(source), context);
        close(source);
        return result;
    }

    @Override
    protected Class<Object> getTargetType() {
        return Object.class;
    }

    @Override
    public int getWeight() {
        // Cannot be used for intermediate
        return 10000;
    }

    /**
     * Get the string value from the source
     * @param source
     * @return A string
     */
    protected abstract String getText(T source);
    
    /**
     * To be overrided by the subclass
     * @param source
     */
    protected void close(T source) {
    }

    @Override
    public String getTargetDataBinding() {
        return SimpleJavaDataBinding.NAME;
    }
}
