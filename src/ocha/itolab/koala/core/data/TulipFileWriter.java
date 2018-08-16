package ocha.itolab.koala.core.data;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import ocha.itolab.koala.core.mesh.*;


public class TulipFileWriter {
	static String filename = "tmp.tlp";
	static BufferedWriter writer = null;
	
	static ArrayList<ArrayList> cedgelist = new ArrayList<ArrayList>();
	
	
	/**
	 * write a tulip-format file
	 */
	public static void write(Graph graph) {
		String line = "";
		
		LocalDateTime ldt = LocalDateTime.now();
		String dt = ldt.toString().replaceAll("-", "").replaceAll(":", "").substring(0, 15);
        filename = dt + ".tlp";
        System.out.println(filename);
        
		// File open
		open(filename);
		if(writer == null) return;
		
		// setup edges in the clusters
		setupEdges(graph);
		
		// start the description
		writeOneLine("(tlp \"2.0\"");
		
		// describe a set of nodes
		int nnode = graph.nodes.size();
		line = "(nodes ";
		for(int i = 0; i < nnode; i++)
			line += (i + " ");
		line += ")";
		writeOneLine(line);
		
		// describe a set of edges
		int nedge = graph.edges.size();
		for(int i = 0; i < nedge; i++) {
			Edge edge = graph.edges.get(i);
			Node node[] = edge.nodes;
			line = "(edge " + i + " " + node[0].id + " " + node[1].id + ")";
			writeOneLine(line);
		}
		
		// describe clusters
		int ncluster = graph.mesh.getNumVertices();
		for(int i = 0; i < ncluster; i++) {
			Vertex v = (Vertex)graph.mesh.getVertices().get(i);
			line = "(cluster " + (i+1) + " \"cluster" + (i+1) + "\"";
			writeOneLine(line);
			
			// describe a set of nodes in this cluster
			ArrayList<Node> nodes = v.getNodes();
			if(nodes.size() > 0) {
				line = "  (nodes ";
				for(int j = 0; j < nodes.size(); j++) {
					Node node = nodes.get(j);
					line += node.id + " ";
				}
				line += ")";
				writeOneLine(line);
			}
			
			// describe a set of edges connecting a pair of nodes of this cluster
			ArrayList<Edge> list = cedgelist.get(i);
			if(list.size() > 0) {
				line = "  (edges ";
				for(int j = 0; j < list.size(); j++) {
					Edge edge = list.get(j);
					line += edge.id + " ";
				}
				line += ")";
				writeOneLine(line);
			}
			
			// close the description of this cluster
			writeOneLine(")");
		}
		
		
		// describe viewLayout
		writeOneLine("(property  0 layout \"viewLayout\" ");
		for(int i = 0; i < nnode; i++) {
			Node node = graph.nodes.get(i);
			line = "  (node " + node.id + " \"(" + node.getX() + "," + node.getY() + ",0)\")";
			writeOneLine(line);
		}
		writeOneLine(")");
		
		// describe viewSize
		writeOneLine("(property  0 size \"viewSize\" ");
		writeOneLine("  (default \"(0.01,0.01,0.0)\" )");
		writeOneLine(")");
		
		// end the description
		writeOneLine(")");
				
		// File close
		close();
		
	}
	
	
	
	static void setupEdges(Graph graph) {
		Mesh mesh = graph.mesh;
		
		// Allocate lists for each cluster (=vertex)
		for(int i = 0; i < mesh.getNumVertices(); i++)
			cedgelist.add(new ArrayList<Edge>());
		
		// for each edge
		for(int i = 0; i < graph.edges.size(); i++) {
			Edge e = graph.edges.get(i);
			Vertex v1 = e.nodes[0].vertex;
			Vertex v2 = e.nodes[1].vertex;
			if(v1 != null && v2 != null && v1.getId() == v2.getId()) {
				ArrayList<Edge> list = cedgelist.get(v1.getId());
				list.add(e);
				//System.out.println("   node1=" + e.nodes[0].id + "  node2=" + e.nodes[1].id);
			}
			
		}
		
		
	}
	
	
	
	static BufferedWriter open(String filename) {	
		try {
			 writer = new BufferedWriter(
			    		new FileWriter(new File(filename)));
			 System.out.println("KoalaFileWriter: " + filename);
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
		return writer;
	}
	
	
	static void close() {
		
		try {
			writer.close();
		} catch (Exception e) {
			System.err.println(e);
			return;
		}
	}
	

	static void writeOneLine(String word) {
		try {
			writer.write(word, 0, word.length());
			writer.flush();
			writer.newLine();
		} catch (Exception e) {
			System.err.println(e);
			return;
		}
	}	
}
