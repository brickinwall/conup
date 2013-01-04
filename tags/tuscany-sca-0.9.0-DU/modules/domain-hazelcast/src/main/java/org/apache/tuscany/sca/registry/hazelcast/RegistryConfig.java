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

package org.apache.tuscany.sca.registry.hazelcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * Utility to parse the config properties.
 * 
 * bind - ip[:port] - defines the local bind address and port, it defaults port 14820 and if that port in use it will try 
 *                    incrementing by one till a free port is found.
 *             
 * multicast - groupip:port | off - defines if multicast discovery is used and if so what multicast ip group and port is used 
 *             defaults to 224.5.12.10:51482. A value of off means multicast is disabled.
 *             
 * wka - ip[:port] - a comma separated list of ip address and port for remote nodes in the domain group. The port defaults to 14820.
 *             
 * userid -  is the userid other nodes must use to connect to this domain group. The default is the default domain name.
 * 
 * password -  is the password other nodes must use to connect to this domain group. The default is 'tuscany'.
 *             
 * client - true means this is an SCAClient call           
 *             
 */
public class RegistryConfig {
    
    private String bindAddress = "*";
    private int bindPort = 14820;
    private boolean multicastDisabled = false;
    private String multicastAddress = "224.5.12.10";
    private int multicastPort = 51482;
    private List<String> wkas = new ArrayList<String>();
    private String userid;
    private String password;
    boolean client;
    
    public RegistryConfig(Properties properties) {
        init(properties);
    }

    private void init(Properties properties) {
   
        String bindValue = properties.getProperty("bind");
        if (bindValue != null) {
            if (bindValue.indexOf(":") == -1) {
                this.bindAddress = bindValue;
            } else {
                String[] addr = bindValue.split(":");
                this.bindAddress = addr[0];
                this.bindPort = Integer.parseInt(addr[1]);
            }
        }

        String multicastValue = properties.getProperty("multicast");
        if (multicastValue != null) {
            if ("off".equalsIgnoreCase(multicastValue)) {
                this.multicastDisabled = true;
            } else {
                if (multicastValue.indexOf(":") == -1) {
                    this.multicastAddress = multicastValue;
                } else {
                    String[] addr = multicastValue.split(":");
                    this.multicastAddress = addr[0];
                    this.multicastPort = Integer.parseInt(addr[1]);
                }
            }
        }
        
        String wkaValue = properties.getProperty("wka");
        if (wkaValue != null) {
            String[] ips = wkaValue.split(",");
            for (String ip : ips) {
                if (ip.indexOf(":") == -1) {
                    wkas.add(ip + ":14820");
                } else {
                    wkas.add(ip);
                }
            }
        }

        this.client = Boolean.parseBoolean(properties.getProperty("client", "false"));
        this.password = properties.getProperty("password", "tuscany");
        this.userid = properties.getProperty("userid", properties.getProperty("defaultDomainName", "default"));

    }

    public String getBindAddress() {
        return bindAddress;
    }

    public int getBindPort() {
        return bindPort;
    }

    public boolean isMulticastDisabled() {
        return multicastDisabled;
    }

    public String getMulticastAddress() {
        return multicastAddress;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public List<String> getWKAs() {
        return wkas;
    }
    
    public String getUserid() {
        return userid;
    }
    public String getPassword() {
        return password;
    }

    /**
     * Parse the config string into a Properties object.
     * The config URI has the following format:
     * uri:<domainName>?name=value&...
     */
    public static RegistryConfig parseConfigURI(String configURI) {
        Properties properties = new Properties();
        int c = configURI.indexOf(':');
        if (c > -1) {
           configURI = configURI.substring(c+1); 
        }
        int qm = configURI.indexOf('?');
        if (qm < 0) {
            properties.setProperty("defaultDomainName", configURI);
        } else {
            if (qm == 0) {
                properties.setProperty("defaultDomainName", "default");
            } else {
                properties.setProperty("defaultDomainName", configURI.substring(0, qm));
            }
            if (configURI.length() > qm+1) {
                Map<String, String> params = new HashMap<String, String>();
                for (String param : configURI.substring(qm+1).split("&")) {
                    String[] px = param.split("=");
                    if (px.length == 2) {
                        params.put(px[0], px[1]);
                    } else {
                        params.put(px[0], "");
                    }
                }
                for (String name : params.keySet()) {
                    properties.setProperty(name, params.get(name));
                }
            }
        }
        return new RegistryConfig(properties);
    }
}
