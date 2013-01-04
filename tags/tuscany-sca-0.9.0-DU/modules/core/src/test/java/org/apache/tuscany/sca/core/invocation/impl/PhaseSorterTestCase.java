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
package org.apache.tuscany.sca.core.invocation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PhaseSorterTestCase {
    private PhaseSorter<String> graph;

    @Before
    public void setUp() throws Exception {
        graph = new PhaseSorter<String>();
    }

    @Test
    public void testSort() {
        graph.addEdge("a", "b");
        graph.addEdge("a", "c");
        graph.addEdge("c", "d");
        graph.addEdge("b", "c");
        List<String> order = graph.topologicalSort(true);
        assertEquals(Arrays.asList("a", "b", "c", "d"), order);
        assertTrue(!graph.getVertices().isEmpty());

        graph.addEdge("d", "a");
        try {
            order = graph.topologicalSort(true);
            assertTrue("Should have failed", false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        graph.removeEdge("d", "a");
        order = graph.topologicalSort(false);
        assertEquals(Arrays.asList("a", "b", "c", "d"), order);
        assertTrue(graph.getVertices().isEmpty());
    }

    @After
    public void tearDown() throws Exception {
    }

}
