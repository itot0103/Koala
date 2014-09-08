package ocha.itolab.koala.datagen.egotwitter;

import java.io.*;
import java.util.*;

public class FeatureProcessor {
	static BufferedReader reader = null;
	static ArrayList<boolean[]> featurelist;
	static ArrayList<String> nodenamelist;
	static ArrayList<String> featurenamelist;
	static ArrayList<ArrayList> clusters;
	static ArrayList<String> clusternames;
	static boolean[] egofeature;
	static String egoname;

	static int MIN_SHARED_TAG = 4;
	static int MIN_CLUSTER_ACCOUNTS = 10;
	static double MIN_TAG_FREQUENCY = 6;
	
	
	/**
	 * Read the series of feature files and define labels of nodes
	 */
	public static void parseFeatureFiles(Graph graph, String path, String ego) {
		featurelist = new ArrayList<boolean[]>();
		nodenamelist = new ArrayList<String>();
		featurenamelist = new ArrayList<String>();
		clusters = new ArrayList<ArrayList>();
		clusternames = new ArrayList<String>();
		egoname = ego;
		
		// Parse the feature of the ego
		parseEgofeatFile(path + ego + ".egofeat");
		parseFeatFile(path + ego + ".feat");
		parseFeatnamesFile(path + ego + ".featnames");
		
		// Analyze the co-occerence of hashtags or retweets
		analyzeCoOccerence(graph);
		
	}
	
	
	/**
	 * Parse the features of the ego
	 */
	static void parseEgofeatFile(String path) {
		reader = EgoTwitterDataGenerator.openReader(path);
		if(reader == null) return;

		try {
			String line = reader.readLine();
			StringTokenizer token = new StringTokenizer(line);
			egofeature = new boolean[token.countTokens()];
			int count = 0;
			while(token.countTokens() > 0) {
				String word = token.nextToken();
				if(word.startsWith("0")) egofeature[count] = false;
				else                     egofeature[count] = true;
				count++;
			}
			
		} catch (Exception e) {
			System.err.println(e);
		}
		
		
		EgoTwitterDataGenerator.closeReader();
		
		nodenamelist.add(egoname);
		featurelist.add(egofeature);
	}
	
	
	/**
	 * Parse the features of the following accounts
	 */
	static void parseFeatFile(String filename) {
		reader = EgoTwitterDataGenerator.openReader(filename);
		if(reader == null) return;

		while(true) {
			try {
				String line = reader.readLine();
				if(line == null) break;
				StringTokenizer token = new StringTokenizer(line);
				boolean[] feature = new boolean[egofeature.length];
				String name = token.nextToken();
				int count = 0;
				while(token.countTokens() > 0) {
					String word = token.nextToken();
					if(word.startsWith("0")) feature[count] = false;
					else                     feature[count] = true;
					count++;
				}
				nodenamelist.add(name);
				featurelist.add(feature);
			}
			
			catch (Exception e) {
				System.err.println(e);
			}
		}
		
		EgoTwitterDataGenerator.closeReader();
	}
	
	
	/**
	 * Parse the feature names
	 */
	static void parseFeatnamesFile(String path) {
		reader = EgoTwitterDataGenerator.openReader(path);
		if(reader == null) return;

		while(true) {
			try {
				String line = reader.readLine();
				if(line == null) break;
				StringTokenizer token = new StringTokenizer(line);
				token.nextToken();
				featurenamelist.add(token.nextToken());
			
			} catch (Exception e) {
				System.err.println(e);
			}
		}
		
		
		EgoTwitterDataGenerator.closeReader();
		
	}
	
	
	static void analyzeCoOccerence(Graph graph) {

		// for each pair of the account
		for(int i = 0; i < featurelist.size(); i++) {
			boolean[] f1 = featurelist.get(i);
			for(int j = (i + 1); j < featurelist.size(); j++) {
				boolean[] f2 = featurelist.get(j);
				
				// count
				int count = 0;
				for(int k = 0; k < egofeature.length; k++) {
					if(f1[k] == true && f2[k] == true)
						count++;
				}
				
				if(count >= MIN_SHARED_TAG) {
					addAccountClusters(i, j);
				}
				
			}
		}
		
		// for each cluster
		for(int i = 0; i < clusters.size(); i++) {
			ArrayList cl = clusters.get(i);

			// remove the small clusters
			if(cl.size() < MIN_CLUSTER_ACCOUNTS) {
				clusters.remove(cl);
				i--;  continue;
			}
			
			// Assign clusterId to the nodes
			for(int j = 0; j < cl.size(); j++) {
				int[] featureId = (int[])cl.get(j);
				String nodename = nodenamelist.get(featureId[0]);
				Node node = (Node)graph.nodemap.get(nodename);
				node.clusterId = i;
			}
			
			// Find frequent tags 
			String clname = "";
			int nameCount = 0;
			for(int j = 0; j < featurenamelist.size(); j++) {
				int accountCount = 0;
				for(int k = 0; k < cl.size(); k++) {
					int[] nodeId = (int[])cl.get(k);
					boolean[] feature = featurelist.get(nodeId[0]);
					if(feature[j] == true) accountCount++;
					if(accountCount >= MIN_TAG_FREQUENCY) break;
				}
				if(accountCount >= MIN_TAG_FREQUENCY) {
					String name = featurenamelist.get(j);
					clname += (name + ",");
					nameCount++;
					if(nameCount >= 200) break;
				}
			}
			
			System.out.println("   cluster.size=" + cl.size() + " name=" + clname);
			
		}
		
		
		// allocate nodes which are not in the edge file
		for(String name : nodenamelist) {
			Node n1 = (Node)graph.nodemap.get(name);
			if(n1 == null)
				graph.addOneNode(name);
		}
		
		
		// for each node
		for(Node node : graph.nodelist) {
			node.dissim = new double[graph.nodelist.size()];
			for(int i = 0; i < node.dissim.length; i++)
				node.dissim[i] = 1.0;
		}
		
			
		// for each pair of features
		for(int i = 0; i < featurelist.size(); i++) {
			boolean[] feature1 = featurelist.get(i);
			Node n1 = (Node)graph.nodemap.get(nodenamelist.get(i));
			if(n1 == null) continue;
			
			for(int j = 0; j < i; j++) {
				boolean[] feature2 = featurelist.get(j);
				Node n2 = (Node)graph.nodemap.get(nodenamelist.get(j));
				if(n2 == null) continue;
				
				int count = 0;
				for(int k = 0; k < feature1.length; k++) {
					if(feature1[k] == true && feature2[k] == true)
						count++;
				}

				double dis = 1.0 / (double)(1 + count);
				n1.dissim[j] = n2.dissim[i] = dis;
			}
		}
		
		
	}
	
	
	static void addAccountClusters(int id1, int id2) {
		ArrayList cluster1 = null, cluster2 = null;
		
		// for each cluster
		for(ArrayList cl : clusters) {
			for(int i = 0; i < cl.size(); i++) {
				int id[] = (int[])cl.get(i);
				if(id[0] == id1) {
					cluster1 = cl;  break;
				}
				if(id[0] == id2) {
					cluster2 = cl;  break;
				}
			}
		}
		
		// if either of the clusters is null
		if(cluster1 != null && cluster2 == null) {
			int id[] = new int[1];
			id[0] = id2;
			cluster1.add(id);
		}
		if(cluster1 == null && cluster2 != null) {
			int id[] = new int[1];
			id[0] = id1;
			cluster2.add(id);
		}
		
		// if both of clusters are null
		if(cluster1 == null && cluster2 == null) {
			ArrayList newcl = new ArrayList();
			int id11[] = new int[1];
			id11[0] = id1;
			int id22[] = new int[1];
			id22[0] = id2;
			newcl.add(id11);
			newcl.add(id22);
			clusters.add(newcl);
		}
		
	}
	
	
}
