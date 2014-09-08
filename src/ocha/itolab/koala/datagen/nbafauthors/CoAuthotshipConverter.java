package ocha.itolab.koala.datagen.nbafauthors;

import java.io.*;
import java.util.*;


public class CoAuthotshipConverter {
	static String path = "C:/itot/projects/FRUITSNet/Koala/data/";
	static String infilename = "NBAF_Bibliography_Plain_Curated_Part_ANSI_importresult_importresult_FMMM.gml";
	static String outfilename = "NBAF_Coauthorship.csv";
	
	static BufferedWriter writer;
	static BufferedReader reader;
	static ArrayList<Node> nodelist = new ArrayList<Node>();
	static ArrayList<Edge> edgelist = new ArrayList<Edge>();
	static ArrayList<String> titlelist = new ArrayList<String>();
	static Node node;
	static Edge edge;
	
	static String keywords[] = {
		//"anaerobic",
		//"thermal",
		//"transcriptome",
		"genetic",
		"molecular",
		"loci",
		"microsatellites",
		"isolation",
		"inbreeding",
		//"male-killing",
		"transcriptomics",
		"expression",
		"bacterial",
		"breeding",
		"polymorphic",
	};
	
	
	static class Node {
		int id;
		String name;
		ArrayList<Node> connecting = new ArrayList<Node>();
		ArrayList<Node> connected = new ArrayList<Node>();
		ArrayList<String> titlelist = new ArrayList<String>();
		double vector[];
	}
	
	static class Edge {
		int id;
		Node n1, n2;
		String name;
	}
	

