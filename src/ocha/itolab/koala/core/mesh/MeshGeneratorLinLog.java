package ocha.itolab.koala.core.mesh;

import java.util.*;
import ocha.itolab.koala.core.data.*;
import ocha.itolab.koala.core.forcedirected.*;

public class MeshGeneratorLinLog {
	static Mesh mesh = null;
	static Graph graph = null;
	static Vector edgelist = new Vector();
	static double FEATURE_THRESHOLD = 0.9;
	
	public static void execute(Mesh m, Graph g) {
		mesh = m;  graph = g;
		
		constructEdgeList();
		Map<String,Map<String,Double>> llgraph = LinLogLayout.constructGraph(edgelist);
		llgraph = LinLogLayout.makeSymmetricGraph(llgraph);
		Map<String,FdNode> nameToNode = LinLogLayout.makeNodes(llgraph);
        List<FdNode> nodes = new ArrayList<FdNode>(nameToNode.values());
        List<FdEdge> edges = LinLogLayout.makeEdges(llgraph,nameToNode);
		Map<FdNode,double[]> nodeToPosition = LinLogLayout.makeInitialPositions(nodes, false);
		// see class MinimizerBarnesHut for a description of the parameters
		new MinimizerBarnesHut(nodes, edges, 1.0, 3.0, 0.0001).minimizeEnergy(nodeToPosition, 100);
        // see class OptimizerModularity for a description of the parameters
        Map<FdNode,Integer> nodeToCluster = 
            new OptimizerModularity().execute(nodes, edges, true, false);
        generateMesh(nodeToPosition, nodeToCluster);
	}
	
	
	
	/**
	 * Construct edges
	 */
	static void constructEdgeList() {
		// Clear
		edgelist.clear();
		graph.setupDissimilarityForClustering();
		
		// for each pair of vertices
		for(int i = 0; i < graph.edges.size(); i++) {
			Edge e = graph.edges.get(i);
			Node nodes[] = e.getNode();
			int id1 = nodes[0].getId();
			int id2 = nodes[1].getId();
			
			//System.out.println("  dissim=" + nodes[0].getDisSim2(id2));
			if(nodes[0].getDisSim2(id2) < FEATURE_THRESHOLD) {
				continue;
			}
			
			
			if(id1 > id2) {
				int tmp = id1;
				id1 = id2;   id2 = tmp;
			}
			// 新規エッジを生成する
			InputEdge ie = new InputEdge();
			ie.node1 = id1;
			ie.node2 = id2;
			ie.weight = 1.0;
			edgelist.add(ie);
		}
	}
	
	
	static void generateMesh(Map<FdNode,double[]> nodeToPosition, Map<FdNode,Integer>nodeToCluster) {
		int numClusters = -1;
		
		// クラスタ数を確認する
		for (FdNode fdnode : nodeToPosition.keySet()) {
			int cluster = nodeToCluster.get(fdnode);
			//System.out.println("   cluster=" + cluster);
			if(cluster >= numClusters)
				numClusters = cluster + 1;
		}
		
		// クラスタを確保
		for(int i = 0; i < numClusters; i++) 
			mesh.addOneVertex();
		
		// ノードのクラスタ情報をリセット
		for(int i = 0; i < graph.nodes.size(); i++)
			graph.nodes.get(i).setVertex(null);
		
		// ノードのクラスタ情報をセット
		for (FdNode fdnode : nodeToPosition.keySet()) {
			int cluster = nodeToCluster.get(fdnode);
			int id = Integer.parseInt(fdnode.name);
			Node node = graph.nodes.get(id);
			Vertex vertex = mesh.getVertex(cluster);
			vertex.nodes.add(node);
			node.setVertex(vertex);
		}
		
		// 孤立ノードのクラスタ情報をセット
		for(int i = 0; i < graph.nodes.size(); i++) {
			Node node = graph.nodes.get(i);
			if(node.getVertex() != null) continue;
			Vertex vertex = mesh.addOneVertex();
			vertex.nodes.add(node);
			node.setVertex(vertex);
		}
				
	}
		
}
