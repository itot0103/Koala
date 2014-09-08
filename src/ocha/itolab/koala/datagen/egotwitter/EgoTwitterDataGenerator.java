package ocha.itolab.koala.datagen.egotwitter;

import java.util.*;
import java.io.*;

public class EgoTwitterDataGenerator {
	
	static String[] egolist = {
		"12831"
	};
	static String path = "C:/itot/projects/FRUITSNet/Koala/data/ego-twitter/";
	
	static BufferedReader reader = null;
	static BufferedWriter writer = null;
	
	static ArrayList<Graph> graphlist = new ArrayList<Graph>();
	//static Graph graph = null;
	//static Graph allgraph = null;
	
	
	
	
	/**
	 * Main method
	 */
	public static void main(String args[]) {

		// for each ego
		for(String ego : egolist) {
			parseOneEgo(ego);
		}
		
		// Write the parsed data
		Graph allgraph = integrate();
		write(allgraph);
	}
	

	/**
	 * Parse one ego
	 */
	static void parseOneEgo(String ego) {
		Graph graph = new Graph();
		graphlist.add(graph);
		
		graph.addOneNode(ego);
		String edgefilename = path + ego + ".edges";
		parseEdgesFile(edgefilename, graph);
		FeatureProcessor.parseFeatureFiles(graph, path, ego);
	}
	
	/**
	 * Parse edges file
	 */
	static void parseEdgesFile(String filename, Graph graph) {
		
		reader = EgoTwitterDataGenerator.openReader(filename);
		if(reader == null) return;
		
		// for each line
		while(true) {
			try {
				String line = reader.readLine();
				if(line == null) break;
				StringTokenizer token = new StringTokenizer(line);
				String nodename1 = token.nextToken();
				String nodename2 = token.nextToken();
				Node n1 = (Node)graph.nodemap.get(nodename1);
				Node n2 = (Node)graph.nodemap.get(nodename2);
				if(n1 == null) n1 = graph.addOneNode(nodename1);
				if(n2 == null) n2 = graph.addOneNode(nodename2);
				graph.addOneEdge(n1, n2);
				n1.following.add(n2);
				n2.followed.add(n1);
			} catch (Exception e) {
				System.err.println(e);
			}
		}
		
		EgoTwitterDataGenerator.closeReader();
		
	}

	
	/**
	 * Integrate all the graphs into one
	 */
	static Graph integrate() {
		Graph allgraph = new Graph();

		// total num of nodes
		int allnumnode = 0;
		for(Graph graph : graphlist) 
			allnumnode += graph.nodelist.size();
	
		
		// for each graph
		int sumnode = 0;
		for(Graph graph : graphlist) {
			
			// for each node
			for(int i = 0; i < graph.nodelist.size(); i++) {
				Node node = graph.nodelist.get(i);
				node.id = allgraph.nodelist.size();
				allgraph.nodelist.add(node);
				double dcopy[] = new double[node.dissim.length];
				for(int j = 0; j < dcopy.length; j++)
					dcopy[j] = node.dissim[j];
				
				node.dissim = new double[allnumnode];
				for(int j = 0; j < allnumnode; j++) {
					if(j >= sumnode && j < (sumnode + graph.nodelist.size())) {
						node.dissim[j] = dcopy[j - sumnode];
					}
					else
						node.dissim[j] = 1.0;
				}
				
			}
			sumnode += graph.nodelist.size();
			
			// for each edge
			for(Edge edge : graph.edgelist) {
				edge.id = allgraph.edgelist.size();
				allgraph.edgelist.add(edge);
			}
		}
		
		System.out.println("integrate 3");
		
		graphlist = null;
		return allgraph;
	}
	
	
	
	/**
	 * Write the parsed data
	 */
	static void write(Graph allgraph) {
		System.out.println("write");
		openWriter(path + "ego-twitter.csv");
		println("#connectivity");
		
		for(Node node : allgraph.nodelist) {
			String line = node.id + "," + node.name;
			println(line);
			line = "";
			for(int i = 0; i < node.following.size(); i++) {
				Node node2 = (Node)node.following.get(i);
				line += ("," + node2.id);
			}
			println(line);
			line = "";
			for(int i = 0; i < node.followed.size(); i++) {
				Node node2 = (Node)node.followed.get(i);
				line += ("," + node2.id);
			}
			println(line);
		}
		
		println("#dissimilarity");
		for(Node node : allgraph.nodelist) {
			String line = node.id + "," + node.clusterId;
			for(int i = 0; i < node.dissim.length; i++)
				line += ("," + node.dissim[i]);
			println(line);
		}
		
		closeWriter();
	}
	
	
	
	/**
	 * ファイルを開く
	 */
	static BufferedReader openReader(String filename) {
		
		System.out.println(filename);
		try {
			reader = new BufferedReader(new FileReader(new File(filename)));
			reader.ready();
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
		
		return reader;
	}
	
	/**
	 * ファイルを閉じる
	 */
	static void closeReader() {
		try {
			reader.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	/**
	 * ファイルを開く
	 */
	static BufferedWriter openWriter(String filename) {	
		try {
			 writer = new BufferedWriter(
			    		new FileWriter(new File(filename)));
		} catch (Exception e) {
			System.err.println(e);
			writer = null;
			return null;
		}
		return writer;
	}
	
	/**
	 * ファイルを閉じる
	 */
	static void closeWriter() {
		if(writer == null) return;
		
		try {
			writer.close();
		} catch (Exception e) {
			System.err.println(e);
			return;
		}
	}
	
	/**
	 * 改行つきで出力する
	 */
	static void println(String word) {
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