	public static void main(String args[]) {
		readGmlFile();
		//analyzeTitle();
		calculateAuthorVector();
		writeCsvFile();
	}
	
	
	static void readGmlFile() {
		openReader(path + infilename);
		
		boolean isNode = true;
		
		while(true) {
			try {
				String line = reader.readLine();
				if(line == null) break;
				StringTokenizer token = new StringTokenizer(line);
				String tag = token.nextToken();
				if(tag.startsWith("node")) {
					node = new Node();
					node.id = nodelist.size();
					nodelist.add(node);
					isNode = true;
				}
				if(tag.startsWith("edge")) {
					edge = new Edge();
					edge.id = edgelist.size();
					edgelist.add(edge);
					isNode = false;
				}
				if(tag.startsWith("source")) {
					int nodeId = Integer.parseInt(token.nextToken());
					edge.n1 = nodelist.get(nodeId);
				}
				if(tag.startsWith("target")) {
					int nodeId = Integer.parseInt(token.nextToken());
					edge.n2 = nodelist.get(nodeId);
					edge.n2.connected.add(edge.n1);
					edge.n1.connecting.add(edge.n2);
				}
				if(tag.startsWith("label") && isNode == true) {
					node.name = "";
					while(token.countTokens() > 0) {
						node.name += (token.nextToken() + " ");
					}
					node.name = node.name.replace("\"", "");
					node.name = node.name.replace(",", " ");
				}
				if(tag.startsWith("label") && isNode == false) {
					edge.name = "";
					while(token.countTokens() > 0) {
						edge.name += (token.nextToken() + " ");
					}
					edge.name = edge.name.replace("\"", "");
					edge.name = edge.name.replace(")", "");
					edge.name = edge.name.replace("(", "");
					edge.name = edge.name.replace(",", " ");
					edge.name = edge.name.toLowerCase();
					
					boolean isTitleExist = false;
					for(String title : titlelist) {
						if(edge.name.startsWith(title) == true) {
							isTitleExist = true;  break;
						}
					}
					if(isTitleExist == false)
						titlelist.add(edge.name);
					
					isTitleExist = false;
					for(String title : edge.n1.titlelist) {
						if(edge.name.startsWith(title) == true) {
							isTitleExist = true;  break;
						}
					}
					if(isTitleExist == false)
						edge.n1.titlelist.add(edge.name);
					
					isTitleExist = false;
					for(String title : edge.n2.titlelist) {
						if(edge.name.startsWith(title) == true) {
							isTitleExist = true;  break;
						}
					}
					if(isTitleExist == false)
						edge.n2.titlelist.add(edge.name);
					
				}
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		closeReader();
	}
	
	static void writeCsvFile() {
		openWriter(path + outfilename);
		
		try {
			
			// Connectivity
			String line = "#connectivity";
			println(line);
			for(Node n: nodelist) {
				line = n.id + "," + n.name;
				println(line);
				line = "";
				for(int j = 0; j < n.connected.size(); j++) {
					Node n2 = (Node)n.connected.get(j);
					line += ("," + n2.id);
				}
				println(line);
				line = "";
				for(int j = 0; j < n.connecting.size(); j++) {
					Node n2 = (Node)n.connecting.get(j);
					line += ("," + n2.id);
				}
				println(line);
			}

			// Vector
			line = "#vector";
			for(String key : keywords)
				line += ("," + key);
			println(line);
			for(Node n : nodelist) {
				line = Integer.toString(n.id);
				for(int j = 0; j < keywords.length; j++) {
					line += ("," + n.vector[j]);
				}
				println(line);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		closeWriter();
	}
	
	
	static void analyzeTitle() {
		ArrayList<String> wordlist = new ArrayList<String>();
		
		for(String title : titlelist) {
			StringTokenizer token = new StringTokenizer(title);
			while(token.countTokens() > 0) {
				String word = token.nextToken();
				
				boolean isWordExist = false;
				for(String w : wordlist) {
					if(w.startsWith(word) == true) {
						isWordExist = true;  break;
					}
				}
				if(isWordExist == false)
					wordlist.add(word);
			}
		}
		
		int count[] = new int[wordlist.size()];
		for(String title : titlelist) {
			StringTokenizer token = new StringTokenizer(title);
			while(token.countTokens() > 0) {
				String word = token.nextToken();
				
				for(int i = 0; i < wordlist.size(); i++) {
					String w = wordlist.get(i);
					if(w.startsWith(word) == true) {
						count[i]++;   break;
					}
				}
			}
		}
		
		for(int i = 0; i < wordlist.size(); i++) {
			String w = wordlist.get(i);
			if(count[i] >= 10)
			System.out.println("     " + w + ": count=" + count[i]);
		}
		
	}
	
	
	static void calculateAuthorVector() {		
		int count[][] = new int[nodelist.size()][];
		for(int i = 0; i < count.length; i++)
			count[i] = new int[keywords.length];
		
		for(int i = 0; i < nodelist.size(); i++) {
			Node node = nodelist.get(i);
			for(String title : node.titlelist) {
				
				StringTokenizer token = new StringTokenizer(title);
				while(token.countTokens() > 0) {
					String word = token.nextToken();
					for(int j = 0; j < keywords.length; j++) {
						if(word.startsWith(keywords[j]) == true)
							count[i][j]++;
					}
				}
				
			}
		}
		
		int maxcount[] = new int[keywords.length];
		for(int j = 0; j < keywords.length; j++) {
			for(int i = 0; i < count.length; i++) {
				if(maxcount[j] < count[i][j])
					maxcount[j] = count[i][j];
			}
		}
		
		for(int i = 0; i < nodelist.size(); i++) {
			Node node = nodelist.get(i);
			node.vector = new double[keywords.length];
			for(int j = 0; j < keywords.length; j++) {
				if(maxcount[j] <= 0)
					node.vector[j] = 0.0;
				else
					node.vector[j] = (double)count[i][j] / (double)maxcount[j];
			}
		}
		
		
		int keycount[] = new int[keywords.length];
		for(int i = 0; i < nodelist.size(); i++) {
			Node node = nodelist.get(i);
			double max = 0.0;
			int maxid = -1;
			for(int j = 0; j < keywords.length; j++) {
				if(max < node.vector[j]) {
					max = node.vector[j];   maxid = j;
				}
			}
			if(maxid >= 0)
				keycount[maxid]++;
		}
		for(int j = 0; j < keywords.length; j++) 
			System.out.print(keycount[j] + ",");
		System.out.println("");
		
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
