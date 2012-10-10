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

package org.apache.tuscany.sca.databinding.json;

import java.util.Collection;

import org.apache.tuscany.sca.databinding.json.jackson.JacksonHelper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.json.JSONArray;

/**
 * @version $Rev: 1180780 $ $Date: 2011-10-10 06:01:04 +0100 (Mon, 10 Oct 2011) $
 */
public class JSONHelper {
    private JSONHelper() {

    }

    /**
     * Convert to Jettison JSONObject
     * @param source
     * @return
     */
    public static JSONObject toJettison(Object source) {
        JSONObject json = null;
        if (source instanceof JSONObject) {
            json = (JSONObject)source;
        } else if (source instanceof org.json.JSONObject || source instanceof String) {
            json = stringToJettision(source.toString());
        } else if (source instanceof JsonNode) {
            json = stringToJettision(JacksonHelper.toString((JsonNode)source));
        } else if (source instanceof JsonParser) {
            json = stringToJettision(JacksonHelper.toString((JsonParser)source));
        }
        return json;
    }

    private static JSONObject stringToJettision(String content) {
        try {
            return new JSONObject(content);
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Convert to org.json.JSONObject
     * @param source
     * @return
     */
    public static org.json.JSONObject toJSONOrg(Object source) {
        org.json.JSONObject json = null;
        if (source instanceof JSONObject) {
            try {
                json = new org.json.JSONObject(((JSONObject)source).toString());
            } catch (org.json.JSONException e) {
                throw new IllegalArgumentException(e);
            }
        } else if (source instanceof org.json.JSONObject) {
            json = (org.json.JSONObject)source;
        }
        return json;
    }

    public static Object toJSON(String json, Class<?> type) {
        if (type == JSONObject.class) {
            try {
                return new JSONObject(json);
            } catch (JSONException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            if (type == null) {
                type = org.json.JSONObject.class;
            }
            try {
                if (type == JSONArray.class || type.isArray() || Collection.class.isAssignableFrom(type)) {
                    return new JSONArray(json);
                }
                return JacksonHelper.MAPPER.readValue(json, org.json.JSONObject.class);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
