package ocha.itolab.koala.core.mesh;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import mdsj.MDSJ;
import ocha.itolab.koala.core.data.*;
import ocha.itolab.koala.core.forcedirected.*;

public class MeshGenerator {
	static int CLUSTERING_BYMYSELF = 1;
	static int CLUSTERING_EXTERNAL = 2;
	static int clusteringMode = 1;
	
	static double clusteringThreshold = 1.1;
	static int clusteringMaxIteration = 50;

	
	
	public static Mesh generate(Graph g) {
		Mesh m = new Mesh();
		
		if(clusteringMode == CLUSTERING_BYMYSELF) {
			
			// Generate & cluster vertices
			generateVertices(m, g);
			clusterVertices(m, g);
		}
		
		if(clusteringMode == CLUSTERING_EXTERNAL) {
			writeEdgeFile(g);
			readClusteringFile(g, m);
		}
		
		
		// Calculate distances between vertices
		calcDistances(m, g);
		
		// Calculate initial positions of vertices
		InitialLayoutInvoker.exec(g, m);
		
		// Delaunay triangulation
		MeshTriangulator.triangulate(m);
		
		return m;
	}
	
	
	/**
	 * Generate vertices
	 */
	public static void generateVertices(Mesh mesh, Graph graph) {
		mesh.vertices.clear();
		
		// for each node of the graph
		for(int i = 0; i < graph.nodes.size(); i++) {
			Node node = (Node)graph.nodes.get(i);
			Vertex vertex = mesh.addOneVertex();
			vertex.nodes.add(node);
			node.setVertex(vertex);
			vertex.setPosition(node.getX(), node.getY(), 0.0);
		}
	}
	
	
	

	/**
	 * Cluster vertices
	 */
	public static void clusterVertices(Mesh mesh, Graph graph) {
			
		graph.setupDissimilarityForClustering();
		for(int i = 0; i < clusteringMaxIteration; i++) {
			double threshold = clusterVerticesOneStep(mesh);
			//System.out.println("    numnode=" + graph.nodes.size() + "  numvertex=" + mesh.getNumVertices() + "  th=" + threshold);
			if(threshold > graph.clustersizeRatio) break;
		}
		
	}
	
	
	
