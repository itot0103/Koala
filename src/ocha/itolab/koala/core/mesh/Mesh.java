package ocha.itolab.koala.core.mesh;

import java.util.*;
import ocha.itolab.koala.core.data.*;

public class Mesh {
	public static double CLUSTER_NODE_DISTANCE = 0.005;
	public static double NODE_EMPHASIS_MAGNITUDE = 0.05;
	public static int[] LAYER_NUMNODES = {6, 18, 36, 60, 90, 126, 168, 216, 270, 330, 396, 468};
	
	ArrayList triangles, vertices;
	double minx, miny, maxx, maxy;
	public double keyEmphasis = 0.0;	
	
	public Mesh() {
		vertices = new ArrayList();
		triangles = new ArrayList();
		minx = miny = 1.0e+30;
		maxx = maxy = -1.0e+30;
	}

	public Vertex addOneVertex() {
		Vertex v = new Vertex();
		v.setId(vertices.size());
		vertices.add(v);
		return v;
	}

	public void removeOneVertex(Vertex vertex) {
		int id = vertex.getId();
		vertices.remove(vertex);
		for (int i = id; i < vertices.size(); i++) {
			Vertex v = (Vertex) vertices.get(i);
			v.setId(i);
		}
	}

	public ArrayList getVertices() {
		return vertices;
	}

	public Vertex getVertex(int id) {
		return (Vertex) vertices.get(id);
	}

	public int getNumVertices() {
		return vertices.size();
	}


	public Triangle addOneTriangle() {
		Triangle t = new Triangle();
		t.setId(triangles.size());
		triangles.add(t);
		return t;
	}

	public void removeOneTriangle(Triangle triangle) {
		int id = triangle.getId();
		triangles.remove(triangle);
		for (int i = id; i < triangles.size(); i++) {
			Triangle t = (Triangle) triangles.get(i);
			t.setId(i);
		}
	}

	public ArrayList getTriangles() {
		return triangles;
	}

	public Triangle getTriangle(int id) {
		return (Triangle) triangles.get(id);
	}

	public int getNumTriangles() {
		return triangles.size();
	}

	

	/**
	 * Calculate the positions of nodes from the positions of vertices
	 */
	public void finalizePosition() {
		
		// for each vertex
		for(int i = 0; i < vertices.size(); i++) {
			Vertex v = (Vertex)vertices.get(i);
			double pos[] = v.pos;

			// If no nodes are associated
			if(v.nodes.size() <= 0) continue;

			// Copy the position of the vertex if just one node is related
			else if(v.nodes.size() == 1) {
				Node n = (Node)v.nodes.get(0);
				n.setPosition(pos[0], pos[1]);
			}
			
			// for each layer
			double layerDistance = Mesh.CLUSTER_NODE_DISTANCE * (1.0 - keyEmphasis);
			for(int j = 0; j <= LAYER_NUMNODES.length; j++) {
				
				// determine the number of the nodes in this layer
				int nlnodes = 0;
				if(j == 0) {
					nlnodes = (v.nodes.size() <= LAYER_NUMNODES[0]) ? v.nodes.size() : LAYER_NUMNODES[0];
				}
				else if(j < LAYER_NUMNODES.length)
					nlnodes = (v.nodes.size() >= LAYER_NUMNODES[j])
						? (LAYER_NUMNODES[j] - LAYER_NUMNODES[j - 1]) : (v.nodes.size() - LAYER_NUMNODES[j - 1]);
				else
					nlnodes = v.nodes.size() - LAYER_NUMNODES[j - 1];				
				if(nlnodes <= 0) break;
				
				for(int k = 0; k < nlnodes; k++) {
					Node n = (j == 0) ? (Node)v.nodes.get(k) : (Node)v.nodes.get(k + LAYER_NUMNODES[j - 1]);
					double theta = 2.0 * Math.PI * (double)k / (double)nlnodes;
					double dx = Math.cos(theta);
					double dy = Math.sin(theta);
					//System.out.println("    " + j + " theta=" + theta + " x=" + dx + " y=" + dy);
					double x = pos[0] + dx * layerDistance * (double)(j + 1);
					double y = pos[1] + dy * layerDistance * (double)(j + 1);
					n.setPosition(x, y);
				}
			}
			
			
		}
		
	}
	

}
