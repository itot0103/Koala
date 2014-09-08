package ocha.itolab.koala.core.data;

import java.util.*;
import ocha.itolab.koala.core.mesh.*;

public class Node {
	int id;
	String description[];
	int colorId;
	double vector[];
	double dissim1[];
	double dissim2[];
	double x, y;
	int connected[];
	int connecting[];
	ArrayList connectedEdge = new ArrayList();
	ArrayList connectingEdge = new ArrayList();
	Vertex vertex = null;
	
	
	public void setPosition(double x, double y) {
		this.x = x;    this.y = y;
	}
	

	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public int getId() {
		return id;
	}
	
	public int getColorId() {
		return colorId;
	}
	
	
	public double getValue(int i) {
		return vector[i];
	}
	
	public double getDisSim1(int i) {
		return dissim1[i];
	}
	
	public double getDisSim2(int i) {
		return dissim2[i];
	}
	
	public int getNumDescription() {
		return description.length;
	}
	
	public String getDescription(int id) {
		return description[id];
	}
	
	
	public int getNumConnectedEdge() {
		return connectedEdge.size();
	}
	
	public Edge getConnectedEdge(int i) {
		return (Edge)connectedEdge.get(i);
	}
	
	public int getNumConnectingEdge() {
		return connectingEdge.size();
	}
	
	public Edge getConnectingEdge(int i) {
		return (Edge)connectingEdge.get(i);
	}
	
	public void setVertex(Vertex v) {
		vertex = v;
	}
	
	public Vertex getVertex() {
		return vertex;
	}
	
}
