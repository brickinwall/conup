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
package org.apache.tuscany.sca.binding.websocket;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.provider.BaseBindingImpl;

/**
 * Represents a binding to a Websocket service.
 */
public class WebsocketBinding extends BaseBindingImpl {

    public static final QName TYPE = new QName(SCA11_TUSCANY_NS, "binding.websocket");
    private String port;

    public WebsocketBinding() {
    }

    @Override
    public QName getType() {
        return TYPE;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

}
