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

package org.apache.tuscany.sca.http.jetty;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.resource.Resource;
import org.mortbay.resource.URLResource;

/**
 * Customizes the Jetty default Servlet.
 *
 * @version $Rev: 639608 $ $Date: 2008-03-21 12:52:19 +0000 (Fri, 21 Mar 2008) $
 */
public class JettyDefaultServlet extends DefaultServlet {
    private static final long serialVersionUID = 7000218247190209353L;

    private String documentRoot;
    private String servletPath;
    
    public JettyDefaultServlet(String servletPath, String documentRoot) {
        this.servletPath = servletPath + '/';
        this.documentRoot = documentRoot;
    }

    @Override
    public Resource getResource(String pathInContext) {
        if (pathInContext.startsWith(servletPath)) {
            if (pathInContext.length() > servletPath.length()) {
                pathInContext = pathInContext.substring(servletPath.length());
            } else {
                pathInContext = "";
            }
        } else {
            if ((pathInContext + "/").equals(servletPath)) {
                pathInContext = "";
            }
        }
        
        try {
            URL url = new URL(documentRoot + "/" + pathInContext);
            return new URLResource(url, url.openConnection()) {
                private static final long serialVersionUID = 8560952113883507717L;
                
                @Override
                public File getFile() throws IOException {
                    return null;
                }
                
            };
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
    
    @Override
    public String getInitParameter(String name) {
        if ("resourceBase".equals(name)) {
            return documentRoot;
        } else if ("redirectWelcome".equals(name)) {
            return "true";
        } else {
            return super.getInitParameter(name);
        }
    }
    
}
