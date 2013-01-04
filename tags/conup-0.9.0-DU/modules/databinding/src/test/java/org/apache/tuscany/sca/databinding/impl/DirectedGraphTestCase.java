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
package org.apache.tuscany.sca.databinding.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.tuscany.sca.databinding.impl.DirectedGraph.Edge;
import org.apache.tuscany.sca.databinding.impl.DirectedGraph.Vertex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @version $Rev: 906613 $ $Date: 2010-02-04 19:36:42 +0000 (Thu, 04 Feb 2010) $
 */
public class DirectedGraphTestCase {
    private DirectedGraph<String, Object> graph;

    @Before
    public void setUp() throws Exception {
        graph = new DirectedGraph<String, Object>();
    }

    @Test
    public void testGraph() {
        graph.addEdge("a", "b", null, 4, true);
        graph.addEdge("a", "b", null, 5, true);
        Assert.assertEquals(4, graph.getEdge("a", "b").getWeight());
        graph.addEdge("a", "b", null, 3, true);
        Assert.assertEquals(3, graph.getEdge("a", "b").getWeight());
        graph.addEdge("b", "c", null, 1, true);
        // graph.addEdge("a", "c", null, 8, true);
        graph.addEdge("a", "d", null, 3, true);
        graph.addEdge("b", "d", null, 2, true);
        graph.addEdge("d", "c", null, 3, true);
        graph.addEdge("c", "b", null, 1, true);
        graph.addEdge("c", "d", null, 2, true);
        graph.addEdge("d", "b", null, 1, true);
        graph.addEdge("a", "e", null, 8, true);
        graph.addEdge("c", "c", null, 2, true);
        graph.addEdge("f", "g", null, 2, false);
        graph.addEdge("f", "h", null, 8, true);
        graph.addEdge("g", "j", null, 2, false);
        graph.addEdge("j", "i", null, 2, true);
        graph.addEdge("h", "i", null, 8, true);

        Vertex vertex = graph.getVertex("a");
        Assert.assertNotNull(vertex);
        Assert.assertEquals(vertex.getValue(), "a");

        Assert.assertNull(graph.getVertex("1"));

        Edge edge = graph.getEdge("a", "b");
        Assert.assertNotNull(edge);
        Assert.assertEquals(edge.getWeight(), 3);

        edge = graph.getEdge("b", "a");
        Assert.assertNull(edge);

        DirectedGraph<String, Object>.Path path = graph.getShortestPath("a", "c");

        List<DirectedGraph<String, Object>.Edge> edges = path.getEdges();
        Assert.assertEquals(edges.size(), 2);
        Assert.assertEquals(edges.get(0), graph.getEdge("a", "b"));
        Assert.assertEquals(edges.get(1), graph.getEdge("b", "c"));

        Assert.assertEquals(path.getWeight(), 4);

        DirectedGraph<String, Object>.Path path2 = graph.getShortestPath("b", "e");
        Assert.assertNull(path2);

        DirectedGraph<String, Object>.Path path3 = graph.getShortestPath("a", "a");
        Assert.assertTrue(path3.getWeight() == 0 && path3.getEdges().isEmpty());

        DirectedGraph<String, Object>.Path path4 = graph.getShortestPath("c", "c");
        Assert.assertTrue(path4.getWeight() == 2 && path4.getEdges().size() == 1);

        DirectedGraph<String, Object>.Path path5 = graph.getShortestPath("f", "i");
        Assert.assertTrue(path5.getWeight() == 16 && path5.getEdges().size() == 2);

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
}
