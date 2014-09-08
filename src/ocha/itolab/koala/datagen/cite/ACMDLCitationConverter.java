package ocha.itolab.koala.datagen.cite;

import java.io.*;
import java.util.*;

public class ACMDLCitationConverter {
	static String path = "C:/itot/projects/FRUITSNet/Leena/cite/data/";
	static String idfile = "id.txt";
	static String thetafile = "theta.txt";
	static String edgefile = "edge.txt";
	static String titlefile = "node0605.txt";
	static String outfile1 = "papers.csv";
	static String outfile2 = "distances.csv";
	static int NUMTOPIC = 10;
	
	
	class Paper {
		int id;
		int orgid;
		String year;
		double theta[];
		ArrayList cited, citing;
		String title;
		
		public Paper() {
			theta = new double[NUMTOPIC];
			cited = new ArrayList();
			citing = new ArrayList();
		}
	}
	
	class Edge {
		int id;
		Paper p1, p2;
	}
	
	
	static HashMap papers;
	static ArrayList<Paper> paperlist;
	static ArrayList<Edge> edges;
	
	
	
	public static void main(String args[]) {
		papers = new HashMap();
		paperlist = new ArrayList<Paper>();
		edges = new ArrayList<Edge>();
		
		ACMDLCitationConverter c = new ACMDLCitationConverter();
		
		c.readIdFile();
		c.readThetaFile();
		c.readTitleFile();
		c.readEdgeFile();
		c.writePapers();
		//c.writeDistances();
	}
	
	
	
	static BufferedReader reader;
	static BufferedWriter writer;
	
