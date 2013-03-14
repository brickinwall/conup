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
package echo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oasisopen.sca.annotation.Remotable;

import bean.TestBean;

/**
 * Interface of our sample JSONRPC service.
 * 
 * @version $Rev: 1004744 $ $Date: 2010-10-06 02:03:45 +0800 (周三, 06 十月 2010) $
 */
@Remotable
public interface Echo {

    String echo(String msg);
    
    void echoVoid();
    
    void echoRuntimeException() throws RuntimeException;

    void echoBusinessException() throws EchoBusinessException;

    int echoInt(int param);
    
    double echoDouble(double param);

    boolean echoBoolean(boolean param);

    Map echoMap(HashMap map);

    TestBean echoBean(TestBean testBean);

    List echoList(ArrayList list);

    String [] echoArrayString(String[] stringArray);

    int [] echoArrayInt(int[] intArray);

    Set echoSet(HashSet set);

    void get\u03a9\u03bb\u03c0();

    BigDecimal echoBigDecimal(BigDecimal param);
}
