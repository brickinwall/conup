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

package org.apache.tuscany.sca.node.impl;

import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.ContributionFactory;

/**
 * NodeUtil
 *
 * @version $Rev: 803742 $ $Date: 2009-08-13 00:43:27 +0100 (Thu, 13 Aug 2009) $
 */
public class NodeUtil {

    private NodeUtil() {
    }

    static Contribution contribution(ContributionFactory contributionFactory, org.apache.tuscany.sca.node.Contribution c) {
        Contribution contribution = contributionFactory.createContribution();
        contribution.setURI(c.getURI());
        contribution.setLocation(c.getLocation());
        contribution.setUnresolved(true);
        return contribution;
    }

}
