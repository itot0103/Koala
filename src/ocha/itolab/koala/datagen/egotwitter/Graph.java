package ocha.itolab.koala.datagen.egotwitter;

import java.util.*;

public class Graph {
	HashMap nodemap = new HashMap();
	ArrayList<Node> nodelist = new ArrayList<Node>();
	ArrayList<Edge> edgelist = new ArrayList<Edge>();
	
	/**
	 * Allocate One Node
	 */
	Node addOneNode(String name) {
		Node node = new Node();
		node.name = name;
		node.id = nodelist.size();
		nodemap.put(name, node);
		nodelist.add(node);
		return node;
	}
	
	
	/**
	 * Allocate One Edge
	 */
	Edge addOneEdge(Node n1, Node n2) {
		Edge edge = new Edge();
		edge.id = edgelist.size();
		edge.node1 = n1;
		edge.node2 = n2;
		edgelist.add(edge);
		return edge;
	}
	
}
