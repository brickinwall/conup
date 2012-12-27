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

package org.apache.tuscany.sca.binding.atom;

/**
 * Factory for the Atom binding
 *
 * @version $Rev: 635751 $ $Date: 2008-03-10 23:09:08 +0000 (Mon, 10 Mar 2008) $
 */
public interface AtomBindingFactory {

    /**
     * Creates a new Atom binding.
     * @return the new Atom binding
     */
    AtomBinding createAtomBinding();

}
