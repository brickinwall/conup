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
package org.apache.tuscany.sca.core.databinding.transformers;

import java.lang.reflect.Array;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.databinding.Mediator;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.interfacedef.DataType;

/**
 * This is a special transformer to transform the output from one IDL to the
 * other one
 *
 * @version $Rev: 916888 $ $Date: 2010-02-27 00:44:05 +0000 (Sat, 27 Feb 2010) $
 */
public class Array2ArrayTransformer extends BaseTransformer<Object, Object> implements PullTransformer<Object, Object> {

    protected Mediator mediator;

    public Array2ArrayTransformer(ExtensionPointRegistry registry) {
        super();
        this.mediator = registry.getExtensionPoint(UtilityExtensionPoint.class).getUtility(Mediator.class);
    }

    @Override
    public String getSourceDataBinding() {
        return "java:array";
    }

    @Override
    public String getTargetDataBinding() {
        return "java:array";
    }

    /**
     * @see org.apache.tuscany.sca.databinding.BaseTransformer#getSourceType()
     */
    @Override
    protected Class<Object> getSourceType() {
        return Object.class;
    }

    /**
     * @see org.apache.tuscany.sca.databinding.BaseTransformer#getTargetType()
     */
    @Override
    protected Class<Object> getTargetType() {
        return Object.class;
    }

    /**
     * @see org.apache.tuscany.sca.databinding.Transformer#getWeight()
     */
    @Override
    public int getWeight() {
        return 10;
    }

    @SuppressWarnings("unchecked")
    public Object transform(Object array, TransformationContext context) {
        try {
            if (array == null) {
                return null;
            }
            DataType<DataType> sourceType = context.getSourceDataType();
            DataType<DataType> targetType = context.getTargetDataType();
            int length = Array.getLength(array);
            Object targetArray = Array.newInstance(targetType.getPhysical().getComponentType(), length);
            for (int i = 0; i < length; i++) {
                Object sourceItem = Array.get(array, i);
                Object targetItem =
                    mediator.mediate(sourceItem, sourceType.getLogical(), targetType.getLogical(), context
                        .getMetadata());
                Array.set(targetArray, i, targetItem);
            }
            return targetArray;
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

}
