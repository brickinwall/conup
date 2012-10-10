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

package org.apache.tuscany.sca.interfacedef.java.jaxws;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev: 661921 $ $Date: 2008-05-31 01:40:42 +0100 (Sat, 31 May 2008) $
 */
public class TestGenericClass <T extends Serializable & List<String>, S> {
    public TestGenericClass<?, S> i;
    public T f1;
    public T[] f2;
    public S f3;
    public List<? extends T> list1;
    public List<?> list2;
    public List<? extends Serializable> list3;
    public int f4;
    public int[] f5;
    public Map<? super T, S> map;
}
