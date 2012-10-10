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

package org.apache.tuscany.sca.common.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.xml.sax.InputSource;

/**
 * @version $Rev: 938056 $ $Date: 2010-04-26 15:12:30 +0100 (Mon, 26 Apr 2010) $]
 * @tuscany.spi.extension.asclient
 */
public class XMLDocumentHelper {
    protected static final int BUFFER_SIZE = 256;

    /**
     * Detect the XML encoding of the document
     * 
     * @param is The input stream
     * @return The encoding
     * @throws IOException
     */
    public static String getEncoding(InputStream is) throws IOException {
        if (!is.markSupported())
            is = new BufferedInputStream(is);

        byte[] buffer = readBuffer(is);
        return getXMLEncoding(buffer);
    }

    /**
     * Searches the array of bytes to determine the XML encoding.
     */
    protected static String getXMLEncoding(byte[] bytes) {
        String javaEncoding = null;

        if (bytes.length >= 4) {
            if (((bytes[0] == -2) && (bytes[1] == -1)) || ((bytes[0] == 0) && (bytes[1] == 60)))
                javaEncoding = "UnicodeBig";
            else if (((bytes[0] == -1) && (bytes[1] == -2)) || ((bytes[0] == 60) && (bytes[1] == 0)))
                javaEncoding = "UnicodeLittle";
            else if ((bytes[0] == -17) && (bytes[1] == -69) && (bytes[2] == -65))
                javaEncoding = "UTF8";
        }

        String header = null;

        try {
            if (javaEncoding != null)
                header = new String(bytes, 0, bytes.length, javaEncoding);
            else
                header = new String(bytes, 0, bytes.length);
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        if (!header.startsWith("<?xml"))
            return "UTF-8";

        int endOfXMLPI = header.indexOf("?>");
        int encodingIndex = header.indexOf("encoding", 6);

        if ((encodingIndex == -1) || (encodingIndex > endOfXMLPI))
            return "UTF-8";

        int firstQuoteIndex = header.indexOf("\"", encodingIndex);
        int lastQuoteIndex;

        if ((firstQuoteIndex == -1) || (firstQuoteIndex > endOfXMLPI)) {
            firstQuoteIndex = header.indexOf("'", encodingIndex);
            lastQuoteIndex = header.indexOf("'", firstQuoteIndex + 1);
        } else
            lastQuoteIndex = header.indexOf("\"", firstQuoteIndex + 1);

        return header.substring(firstQuoteIndex + 1, lastQuoteIndex);
    }

    protected static byte[] readBuffer(InputStream is) throws IOException {
        if (is.available() == 0) {
            return new byte[0];
        }

        byte[] buffer = new byte[BUFFER_SIZE];
        is.mark(BUFFER_SIZE);
        int bytesRead = is.read(buffer, 0, BUFFER_SIZE);
        int totalBytesRead = bytesRead;

        while (bytesRead != -1 && (totalBytesRead < BUFFER_SIZE)) {
            bytesRead = is.read(buffer, totalBytesRead, BUFFER_SIZE - totalBytesRead);

            if (bytesRead != -1)
                totalBytesRead += bytesRead;
        }

        if (totalBytesRead < BUFFER_SIZE) {
            byte[] smallerBuffer = new byte[totalBytesRead];
            System.arraycopy(buffer, 0, smallerBuffer, 0, totalBytesRead);
            smallerBuffer = buffer;
        }

        is.reset();
        return buffer;
    }

    public static InputSource getInputSource(URL url) throws IOException {
        InputStream is = openStream(url);
        return getInputSource(url, is);
    }

    private static InputStream openStream(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        if (connection instanceof JarURLConnection) {
            // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5041014
            connection.setUseCaches(false);
        }
        InputStream is = connection.getInputStream();
        return is;
    }

    public static InputSource getInputSource(URL url, InputStream is) throws IOException {
        // is = new BufferedInputStream(is);
        // String encoding = getEncoding(is);
        InputSource inputSource = new InputSource(is);
        // inputSource.setEncoding(encoding);
        // [rfeng] Make sure we set the system id as it will be used as the base URI for nested import/include 
        inputSource.setSystemId(url.toString());
        return inputSource;
    }

}
