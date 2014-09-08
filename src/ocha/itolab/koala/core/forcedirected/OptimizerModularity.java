//Copyright (C) 2008 Andreas Noack
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA 

package ocha.itolab.koala.core.forcedirected;
import java.util.*;

/**
 * Optimizer for a generalization of Mark Newman's Modularity measure,
 *   for computing graph clusterings.
 * The Modularity measure is generalized to arbitrary node weights;
 *   it is recommended to set the weight of each node to its degree,
 *   i.e. the total weight of its edges, as Newman did.
 * For more information on the (used version of the) Modularity measure, see
 *   M. E. J. Newman: "Analysis of weighted networks", 
 *   Physical Review E 70, 056131, 2004.
 * For the relation of Modularity to the LinLog energy model, see
 *   Andreas Noack: "Energy Models for Graph Clustering",
 *   Journal of Graph Algorithms and Applications 11(2):453-480, 2007.
 * Freely available at 
 *   <a href="http://jgaa.info/"><code>http://jgaa.info/</code></a>.
 *
 * @author Andreas Noack (an@informatik.tu-cottbus.de)
 * @version 13.01.2008
 */
public class OptimizerModularity {

    /**
     * Returns the clustering quality.
     * @param interAtedges  edge weight between different clusters
     * @param interAtpairs  weighted node pairs between different clusters
     * @param atedges  total edge weight of the graph
     * @param atpairs  total weighted node pairs of the graph
     * @param minShiftedCut  compute negative(!) Modularity (<code>true</code>) 
     *   or Normalized Cut (<code>false</code>)
     * @return clustering quality
     */
    private double quality(final double interAtedges, final double interAtpairs, 
            final double atedges, final double atpairs, final boolean minShiftedCut) {
        if (minShiftedCut) {
            return interAtedges/atedges - interAtpairs/atpairs;
        } else {
            return interAtedges / interAtpairs;
        }
    }
    
    
    /**
     * Improves a graph clustering by greedily moving nodes between clusters.
     * @param nodeToCluster  graph nodes with their current clusters 
     *   (input and output parameter)
     * @param nodeToEdges  graph nodes with their incident edges
     * @param atedges  total edge weight of the graph
     * @param atpairs  total weighted node pairs of the graph
     * @param minShiftedCut  minimize negative(!) modularity (<code>true</code>) 
     *   or normalized cut (<code>false</code>)
     */
    private void refine(final Map<FdNode,Integer> nodeToCluster, final Map<FdNode,List<FdEdge>> nodeToEdges, 
            final double atedges, final double atpairs, final boolean minShiftedCut) {
        int maxCluster = 0;
        for (int cluster : nodeToCluster.values()) {
            maxCluster = Math.max(maxCluster, cluster);
        }

        // compute clusterToAtnodes, interAtedges, interAtpairs
        double[] clusterToAtnodes = new double[nodeToCluster.keySet().size()+1];
        for (FdNode node : nodeToCluster.keySet()) {
            clusterToAtnodes[nodeToCluster.get(node)] += node.weight;
        }
        double interAtedges = 0.0;
        for (List<FdEdge> edges : nodeToEdges.values()) {
            for (FdEdge edge : edges) {
                if ( !nodeToCluster.get(edge.startNode).equals(nodeToCluster.get(edge.endNode)) ) {
                    interAtedges += edge.weight;
                }
            }
        }
        double interAtpairs = 0.0;
        for (FdNode node : nodeToCluster.keySet()) interAtpairs += node.weight;
        interAtpairs *= interAtpairs; 
        for (double clusterAtnodes : clusterToAtnodes) interAtpairs -= clusterAtnodes * clusterAtnodes;

        // greedily move nodes between clusters 
        double prevQuality = Double.MAX_VALUE;
        double quality = quality(interAtedges, interAtpairs, atedges, atpairs, minShiftedCut);
        //System.out.println("Refining " + nodeToCluster.keySet().size()  + " nodes, initial quality " + quality);
        while (quality < prevQuality) {
            prevQuality = quality;
            for (FdNode node : nodeToCluster.keySet()) {
                int bestCluster = 0; 
                double bestQuality = quality, bestInterAtedges = interAtedges, bestInterAtpairs = interAtpairs;
                double[] clusterToAtedges = new double[nodeToCluster.keySet().size()+1];
                for (FdEdge edge : nodeToEdges.get(node)) {
                    if (!edge.endNode.equals(node)) {
                        // count weight twice to include reverse edge
                        clusterToAtedges[nodeToCluster.get(edge.endNode)] += 2*edge.weight;
                    }
                }
                int cluster = nodeToCluster.get(node);
                for (int newCluster = 0; newCluster <= maxCluster+1; newCluster++) {
                    if (cluster == newCluster) continue;
                    double newInterPairs = interAtpairs
                        + clusterToAtnodes[cluster] * clusterToAtnodes[cluster]
                        - (clusterToAtnodes[cluster]-node.weight) * (clusterToAtnodes[cluster]-node.weight)
                        + clusterToAtnodes[newCluster] * clusterToAtnodes[newCluster]
                        - (clusterToAtnodes[newCluster]+node.weight) * (clusterToAtnodes[newCluster]+node.weight);
                    double newInterEdges = interAtedges 
                        + clusterToAtedges[cluster]
                        - clusterToAtedges[newCluster];
                    double newQuality = quality(newInterEdges, newInterPairs, atedges, atpairs, minShiftedCut); 
                    if (bestQuality - newQuality > 1e-8) {
                        bestCluster = newCluster;
                        bestQuality = newQuality; bestInterAtedges = newInterEdges; bestInterAtpairs = newInterPairs;
                    }
                }
                if (bestQuality < quality) {
                    clusterToAtnodes[cluster] -= node.weight;
                    clusterToAtnodes[bestCluster] += node.weight;
                    nodeToCluster.put(node, bestCluster);
                    maxCluster = Math.max(maxCluster, bestCluster);
                    quality = bestQuality; interAtedges = bestInterAtedges; interAtpairs = bestInterAtpairs;
                    //System.out.println(" Moving " + node + " to " + bestCluster + ", "  + "new quality " + quality);
                }
            }
        }
    }

    
    /**
     * Computes a graph clustering with a multi-scale algorithm.
     * @param nodes  graph nodes
     * @param edges  graph edges
     * @param atedges  total edge weight of the graph
     * @param atpairs  total weighted node pairs of the graph
     * @param minShiftedCut  minimize negative(!) modularity (<code>true</code>) 
     *   or normalized cut (<code>false</code>)
     * @return clustering with large Modularity or small Normalized Cut
     *   (depending on parameter <code>minShiftedCut</code>),
     *   as map from graph nodes to cluster IDs. 
     */
    private Map<FdNode,Integer> cluster(final Collection<FdNode> nodes, final List<FdEdge> edges, 
            final double atedges, final double atpairs, final boolean minShiftedCut) {
        //System.out.println("Contracting " + nodes.size() + " nodes, " + edges.size() + " edges");
        
        // contract nodes
        Collections.sort(edges, new Comparator<FdEdge>() { 
            public int compare(FdEdge e1, FdEdge e2) {
                if (e1.density == e2.density) return 0;
                return e1.density < e2.density ? +1 : -1;
            }
        });
        Map<FdNode,FdNode> nodeToContr = new HashMap<FdNode,FdNode>();
        List<FdNode> contrNodes = new ArrayList<FdNode>();
        for (FdEdge edge : edges) {
            if (edge.density < atedges/atpairs && (minShiftedCut || !nodeToContr.isEmpty())) break;
            if (edge.startNode.equals(edge.endNode)) continue;
            if (nodeToContr.containsKey(edge.startNode) || nodeToContr.containsKey(edge.endNode)) continue;
            // randomize contraction
            // if (!nodeToContr.isEmpty() && Math.random() < 0.5) continue;
            
           // System.out.println(" Contracting " + edge);
            FdNode contrNode = new FdNode(
                    edge.startNode.name + " " + edge.endNode.name,
                    edge.startNode.weight + edge.endNode.weight);
            nodeToContr.put(edge.startNode, contrNode);
            nodeToContr.put(edge.endNode, contrNode);
            contrNodes.add(contrNode);
        }
        // terminal case: no nodes to contract
        if (nodeToContr.isEmpty() || (!minShiftedCut && nodes.size() <= 2)) {
            Map<FdNode,Integer> nodeToCluster = new HashMap<FdNode,Integer>();
            int clusterId = 0;
            for (FdNode node : nodes) nodeToCluster.put(node, clusterId++);
            return nodeToCluster;
        }
        // "contract" singleton clusters
        for (FdNode node : nodes) {
            if (!nodeToContr.containsKey(node)) {
                FdNode contrNode = new FdNode(node.name, node.weight);
                nodeToContr.put(node, contrNode);
                contrNodes.add(contrNode);
            }
        }
        
        // contract edges
        Map<FdNode,Map<FdNode,Double>> startToEndToWeight = new HashMap<FdNode,Map<FdNode,Double>>();
        for (FdNode contrNode : contrNodes) {
            startToEndToWeight.put(contrNode, new HashMap<FdNode,Double>());
        }
        for (FdEdge edge : edges) {
            FdNode contrStart = nodeToContr.get(edge.startNode);
            FdNode contrEnd   = nodeToContr.get(edge.endNode);
            double contrWeight = 0.0;
            Map<FdNode,Double> endToWeight = startToEndToWeight.get(contrStart); 
            if (endToWeight.containsKey(contrEnd)) {
                contrWeight = endToWeight.get(contrEnd);
            }
            endToWeight.put(contrEnd, contrWeight + edge.weight);
        }   
        List<FdEdge> contrEdges = new ArrayList<FdEdge>();
        for (FdNode contrStart : startToEndToWeight.keySet()) {
            Map<FdNode,Double> endToWeight = startToEndToWeight.get(contrStart);
            for (FdNode contrEnd : endToWeight.keySet()) {
                FdEdge contrEdge = new FdEdge(contrStart, contrEnd, endToWeight.get(contrEnd));
                contrEdges.add(contrEdge);
            }
        }

        // cluster contracted graph
        Map<FdNode,Integer> contrNodeToCluster 
            = cluster(contrNodes, contrEdges, atedges, atpairs, minShiftedCut);
        
        // decontract clustering
        Map<FdNode,Integer> nodeToCluster = new HashMap<FdNode,Integer>();
        for (FdNode node : nodeToContr.keySet()) {
            nodeToCluster.put(node, contrNodeToCluster.get(nodeToContr.get(node)));
        }

        // refine decontracted clustering
        Map<FdNode,List<FdEdge>> nodeToEdge = new HashMap<FdNode,List<FdEdge>>();
        for (FdNode node : nodes) nodeToEdge.put(node, new ArrayList<FdEdge>());
        for (FdEdge edge : edges) nodeToEdge.get(edge.startNode).add(edge);
        refine(nodeToCluster, nodeToEdge, atedges, atpairs, minShiftedCut);

        return nodeToCluster;
    }
    
    
    /**
     * Computes a clustering of a given graph by maximizing the Modularity
     *   or minimizing the Normalized Cut.
     * @param nodes  weighted nodes of the graph.
     *   It is recommended to set the weight of each node to the sum 
     *   of the weights of its edges.  Weights must not be negative.   
     * @param edges  weighted edges of the graph.
     *   Omit edges with weight 0.0 (i.e. non-edges).  
     *   For unweighted graphs use weight 1.0 for all edges.
     *   Weights must not be negative.   
     *   Weights must be symmetric, i.e. the weight  
     *   from node <code>n1</code> to node <code>n2</code> must be equal to
     *   the weight from node <code>n2</code> to node <code>n1</code>. 
     * @param minShiftedCut  minimize negative(!) Modularity (<code>true</code>) 
     *   or Normalized Cut (<code>false</code>)
     * @param ignoreLoops  set to <code>true</code> to use an adapted version
     *   of Modularity for graphs without loops (edges whose start node
     *   equals the end node)
     * @return clustering with large Modularity or small Normalized Cut
     *   (depending on parameter <code>minShiftedCut</code>),
     *   as map from graph nodes to cluster IDs. 
     */
    public Map<FdNode,Integer> execute(
            final List<FdNode> nodes, final List<FdEdge> edges, 
            final boolean minShiftedCut, final boolean ignoreLoops) {

        // compute atedgeCnt and atpairCnt
        double atedgeCnt = 0.0; 
        for (FdEdge edge : edges) {
            if (!ignoreLoops || !edge.startNode.equals(edge.endNode)) { 
                atedgeCnt += edge.weight;
            }
        }
        double atpairCnt = 0.0; 
        for (FdNode node : nodes) atpairCnt += node.weight;
        atpairCnt *= atpairCnt;
        if (ignoreLoops) { 
            for (FdNode node : nodes) atpairCnt -= node.weight*node.weight;
        }
        
        // compute clustering
        return cluster(nodes, edges, atedgeCnt, atpairCnt, minShiftedCut);
    }
    
}
