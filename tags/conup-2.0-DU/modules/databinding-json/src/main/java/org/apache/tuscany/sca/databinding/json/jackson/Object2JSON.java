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

package org.apache.tuscany.sca.databinding.json.jackson;

import java.math.BigDecimal;

import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.javabeans.JavaBeansDataBinding;
import org.apache.tuscany.sca.databinding.json.JSONDataBinding;
import org.apache.tuscany.sca.databinding.json.JSONHelper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @version $Rev: 1181240 $ $Date: 2011-10-10 22:15:41 +0100 (Mon, 10 Oct 2011) $
 */
public class Object2JSON implements PullTransformer<Object, Object> {

    public Object2JSON() {
        super();
    }

    public Object transform(Object source, TransformationContext context) {
        if (source == null) {
            return null;
        }

        Class<?> targetType = null;
        if (context != null && context.getTargetDataType() != null) {
            targetType = context.getTargetDataType().getPhysical();
        }
        if (targetType == null) {
            targetType = String.class;
        }
        try {
            if (targetType != null && targetType.isPrimitive()) {
                return source;
            }
            ObjectMapper mapper = JacksonHelper.createObjectMapper(targetType);
            String value = mapper.writeValueAsString(source);
            if (JsonNode.class.isAssignableFrom(targetType)) {
                return JacksonHelper.createJsonParser(value).readValueAsTree();
            } else if (targetType == String.class || targetType == Object.class || targetType.isPrimitive()) {
                return value;
            } else if (targetType == BigDecimal.class) {
                return value.toString();
            } else if (JsonParser.class.isAssignableFrom(targetType)) {
                return JacksonHelper.createJsonParser(value);
            } else {
                return JSONHelper.toJSON(value, targetType);
            }
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    public String getSourceDataBinding() {
        return JavaBeansDataBinding.NAME;
    }

    public String getTargetDataBinding() {
        return JSONDataBinding.NAME;
    }

    public int getWeight() {
        return 5000;
    }
}
