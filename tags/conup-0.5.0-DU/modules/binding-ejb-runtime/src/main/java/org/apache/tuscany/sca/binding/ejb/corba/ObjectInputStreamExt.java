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
package org.apache.tuscany.sca.binding.ejb.corba;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;

/**
 * @version $Rev: 665774 $ $Date: 2008-06-09 18:05:18 +0100 (Mon, 09 Jun 2008) $
 */
public class ObjectInputStreamExt extends ObjectInputStream {

    private ClassLoader classloader;

    public ObjectInputStreamExt(InputStream in, ClassLoader loader) throws IOException {
        super(in);
        this.classloader = loader;
    }

    @Override
    protected Class resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        return ClassLoadingUtil.loadClass(classDesc.getName(), classloader);
    }

    @Override
    protected Class resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
        Class[] cinterfaces = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++)
            cinterfaces[i] = classloader.loadClass(interfaces[i]);

        try {
            return Proxy.getProxyClass(classloader, cinterfaces);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }

    ClassLoader getClassloader() {
        return classloader;
    }

}
