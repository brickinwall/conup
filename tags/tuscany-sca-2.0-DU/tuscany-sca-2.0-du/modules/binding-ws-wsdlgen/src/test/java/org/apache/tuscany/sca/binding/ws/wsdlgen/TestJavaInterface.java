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

package org.apache.tuscany.sca.binding.ws.wsdlgen;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import org.apache.tuscany.sca.binding.ws.other.Other;

import org.oasisopen.sca.annotation.OneWay;
import org.oasisopen.sca.annotation.Remotable;

/**
 *
 * @version $Rev: 1063125 $ $Date: 2011-01-25 03:38:57 +0000 (Tue, 25 Jan 2011) $
 */
@Remotable
@WebService
public interface TestJavaInterface {
    String m1(String str);

    @OneWay
    @WebMethod
    void m2(int i);

    @WebMethod
    String m3();

    void m4();

    @WebMethod
    String m5(String str, int i);

    @WebMethod(exclude = true)
    void dummy();

    @WebMethod
    void m6(TestJavaClass info) throws TestException;

    @WebMethod
    void m7(TestJavaClass info) throws TestFault;
    
    @WebMethod
    @SOAPBinding(parameterStyle=ParameterStyle.BARE)
    void m8(String str);
    
    @WebMethod
    @SOAPBinding(parameterStyle=ParameterStyle.BARE)
    int m9(String str);

    @WebMethod
    @SOAPBinding(parameterStyle=ParameterStyle.BARE)
    Other m10(Other other);
}
