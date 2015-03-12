package ocha.itolab.koala.core.forcedirected;

import java.io.*;
import java.util.*;

public class GmlFileReader {
	static BufferedReader breader = null;
	
	public static void read(Vector edgelist, Vector nodelist, String filename) {
		open(filename);
		if(breader == null) return;
		
		edgelist.clear();
		nodelist.clear();
		
		String line = null;
		OutputNode on = null;
		InputEdge ie = null;
		Vector tmpnodelist = new Vector();
		
		while(true) {
			try {
				
				line = breader.readLine();
				if(line == null) break;
				
				if(line.indexOf("node") >= 0) {
					on = new OutputNode();
					on.id = nodelist.size();
					tmpnodelist.add(on);
				}
				
				if(line.indexOf("edge") >= 0) {
					ie = new InputEdge();
					edgelist.add(ie);
				}
				
				StringTokenizer token = new StringTokenizer(line);
				if(token.countTokens() >= 2) {
					String first = token.nextToken();
					if(first.compareTo("x") == 0) {
						on.x = Double.parseDouble(token.nextToken());
					}
					else if(first.compareTo("y") == 0) {
						on.y = Double.parseDouble(token.nextToken());
					}
					else if(first.compareTo("label") == 0) {
						if(ie == null) {
							String label = token.nextToken().replace("\"", "");
							on.label = Integer.parseInt(label);
						}
					}
					else if(first.compareTo("source") == 0) {
						ie.node1 = Integer.parseInt(token.nextToken());
					}
					else if(first.compareTo("target") == 0) {
						ie.node2 = Integer.parseInt(token.nextToken());
					}
					else if(first.compareTo("weight") == 0) {
						if(ie != null)
						ie.weight = Double.parseDouble(token.nextToken());
					}
				}
			
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println(" ????? line=" + line);
			}
		}
		
		while(tmpnodelist.size() > 0) {
			boolean isFound = false;
			for(int i = 0; i < tmpnodelist.size(); i++) {
				OutputNode on2 = (OutputNode)tmpnodelist.get(i);
				if(on2.label == nodelist.size()) {
					on2.id = on2.label;
					nodelist.add(on2);
					tmpnodelist.remove(on2);
					isFound = true;
					break;
				}
			}
			if(isFound == false) {
				OutputNode on2 = new OutputNode();
				on2.id = on2.label = nodelist.size();
				nodelist.add(on2);
			}
		}
		
		
		close();
	}
	

	/**
	 * ファイルを開く
	 */
	static void open(String filename) {
		try {
			File file = new File(filename);
			breader = new BufferedReader(new FileReader(file));
			breader.ready();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	

	/**
	 * ファイルを閉じる
	 */
	static void close() {
		try {
			breader.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
}
