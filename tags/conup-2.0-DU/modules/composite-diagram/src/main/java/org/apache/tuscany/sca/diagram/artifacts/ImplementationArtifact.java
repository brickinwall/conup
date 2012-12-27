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

package org.apache.tuscany.sca.diagram.artifacts;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ImplementationArtifact extends Artifact {

    /**
     * Create an element with specified height and width
     */
    public Element addElement(Document document, String svgNs, int x, int y, int height, int width) {

        this.setHeight(height);
        this.setWidth(width);
        this.setxCoordinate(x);
        this.setyCoordinate(y);

        Element rectangle = document.createElementNS(svgNs, "rect");
        rectangle.setAttributeNS(null, "x", x + "");
        rectangle.setAttributeNS(null, "y", y + "");
//        rectangle.setAttributeNS(null, "rx", getRoundCorner());
//        rectangle.setAttributeNS(null, "ry", getRoundCorner());
        rectangle.setAttributeNS(null, "width", width + "");
        rectangle.setAttributeNS(null, "height", height + "");
        rectangle.setAttributeNS(null, "fill", "purple");
        rectangle.setAttributeNS(null, "stroke", "black");
        rectangle.setAttributeNS(null, "stroke-width", "1");
        rectangle.setAttributeNS(null, "fill-opacity", "0.1");
        
        rectangle.setAttributeNS(null, "class", "implementation");
        return rectangle;
    }

    /**
     * Create an element with default height and width
     */
    public Element addElement(Document document, String svgNs, int x, int y) {

        return addElement(document, svgNs, x, y, Constant.COMPONENT_DEFAULT_HEIGHT/2, Constant.COMPONENT_DEFAULT_WIDTH/2);

    }

}