	/**
	 * One step of the vertex clustering
	 */
	static double clusterVerticesOneStep(Mesh mesh) {
		
		// for each pair of the vertices
		double mindis = 1.0e+30;
		for(int i = 0; i < mesh.getNumVertices(); i++) {
			Vertex v1 = mesh.getVertex(i);
			for(int j = (i + 1); j < mesh.getNumVertices(); j++) {
				Vertex v2 = mesh.getVertex(j);
			
				// for each pair of the nodes
				double maxdis = 0.0;
				for(int ii = 0; ii < v1.nodes.size(); ii++) {
					Node n1 = (Node)v1.nodes.get(ii);
					for(int jj = 0; jj < v2.nodes.size(); jj++) {
						Node n2 = (Node)v2.nodes.get(jj);
						if(n1.getDisSim2(n2.getId()) > maxdis) {
							maxdis = n1.getDisSim2(n2.getId());
						}
					}
				}
				
				// update mindis
				if(mindis > maxdis) {
					mindis = maxdis;
					//System.out.println("   updated mindis=" + mindis);
				}
			}
		}
		
		// Determine the threshold
		double threshold = mindis * clusteringThreshold;
		
		// Combine close two vertices 
		for(int i = 0; i < mesh.getNumVertices(); i++) {
			Vertex v1 = mesh.getVertex(i);
			for(int j = (i + 1); j < mesh.getNumVertices(); j++) {
				Vertex v2 = mesh.getVertex(j);
			
				// for each pair of the nodes
				double maxdis = -1.0;
				String authors = "";
				for(int ii = 0; ii < v1.nodes.size(); ii++) {
					Node n1 = (Node)v1.nodes.get(ii);
					for(int jj = 0; jj < v2.nodes.size(); jj++) {
						Node n2 = (Node)v2.nodes.get(jj);
						if(n1.getDisSim2(n2.getId()) > maxdis) {
							maxdis = n1.getDisSim2(n2.getId());
							authors = n1.getDescription(0) + "," + n2.getDescription(0);
						}
					}
				}				
				if(maxdis > threshold) continue;
				
				//System.out.println("   combine: i=" + i + " j=" + j + " names=" + authors + " maxdis=" + maxdis + " th=" + threshold);
				
				// combine the two vertices
				for(int jj = 0; jj < v2.nodes.size(); jj++) {
					Node n2 = (Node)v2.nodes.get(jj);
					v1.nodes.add(n2);
					n2.setVertex(v1);
				}
				mesh.removeOneVertex(v2);
				j--;
			}
		}
		
		return threshold;
	}
	
	

	
	/**
	 * Calculate dissimilarity between pairs of vertices
	 */
	static void calcDistances(Mesh mesh, Graph graph) {
		graph.setupDissimilarityForPlacement();

		// Setup an array for dissimilarity calculation
		for(int i = 0; i < mesh.getNumVertices(); i++) {
			Vertex v = mesh.getVertex(i);
			v.dissim = new double[mesh.getNumVertices()];
		}
			
		// for each pair of the vertices
		for(int i = 0; i < mesh.getNumVertices(); i++) {
			Vertex v1 = mesh.getVertex(i);
			for(int j = (i + 1); j < mesh.getNumVertices(); j++) {
				Vertex v2 = mesh.getVertex(j);
	
				// calculate inner product
				double dis1 = 0.0;
				if(graph.attributeType == graph.ATTRIBUTE_VECTOR) {
					double average1[] = new double[graph.vectorname.length];
					double average2[] = new double[graph.vectorname.length];
					for(int k = 0; k < graph.vectorname.length; k++) {
						for(int ii = 0; ii < v1.nodes.size(); ii++) {
							Node n1 = (Node)v1.nodes.get(ii);
							average1[k] += n1.getValue(k);
						}
						for(int ii = 0; ii < v2.nodes.size(); ii++) {
							Node n2 = (Node)v2.nodes.get(ii);
							average2[k] += n2.getValue(k);
						}
					}
					
					double d1 = 0.0, d2 = 0.0;		
					for(int k = 0; k < graph.vectorname.length; k++) {
						dis1 += (average1[k] * average2[k]);
						d1 += (average1[k] * average1[k]);
						d2 += (average2[k] * average2[k]);
					}
					if(dis1 < 0.0) dis1 = 0.0;
					else
						dis1 /= (Math.sqrt(d1) * Math.sqrt(d2));
					dis1 = 1.0 - dis1;
				}
				
				// retrieve distance value
				else {
					Node n1 = (Node)v1.nodes.get(0);
					Node n2 = (Node)v2.nodes.get(0);
					dis1 = n1.getDisSim1(n2.getId());
				}
				
				
				// for each pair of the nodes belonging to the two vertices
				int count = 0;
				for(int ii = 0; ii < v1.nodes.size(); ii++) {
					Node n1 = (Node)v1.nodes.get(ii);
					for(int jj = 0; jj < v2.nodes.size(); jj++) {
						Node n2 = (Node)v2.nodes.get(jj);
						if(graph.isTwoNodeConnected(n1, n2) == true)
							count++;
					}
				}
				double dis2 = 1.0 / (double)(1 + count);
				
				//double dis = graph.distanceRatio * dis1 + (1.0 - graph.distanceRatio) * dis2;
				double dis = dis2;
				v1.dissim[j] = v2.dissim[i] = dis;
				
			}
		}
	
	}

	
	
	
	static void writeEdgeFile(Graph graph) {
		BufferedWriter writer;
		
		try {
			 writer = new BufferedWriter(
			    		new FileWriter(new File("clusteredges.txt")));
			 if(writer == null) return;
			 
			for(int i = 0; i < graph.edges.size(); i++) {
				Edge e = graph.edges.get(i);
				Node nodes[] = e.getNode();
				String line = nodes[0].getId() + " " + nodes[1].getId();
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

	
	
	
	static String path = "C:/itot/projects/FRUITSNet/Koala/lib/";
	static String filename = "polbooks-clustering.txt";
	static int HIERARCHY_LEVEL = 1;
	
	static void readClusteringFile(Graph graph, Mesh mesh) {
		BufferedReader reader;
		int numv = 0;
		
		try {
			
			// first read
			File file = new File(path + filename);
			reader = new BufferedReader(new FileReader(file));
			reader.ready();
			while(true) {
				String line = reader.readLine();
				if(line == null) break;
				StringTokenizer token = new StringTokenizer(line);
				for(int i = 0; i < HIERARCHY_LEVEL; i++)
					token.nextToken();
				int n = Integer.parseInt(token.nextToken());
				if(n > numv) numv = n;
			}
			reader.close();
			
			// Allovate vertices
			for(int i = 0; i <= numv; i++) {
				Vertex vertex = mesh.addOneVertex();
			}
			
			// Second read
			file = new File(path + filename);
			reader = new BufferedReader(new FileReader(file));
			reader.ready();
			while(true) {
				String line = reader.readLine();
				if(line == null) break;
				StringTokenizer token = new StringTokenizer(line);
				int nid = Integer.parseInt(token.nextToken());
				Node node = graph.nodes.get(nid);
				for(int i = 1; i < HIERARCHY_LEVEL; i++)
					token.nextToken();
				int vid = Integer.parseInt(token.nextToken());
				Vertex vertex = mesh.getVertex(vid);
				vertex.nodes.add(node);
				node.setVertex(vertex);
			}
			reader.close();
						
			
			
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
}
	










	/*
	// Apply MDS
	double[][] output = MDSJ.classicalScaling(input);
	
	// Calculate positions
	double min1 = +1.0e+30, max1 = -1.0e+30;
	double min2 = +1.0e+30, max2 = -1.0e+30;
	for(int i = 0; i < mesh.getNumVertices(); i++) {
		min1 = (min1 < output[0][i]) ? min1 : output[0][i];
		max1 = (max1 > output[0][i]) ? max1 : output[0][i];
		min2 = (min2 < output[1][i]) ? min2 : output[1][i];
		max2 = (max2 > output[1][i]) ? max2 : output[1][i];
	}
	//System.out.println("   min1=" + min1 + " max1=" + max1 + "   min2=" + min2 + " max2=" + max2);
	for(int i = 0; i < mesh.getNumVertices(); i++) {
		Vertex v = mesh.getVertex(i);
		double x = ((output[0][i] - min1) / (max1 - min1)) * 2.0 - 1.0;
		double y = ((output[1][i] - min2) / (max2 - min2)) * 2.0 - 1.0;
		v.setPosition(x, y, 0.0);
		for(int j = 0; j < v.nodes.size(); j++) {
			//System.out.println("     " + j + " x=" + x + " y=" + y);
			Node n = (Node)v.nodes.get(j);
			n.setPosition(x, y);
		}
	}
	*/
	

