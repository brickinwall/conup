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

package org.apache.tuscany.sca.common.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to provide default HTTP Content-Type header
 * based on resource type 
 * 
 * @version $Rev: 942273 $ $Date: 2010-05-08 00:48:23 +0100 (Sat, 08 May 2010) $
 */
public class HTTPContentTypeMapper implements HTTPConstants{
    private static Map<String, String> contentTypeTable = new HashMap<String, String>();

    static {
        contentTypeTable.put(HTM, HTML_CONTENT_TYPE);
        contentTypeTable.put(HTML, HTML_CONTENT_TYPE);
        contentTypeTable.put(JAVASCRIPT, JAVASCRIPT_CONTENT_TYPE);
    }
    
    /**
     * Provided a resource path, identify default content-type based on the resource extension
     * @param resourcePath
     * @return
     */
    public static String getContentType(String resourcePath) {
        return contentTypeTable.get(getResourceType(resourcePath));
    }
    
    /**
     * Utility function to calculate file type based on its extension
     * Useful to map HTTP content-type based on file extension
     * @param resource the resource/file name
     * @return the resource type/extension
     */
    private static String getResourceType(String resource) {
        return resource.substring(resource.lastIndexOf(".") + 1);
    }
}
