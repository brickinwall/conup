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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oasisopen.sca.annotation.AllowsPassByReference;

import bean.TestBean;

/**
 * A simple client component that uses a reference with an JSONRPC binding.
 *
 * @version $Rev: 1180795 $ $Date: 2011-10-10 15:07:31 +0800 (周一, 10 十月 2011) $
 */
@AllowsPassByReference
public class EchoComponentImpl implements Echo {

    public String echo(String msg) {
        System.out.println("Echo: "+ msg);
        return "echo: " + msg;
    }

    
    public void echoVoid() {
        System.out.println("Echo: VOID");
    }

    public void echoBusinessException() throws EchoBusinessException {
        throw new EchoBusinessException("Business Exception");

    }

    public void echoRuntimeException() throws RuntimeException {
        throw new RuntimeException("Runtime Exception");
    }

    public int echoInt(int param) {
        int value = param;
        return value;
    }

    public double echoDouble(double param) {
    	double value = param;
    	return param;
    }

    public boolean echoBoolean(boolean param) {
        boolean value = param;
        return value;
    }

    public Map echoMap(HashMap param) {
        Map map = new HashMap();
        map = param;
        return map;
    }

    public TestBean echoBean(TestBean testBean1) {
        TestBean testBean = new TestBean();
        testBean.setTestString(testBean1.getTestString());
        testBean.setTestInt(testBean1.getTestInt());
        testBean.setStringArray(testBean1.getTestStringArray());
        return testBean;
    }

    public List echoList(ArrayList param){
        List list = new ArrayList();
        for(Iterator itr = param.iterator();itr.hasNext();)
        {
            list.add(itr.next());
        }
        return list;
    }

    public String[] echoArrayString(String[] stringArray) {
        return stringArray;
    }

    public int[] echoArrayInt(int[] intArray) {
        return intArray;
    }

    public Set echoSet(HashSet param){
        Set set = new HashSet();
        set  = param;
        return set;

    }

    public void get\u03a9\u03bb\u03c0() {

    }

    public BigDecimal echoBigDecimal(BigDecimal param) {
        return param;
    }
}
