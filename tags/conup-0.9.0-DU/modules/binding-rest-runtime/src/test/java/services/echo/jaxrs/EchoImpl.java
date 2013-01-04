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
package services.echo.jaxrs;


/**
 * A simple client component that uses a reference with an REST binding.
 *
 * @version $Rev: 1304128 $ $Date: 2012-03-22 23:19:17 +0000 (Thu, 22 Mar 2012) $
 */
public class EchoImpl implements Echo {

    public String echo(String msg) {
        return msg;
    }

    public int echoInt(int param) {
        int value = param;
        return value;
    }

    public String[] echoArrayString(String[] stringArray) {
        return stringArray;
    }

    public int[] echoArrayInt(int[] intArray) {
        return intArray;
    }
}
