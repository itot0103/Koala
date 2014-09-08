package ocha.itolab.koala.core.mesh;

import ocha.itolab.koala.core.data.*;

public class MeshSmoother {
	static int numIteration = 10;
	static double idealDistance = 0.05;
	static double maxMovement = 0.005;
	static double tooSmall = 1.0e-10;
	static int maxDegree = 0;
	
	static Mesh mesh;
	
	
	/**
	 * Apply Laplacian smoothing
	 */
	public static void smooth(Mesh m, int mad) {
		mesh = m;
		maxDegree = mad;
		double movex[] = new double[mesh.getNumVertices()];
		double movey[] = new double[mesh.getNumVertices()];
		
		// for each triangle
		for (int j = 0; j < mesh.getNumTriangles(); j++) {
			Triangle t = mesh.getTriangle(j);
			Vertex v[] = t.getVertices();
			
			// for each edge of the triangle
			for (int k = 0; k < 3; k++) {
				
				// calculate the ideal length of the edge vector
				int k1 = (k == 2) ? 0 : (k + 1);
				Vertex v0 = v[k];
				Vertex v1 = v[k1];
				double idist = calcIdealDistance(v0, v1);
				double p0[] = v0.getPosition();
				double p1[] = v1.getPosition();
				double diff[] = new double[2];
				diff[0] = p0[0] - p1[0];
				diff[1] = p0[1] - p1[1];
				double difflen = Math.sqrt(diff[0] * diff[0] + diff[1] * diff[1]);
				if(difflen > idist) continue;
				if(difflen < tooSmall) continue;
				diff[0] *= (0.5 * idist / difflen);
				diff[1] *= (0.5 * idist / difflen);
				
				// Add the ideal length of the edge vector to the two vertex
				movex[v[k].getId()] += diff[0];
				movey[v[k].getId()] += diff[1];
				movex[v[k1].getId()] -= diff[0];
				movey[v[k1].getId()] -= diff[1];
			}
		}
		
		// for each vertex
		for(int i = 0; i < mesh.getNumVertices(); i++) {
			Vertex v = mesh.getVertex(i);
			double movelen = Math.sqrt(movex[i] * movex[i] + movey[i] * movey[i]);
			if(movelen > maxMovement) {
				movex[i] *= (maxMovement / movelen);
				movey[i] *= (maxMovement / movelen);
			}
			//System.out.println("   id=" + v.getId() + " movex=" + movex[i] + " movey=" + movey[i]);
			double pos[] = v.getPosition();
			v.setPosition(pos[0] + movex[i], pos[1] + movey[i], pos[2]);
			
			// for each node
			for(int j = 0; j < v.nodes.size(); j++) {
				Node node = (Node)v.nodes.get(j);
				double pos2[] = v.getPosition();
				node.setPosition(pos2[0], pos2[1]);
			}
  			
		}	
		
	}
	
	
	/**
	 * Calculate ideal distance between two vertices
	 */
	static double calcIdealDistance(Vertex v0, Vertex v1) {
		double d0 = calcRadius(v0);
		double d1 = calcRadius(v1);		
		double dist = d0 + d1 + idealDistance;
		return dist;
	}
	
	
	/**
	 *  Calculate radius of drawing range for one vertex
	 */
	static double calcRadius(Vertex v) {
		double max = 0.0;
		max = v.getNumNodeLayers() *  Mesh.CLUSTER_NODE_DISTANCE;
		return max;		
	}
	
	
}
