package ocha.itolab.koala.core.data;

public class NodeDistanceCalculator {

	static double placeRatio = 0.5, clusteringRatio = 0.5;
	
	
	
	public static void setPlacementRatio(double r) {
		placeRatio = r;
	}
	
	public static void setClusteringRatio(double r) {
		clusteringRatio = r;
	}
		
	
	public static double calcClusteringDistance(Graph g, Node n1, Node n2) {
		double ret = 0.0, ret1 = 0.0;
		
		if(g.attributeType == g.ATTRIBUTE_VECTOR) {
			double d1 = 0.0, d2 = 0.0;		
			for(int i = 0; i < g.vectorname.length; i++) {
				ret1 += (n1.vector[i] * n2.vector[i]);
				d1 += (n1.vector[i] * n1.vector[i]);
				d2 += (n2.vector[i] * n2.vector[i]);
			}
			if(ret1 < 0.0) ret1 = 0.0;
			else {
				double d12 = Math.sqrt(d1) * Math.sqrt(d2);
				if(d12 < 1.0e-8) ret1 = 0.0;
				else ret1 /= d12;
			}
			ret1 = 1.0 - ret1;
		}
		else {
			int id2 = n2.getId();
			ret1 = n1.getDisSim1(id2);
		}
		
		
		double ret2 = 0.0;
		int count = 0;
		int num1 = n1.connected.length + n1.connecting.length;
		int num2 = n2.connected.length + n2.connecting.length;
		int id1 = 0, id2 = 0;
		
		for(int i = 0; i < num1; i++) {
			if(i < n1.connected.length)
				id1 = n1.connected[i];
			else
				id1 = n1.connecting[i - n1.connected.length];
			
			for(int j = 0; j < num2; j++) {
				if(j < n2.connected.length)
					id2 = n2.connected[j];
				else
					id2 = n2.connecting[j - n2.connected.length];
				if(id1 == id2) {
					count++;  break;
				}
			}
		}
		/*
		int num = (num1 > num2) ? num1 : num2;
		if(num <= 0)
			ret2 = 1.0;
		else
			ret2 = (double)(num - count) / (double)num;
		*/
		int num = num1 + num2;
		if(num <= 0)
			ret2 = 1.0;
		else
			ret2 = (double)(num - count * 2) / (double)num;
		
		ret = clusteringRatio * ret1 + (1.0 - clusteringRatio) * ret2;
		
		return ret;
	}
	
	
	public static double calcPlacementDistance(Graph g, Node n1, Node n2) {
		double ret = 0.0, ret1 = 0.0;
		
		if(g.attributeType == g.ATTRIBUTE_VECTOR) {
			double d1 = 0.0, d2 = 0.0;		
			for(int i = 0; i < g.vectorname.length; i++) {
				ret1 += (n1.vector[i] * n2.vector[i]);
				d1 += (n1.vector[i] * n1.vector[i]);
				d2 += (n2.vector[i] * n2.vector[i]);
			}
			if(ret1 < 0.0) ret1 = 0.0;
			else
				ret1 /= (Math.sqrt(d1) * Math.sqrt(d2));
			ret1 = 1.0 - ret1;
		}
		else {
			int id2 = n2.getId();
			ret1 = n1.getDisSim1(id2);
		}
			
		boolean isConnected = g.isTwoNodeConnected(n1, n2);
		double ret2 = (isConnected == true) ? 0.0 : 1.0;
		
		ret = placeRatio * ret1 + (1.0 - placeRatio) * ret2;
		
		return ret;
	}
	
	
}
