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
package org.apache.tuscany.sca.databinding.xml;

import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.javabeans.JavaBean2XMLStreamReaderTransformer;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;

/**
 *
 * @version $Rev: 801895 $ $Date: 2009-08-07 07:56:48 +0100 (Fri, 07 Aug 2009) $
 */
public class JavaBean2XMLStreamReaderTestCase {
    private static final String XML_RESULT =
        "<?xml version='1.0' encoding='UTF-8'?>" 
            + "<MyBean xmlns=\"http://xml.databinding.sca.tuscany.apache.org/\">"
            + "<arr>1</arr><arr>2</arr><arr>3</arr><bean><name>Name</name></bean><i>1</i><str>ABC</str>"
            + "</MyBean>";

    @Test
    public void testTransformation() throws Exception {
        JavaBean2XMLStreamReaderTransformer t2 = new JavaBean2XMLStreamReaderTransformer();
        MyBean bean = new MyBean();
        bean.str = "ABC";
        bean.i = 1;
        bean.arr = new long[] {1, 2, 3};
        bean.bean = new AnotherBean();
        bean.bean.setName("Name");
        XMLStreamReader reader = t2.transform(bean, null);
        XMLStreamReader2String t3 = new XMLStreamReader2String(new DefaultExtensionPointRegistry());
        String xml = t3.transform(reader, null);
        XMLAssert.assertXMLEqual(XML_RESULT, xml);

    }

    private static class MyBean {
        private String str;
        private int i;
        private long arr[];
        private AnotherBean bean;

        /**
         * @return the arr
         */
        public long[] getArr() {
            return arr;
        }

        /**
         * @param arr the arr to set
         */
        public void setArr(long[] arr) {
            this.arr = arr;
        }

        /**
         * @return the i
         */
        public int getI() {
            return i;
        }

        /**
         * @param i the i to set
         */
        public void setI(int i) {
            this.i = i;
        }

        /**
         * @return the str
         */
        public String getStr() {
            return str;
        }

        /**
         * @param str the str to set
         */
        public void setStr(String str) {
            this.str = str;
        }

        /**
         * @return the bean
         */
        public AnotherBean getBean() {
            return bean;
        }

        /**
         * @param bean the bean to set
         */
        public void setBean(AnotherBean bean) {
            this.bean = bean;
        }

    }

    private static class AnotherBean {
        private String name;

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }
    }

}
