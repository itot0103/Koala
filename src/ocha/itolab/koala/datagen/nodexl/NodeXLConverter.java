package ocha.itolab.koala.datagen.nodexl;

import java.io.*;
import java.util.*;


public class NodeXLConverter {

	static String path = "C:/itot/projects/FRUITSNet/Koala/data/";
	static String nodefilename = "NodeXLWorkbook-28286-nodes.csv";
	static String edgefilename = "NodeXLWorkbook-28286-edges.csv";
	static String outfilename = "NodeXLWorkbook-28286-koala.csv";
	
	static String keywords[] = {
		"somalia",
		"africa",
		"ebola",
		"india",
		"mubasher_lucman",
		"pti",
		"amp",
	};
	
	static BufferedWriter writer;
	static BufferedReader reader;

	static ArrayList<Node> nodelist = new ArrayList<Node>();
	static ArrayList<Edge> edgelist = new ArrayList<Edge>();
	static HashMap nodemap = new HashMap();
	static HashMap edgemap = new HashMap();
	
	static class Node {
		int id;
		String name;
		ArrayList<Node> connecting = new ArrayList<Node>();
		ArrayList<Node> connected = new ArrayList<Node>();
		ArrayList<String> wordlist = new ArrayList<String>();
		double vector[];
	}
	
	static class Edge {
		int id;
		Node n1, n2;
	}
	

	public static void main(String args[]) {
		readNodeFile();
		readEdgeFile();
		System.out.println("   " + nodelist.size() + " nodes, " + edgelist.size() + " edges.");
		analyzeWord();
		writeKoalaFile();	
	}
	

	static void readNodeFile() {
		int numline = 0;
		openReader(path + nodefilename);
		
		while(true) {
			try {
				String line = reader.readLine();
				if(line == null) break;
				numline++;
				if(numline <= 2) continue;
				StringTokenizer token = new StringTokenizer(line, ",", true);
				Node node = new Node();
				node.id = nodelist.size();
				node.name = token.nextToken();
				
				int skipcount = 0;
				while(skipcount < 47) {
					String w = token.nextToken();
					if(w.compareTo(",") == 0) skipcount++;
				}
				String w = token.nextToken();
				if(w.compareTo(",") != 0) {
					addNodeWord(node, w);
					token.nextToken();
				}
				token.nextToken();
				w = token.nextToken();
				if(w.compareTo(",") != 0) {
					addNodeWord(node, w);
					token.nextToken();
				}	
	
				node.vector = new double[keywords.length];
				for(int j = 0; j < keywords.length; j++) {
					for(int k = 0; k < node.wordlist.size(); k++) {
						String ww = node.wordlist.get(k);
						if(ww.startsWith(keywords[j]) == true) {
							node.vector[j] = 1.0;  break;
						}
					}
				}
				
				nodelist.add(node);
				nodemap.put(node.name, node);
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
				
		closeReader();
	}
	

	static void readEdgeFile() {
		openReader(path + edgefilename);
		
		while(true) {
			try {
				String line = reader.readLine();
				if(line == null) break;
				StringTokenizer token = new StringTokenizer(line, ",");
				Node n1 = (Node)nodemap.get(token.nextToken());
				Node n2 = (Node)nodemap.get(token.nextToken());
				
				if(n1 == null || n2 == null) continue;
				if(n1 == n2) continue;
				String ename = n1.name + "---" + n2.name;
				if(edgemap.get(ename) != null) continue;
				ename = n2.name + "---" + n1.name;
				if(edgemap.get(ename) != null) continue;
				
				Edge edge = new Edge();
				edge.id = edgelist.size();
				edge.n1 = n1;
				edge.n2 = n2;
				edge.n2.connected.add(edge.n1);
				edge.n1.connecting.add(edge.n2);
				edgelist.add(edge);
				ename = n1.name + "---" + n2.name;
				edgemap.put(ename, edge);

			} catch(Exception e) {
				e.printStackTrace();
			}
		}
				
		closeReader();
	}

	
	static void analyzeWord() {
		ArrayList<String> wordlist = new ArrayList<String>();
		
		for(Node node : nodelist) {
			for(String w : node.wordlist) {
				
				boolean isWordExist = false;
				for(String w2 : wordlist) {
					if(w2.startsWith(w) == true) {
						isWordExist = true;  break;
					}
				}
				if(isWordExist == false)
					wordlist.add(w);
			}
		}
		
		int count[] = new int[wordlist.size()];
		for(Node node : nodelist) {
			for(String w : node.wordlist) {
				for(int i = 0; i < wordlist.size(); i++) {
					String w2 = wordlist.get(i);
					if(w.startsWith(w2) == true) {
						count[i]++;   break;
					}
				}
			}
		}
		
		for(int i = 0; i < wordlist.size(); i++) {
			String w = wordlist.get(i);
			if(count[i] >= 60)
			System.out.println("     " + w + ": count=" + count[i]);
		}
		
	}
	
	

	static void writeKoalaFile() {
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
					if(n.vector != null)
						line += ("," + n.vector[j]);
					else
						line += ",0";
				}
				println(line);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		closeWriter();
	}
	
	
	static void addNodeWord(Node node, String words) {
		StringTokenizer token1 = new StringTokenizer(words);
		while(token1.countTokens() > 0) {
			String words2 = token1.nextToken();
			StringTokenizer token2 = new StringTokenizer(words2, ";");
			while(token2.countTokens() > 0) {
				String w = token2.nextToken();

				boolean isExist = false;
				for(int i = 0; i < node.wordlist.size(); i++) {
					String w2 = node.wordlist.get(i);
					if(w.compareTo(w2) == 0) {
						isExist = true;  break;
					}
				}
				if(isExist == false) {
					//System.out.println("   node" + node.id + " word=" + w);
					node.wordlist.add(w);
				}
			}
		}
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
