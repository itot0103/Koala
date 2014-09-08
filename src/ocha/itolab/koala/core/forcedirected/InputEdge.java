package ocha.itolab.koala.core.forcedirected;

public class InputEdge {
	public int node1;
	public int node2;
	public double weight;
	
	public InputEdge() {
		node1 = node2 = -1;
		weight = 1.0;
	}
	
}
