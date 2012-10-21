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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.DataPipe;
import org.apache.tuscany.sca.databinding.DataPipeTransformer;

public class StreamDataPipe extends BaseTransformer<OutputStream, InputStream> implements
    DataPipeTransformer<OutputStream, InputStream> {

    public DataPipe<OutputStream, InputStream> newInstance() {
        return new Pipe();
    }

    @Override
    protected Class<InputStream> getTargetType() {
        return InputStream.class;
    }

    @Override
    public int getWeight() {
        return 50;
    }

    @Override
    protected Class<OutputStream> getSourceType() {
        return OutputStream.class;
    }

    public static class Pipe implements DataPipe<OutputStream, InputStream> {
        private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        public InputStream getResult() {
            return new ByteArrayInputStream(outputStream.toByteArray());
        }

        public OutputStream getSink() {
            return outputStream;
        }

    }

}
