package ocha.itolab.koala.datagen.egotwitter;

import java.util.ArrayList;

public class Node {
	int id = -1;
	String name = "";
	int clusterId = -1;
	double dissim[];
	ArrayList<Node> following = new ArrayList<Node>();
	ArrayList<Node> followed = new ArrayList<Node>();
}
