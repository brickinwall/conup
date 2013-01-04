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

package org.apache.tuscany.sca.databinding.extension;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.tuscany.sca.databinding.impl.XSDDataTypeConverter;
import org.junit.Test;

/**
 *
 * @version $Rev: 722501 $ $Date: 2008-12-02 15:39:59 +0000 (Tue, 02 Dec 2008) $
 */
public class XSDDataTypeConverterTestCase {

    @Test
    public void testConvert() throws Exception {
        XSDDataTypeConverter c = new XSDDataTypeConverter();
        assertEquals("123", c.parseAnySimpleType(c.printAnySimpleType("123")));
        assertEquals(true, c.parseBoolean(c.printBoolean(true)));
        assertEquals(false, c.parseBoolean(c.printBoolean(false)));
        assertEquals(123.0, c.parseDouble(c.printDouble(123.0)), 0);
        assertEquals(123.0f, c.parseFloat(c.printFloat(123.0f)), 0);
        assertEquals(64, c.parseByte(c.printByte((byte)64)));
        assertEquals(123, c.parseInt(c.printInt(123)));
        assertEquals(new BigInteger("123456"), c.parseInteger(c.printInteger(new BigInteger("123456"))));
        assertEquals(123456L, c.parseLong(c.printLong(123456L)));
        assertEquals((short)123, c.parseShort(c.printShort((short)123)));

        Calendar calendar = new GregorianCalendar();
        String s = c.printDate(calendar);
        calendar = c.parseDate(s);
        assertEquals(s, c.printDate(calendar));

    }

}
