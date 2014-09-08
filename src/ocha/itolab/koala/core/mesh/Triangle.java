package ocha.itolab.koala.core.mesh;

import java.util.Vector;

/**
 * Triangle
 */
public class Triangle {
	Vertex vertices[] = new Vertex[3];
	Triangle adjacents[] = new Triangle[3];

	int id;
	

	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setVertices(Vertex v1, Vertex v2, Vertex v3) {
		vertices[0] = v1;   vertices[1] = v2;   vertices[2] = v3;
	}
	
	public Vertex[] getVertices() {
		return vertices;
	}
	
	public void setAdjacents(Triangle r1, Triangle r2, Triangle r3) {
		adjacents[0] = r1;   adjacents[1] = r2;   adjacents[2] = r3;
	}
	
	public Triangle[] getAdjacents() {
		return adjacents;
	}
	

}
