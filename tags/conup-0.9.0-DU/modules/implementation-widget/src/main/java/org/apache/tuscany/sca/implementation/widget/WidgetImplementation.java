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
package org.apache.tuscany.sca.implementation.widget;

import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Implementation;


/**
 * The model representing a widget implementation in an SCA assembly model.
 *
 * @version $Rev: 1350973 $ $Date: 2012-06-16 19:13:53 +0100 (Sat, 16 Jun 2012) $
 */
public interface WidgetImplementation extends Implementation {
    QName TYPE = new QName(SCA11_TUSCANY_NS, "implementation.widget");
    
    /**
     * Returns the location of the HTML file representing the Widget
     * @return the location
     */
    String getLocation();

    /**
     * Set the location of the HTML file representing the Widget
     * @param location
     */
    void setLocation(String location);

    /**
     * Returns the Location URL for the HTML file representing the Widget
     * @return the location
     */
    URL getLocationURL();
    
    /**
     * Set the Location URL for the HTML file representing the Widget 
     * @param url the location
     */
    void setLocationURL(URL url);
    
    /**
     * Get the widget URI
     * @return the uri
     */
    String getWidgetUri();
    
    /**
     * Set the widget URI, used to automatically add a binding to the widget
     * @param uri the uri
     */
    void setWidgetUri(String uri);
}
