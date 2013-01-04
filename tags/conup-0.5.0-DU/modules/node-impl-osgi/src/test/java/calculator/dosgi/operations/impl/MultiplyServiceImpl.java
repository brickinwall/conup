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
package calculator.dosgi.operations.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import calculator.dosgi.operations.MultiplyService;

/**
 * An implementation of the Multiply service.
 */
public class MultiplyServiceImpl implements MultiplyService {

    public double multiply(double n1, double n2) {
        Logger logger = Logger.getLogger("calculator");
        logger.log(Level.INFO, "Multiplying " + n1 + " with " + n2);
        return n1 * n2;
    }

}
