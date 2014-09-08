package ocha.itolab.koala.core.mesh;

import ocha.itolab.koala.core.data.*;
import ocha.itolab.koala.core.forcedirected.*;

import java.util.*;
import java.io.*;



/**
 * 元データをLinLogLayoutのデータ構造に変換して座標値を算出し
 * その結果を元データに反映する
 */
public class InitialLayoutInvoker {
	static Graph graph;
	static Mesh mesh;
	static Vector edgelist = new Vector();
	static Vector nodelist = new Vector();
	static Vector fixlist = new Vector();
	
	static int LINLOG = 1;
	static int READGML = 2;
	static int method = LINLOG;
	static String path = "C:/itot/projects/FRUITSNet/Koala/ogdf-layout/";
 	static String filename = "edges_fme.gml";
	
	/**
	 * Execute
	 */
	public static void exec(Graph g, Mesh m) {
		graph = g;
		mesh = m;
		constructEdge();

		// for test
		writeEdgeFile();
				
		if(method == LINLOG)
			LinLogLayout.exec(edgelist, nodelist, null);
		if(method == READGML)
			GmlFileReader.read(edgelist, nodelist, (path + filename));
		
		calcNodePosition();
		
		// for test
		printEdgeLength();
	}
	
	
	/**
	 * Construct edges
	 */
	static void constructEdge() {

		// Clear
		edgelist.clear();
		
		// for each pair of vertices
		for(int i = 0; i < mesh.getNumVertices(); i++) {
			Vertex v1 = mesh.getVertex(i);
			for(int j = (i + 1); j < mesh.getNumVertices(); j++) {
				Vertex v2 = mesh.getVertex(j);
			
				int count = 0;
				ArrayList<Node> nodes1 = v1.getNodes();
				for(int ii = 0; ii < nodes1.size(); ii++) {
					Node n1 = nodes1.get(ii);
					ArrayList<Node> nodes2 = v2.getNodes();
					for(int jj = 0; jj < nodes2.size(); jj++) {
						Node n2 = nodes2.get(jj);
						if(graph.isTwoNodeConnected(n1, n2) == true)
							count++;
					}
				}
		
				// Add an edge
				if(count > 0)
					addEdge(i, j, (double)count);
			}
		}

	}

	
	
	
	/**
	 * Add an edge
	 */
	static void addEdge(int id1, int id2, double weight) {
		
		// IDの大小関係を統一する
		if(id1 > id2) {
			int tmp = id1;
			id1 = id2;   id2 = tmp;
		}
		
		// 既存エッジを検索する
		for(int i = 0; i < edgelist.size(); i++) {
			InputEdge ie = (InputEdge)edgelist.elementAt(i);
			
			// 既存エッジが存在するなら、それに重みを加算する
			if(ie.node1 == id1 && ie.node2 == id2) {
				ie.weight += weight;
				return;
			}
		}
		
		// 新規エッジを生成する
		InputEdge ie = new InputEdge();
		ie.node1 = id1;
		ie.node2 = id2;
		ie.weight = weight;
		edgelist.add(ie);
		
	}
	
	

	/**
	 * ノード座標値を算出する
	 */
	static void calcNodePosition() {
		double xmin = 1.0e+30, xmax = -1.0e+30;
		double ymin = 1.0e+30, ymax = -1.0e+30;
		double xa, xb, ya, yb;
		
		// リスト上の各ノードについて：
		//  座標値の最大・最小値を求める
		for(int i = 0; i < nodelist.size(); i++) {
			OutputNode on = (OutputNode)nodelist.elementAt(i);
			if(xmin > on.x) xmin = on.x;
			if(xmax < on.x) xmax = on.x;
			if(ymin > on.y) ymin = on.y;
			if(ymax < on.y) ymax = on.y;
		}

		// Set the positions
		for(int i = 0; i < nodelist.size(); i++) {
			OutputNode on = (OutputNode)nodelist.elementAt(i);
			Vertex v = mesh.getVertex(on.id);
			double x = ((on.x - xmin) / (xmax - xmin)) * 2.0 - 1.0;
			double y = ((on.y - ymin) / (ymax - ymin)) * 2.0 - 1.0;
			v.setPosition(x, y, 0.0);
			ArrayList<Node> nodes = v.getNodes();
			for(int j = 0; j < nodes.size(); j++) {
					//System.out.println("     " + j + " x=" + x + " y=" + y);
				Node n = nodes.get(j);
				n.setPosition(x, y);
			}
		}	
	}
	
	
	/**
	 * for test
	 */
	static void writeEdgeFile() {
		BufferedWriter writer;
		
		try {
			 writer = new BufferedWriter(
			    		new FileWriter(new File("edges.csv")));
			 if(writer == null) return;
			 
			 for(int i = 0; i < edgelist.size(); i++) {
				 InputEdge ie = (InputEdge)edgelist.get(i);
				 String line = ie.node1 + "," + ie.node2 + "," + ie.weight;
				 writer.write(line, 0, line.length());
				 writer.flush();
				 writer.newLine();
			 }
			
			 writer.close();
		
		} catch (Exception e) {
			System.err.println(e);
			writer = null;
			return;
		}
	}
	
	
	
	static void printEdgeLength() {
		BufferedWriter writer;
		
		try {
			 writer = new BufferedWriter(
			    		new FileWriter(new File("edgelength.txt")));
			 if(writer == null) return;
			 
			for(int i = 0; i < edgelist.size(); i++) {
				InputEdge ie = (InputEdge)edgelist.get(i);
				Vertex v1 = mesh.getVertex(ie.node1);
				Vertex v2 = mesh.getVertex(ie.node2);
				double p1[] = v1.getPosition();
				double p2[] = v2.getPosition();
				double dist = (p1[0] - p2[0]) * (p1[0] - p2[0]) + (p1[1] - p2[1]) * (p1[1] - p2[1]);
				String line = "  edge " + i + " weight=" + ie.weight + " length=" + dist;
				writer.write(line, 0, line.length());
				writer.flush();
				writer.newLine();
			}
			
			writer.close();
			
		} catch (Exception e) {
			System.err.println(e);
			writer = null;
			return;
		}
		
		
	}
	
}
