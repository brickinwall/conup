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
package services.echo;

import javax.ws.rs.QueryParam;

import org.oasisopen.sca.annotation.Remotable;

/**
 * Interface of our sample JSONRPC service.
 *
 * @version $Rev: 962391 $ $Date: 2010-07-09 03:22:29 +0100 (Fri, 09 Jul 2010) $
 */
@Remotable
public interface Echo {

    String echo(@QueryParam("msg") String msg);

    int echoInt(@QueryParam("param") int param);

    String [] echoArrayString(@QueryParam("msgArray") String[] stringArray);

    int [] echoArrayInt(@QueryParam("intArray") int[] intArray);

}