	void readIdFile() {
		System.out.println("readIdFile");
		
		openReader(path + idfile);

		int counter = 0;
		try {
			while(true) {
				String line = reader.readLine();
				if (line == null) return;
				if (line.length() <= 0) continue;
			
				Paper p = new Paper();
				p.id = counter;
				
				counter++;
				
				StringTokenizer token = new StringTokenizer(line);
				String org = token.nextToken().replaceAll("_info", "");
				p.orgid = Integer.parseInt(org);
				papers.put(org, p);
				paperlist.add(p);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		closeReader();
	}
	
	
	void readThetaFile() {
		System.out.println("readThetaFile");
		
		openReader(path + thetafile);
		Paper p = null;
		
		int counter = 0, counter2 = 0;
		try {
			while(true) {
				String line = reader.readLine();
				if (line == null) return;
				if (line.length() <= 0) continue;
				if (line.startsWith("corpus") == true) {
					p = (Paper)paperlist.get(counter++);
					counter2 = 0;
				}
				else {
					p.theta[counter2++] = Double.parseDouble(line);
				}
				
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		closeReader();
	}
	
	
	
	void readTitleFile() {
		System.out.println("readTitleFile");
		
		openReader(path + titlefile);
		Paper p = null;
		
		int counter = 0, counter2 = 0;
		String line = "";
		try {
			while(true) {
				line = reader.readLine();
				if (line == null) return;
				if (line.length() <= 0) continue;
				
				StringTokenizer token1 = new StringTokenizer(line);
				String id = token1.nextToken();
				StringTokenizer token2 = new StringTokenizer(id, ".");
				id = token2.nextToken();
				id = token2.nextToken();
				
				token1.nextToken();
				String year = token1.nextToken();
				token1.nextToken();
				
				String title = "";
				while(token1.countTokens() > 0) {
					title += token1.nextToken();
					title += " ";
				}
				title = title.replace(",", "");
				
				Paper p1 = (Paper)papers.get(id);
				if(p1 != null) {
					p1.title = title;
					p1.year = year;
				}
			}
			
		} catch(Exception e) {
			System.out.println("???? line=" + line);
			e.printStackTrace();
		}
		
		closeReader();
	}
	
	
	
	void readEdgeFile() {
		System.out.println("readEdgeFile");
		
		openReader(path + edgefile);
	
		try {
			while(true) {
				String line = reader.readLine();
				if (line == null) return;
				if (line.length() <= 0) continue;
				StringTokenizer token0 = new StringTokenizer(line);
				String pname1 = token0.nextToken();
				String pname2 = token0.nextToken();
				StringTokenizer token1 = new StringTokenizer(pname1, ".");
				StringTokenizer token2 = new StringTokenizer(pname2, ".");
				if(token1.countTokens() < 2 || token2.countTokens() < 2)
					continue;
				String pn1 = token1.nextToken();
				pn1 = token1.nextToken();
				String pn2 = token2.nextToken();
				pn2 = token2.nextToken();
				
				Paper p1 = (Paper)papers.get(pn1);
				Paper p2 = (Paper)papers.get(pn2);
				if(p1 == null || p2 == null) continue;
				Edge e = new Edge();
				e.id = edges.size();
				e.p1 = p1;
				e.p2 = p2;
				p1.cited.add(p2);
				p2.citing.add(p1);
				edges.add(e);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		closeReader();
	}
	
	
	
	void writePapers() {
		
		openWriter(path + outfile1);
		
		try {
			
			// Connectivity 
			String line = "#connectivity";
			writeOneLine(line);
			for(Paper p: paperlist) {
				line = p.id + "," + p.year + "," + p.title + "," + p.orgid;
				writeOneLine(line);
				line = "";
				for(int j = 0; j < p.cited.size(); j++) {
					Paper p2 = (Paper)p.cited.get(j);
					line += ("," + p2.id);
				}
				writeOneLine(line);
				line = "";
				for(int j = 0; j < p.citing.size(); j++) {
					Paper p2 = (Paper)p.citing.get(j);
					line += ("," + p2.id);
				}
				writeOneLine(line);
			}

			// Vector
			line = "#vector";
			for(int i = 0; i < NUMTOPIC; i++) 
				line += (",TOPIC" + (i + 1));
			writeOneLine(line);
			for(Paper p: paperlist) {
				line = Integer.toString(p.id);
				for(int j = 0; j < NUMTOPIC; j++) {
					line += ("," + p.theta[j]);
				}
				writeOneLine(line);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		closeWriter();
	}
	
	
	
	void writeDistances() {
		
		openWriter(path + outfile2);
	
		try {
			for(int i = 0; i < paperlist.size(); i++) {
				Paper p = (Paper)paperlist.get(i);
				for(int j = (i + 1); j < paperlist.size(); j++) {
					Paper p2 = (Paper)paperlist.get(j);
					double d = calcDistance(p, p2);
					String line = p.id + "," + p2.id + "," + d;
					writeOneLine(line);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		closeWriter();
	}
	
	
	double calcDistance(Paper p, Paper p2) {
		double ret = 0.0;
	
		for(int i = 0; i < NUMTOPIC; i++) {
			ret += (p.theta[i] - p2.theta[i]) * (p.theta[i] - p2.theta[i]);
		}
		
		int count = 0;
		for(int i = 0; i < p.cited.size(); i++) {
			Paper p1c = (Paper)p.cited.get(i);
			for(int j = 0; j < p2.cited.size(); j++) {
				Paper p2c = (Paper)p2.cited.get(j);
				if(p1c == p2c) {
					count++;  break;
				}
			}
		}
		for(int i = 0; i < p.citing.size(); i++) {
			Paper p1c = (Paper)p.citing.get(i);
			for(int j = 0; j < p2.citing.size(); j++) {
				Paper p2c = (Paper)p2.citing.get(j);
				if(p1c == p2c) {
					count++;  break;
				}
			}
		}	
		if(count > 0) {
			ret /= (double)count;
		}
		
		return ret;
	}
	
	
	
	
	/**
	 * ファイルを開く
	 */
	static void openReader(String filename) {
		
		System.out.println(filename);
		try {
			reader = new BufferedReader(new FileReader(new File(filename)));
			reader.ready();
		} catch (Exception e) {
			System.err.println(e);
		}
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
	 * 書き込みファイルを開く 
	 */
	public void openWriter(String filename) {
		try {
			writer = new BufferedWriter(new FileWriter(new File(filename)));
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	
	
	/**
	 * 書き込みファイルを閉じる
	 */
	public void closeWriter() {
		try {
			writer.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	
	/**
	 * 1行を書き込む
	 */
	public void writeOneLine(String line) {
		try {
			writer.write(line, 0, line.length());
			writer.flush();
			writer.newLine();
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	
}
