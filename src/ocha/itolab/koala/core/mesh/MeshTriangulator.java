package ocha.itolab.koala.core.mesh;

import java.util.ArrayList;

public class MeshTriangulator {
	static Mesh mesh;
	static ArrayList swapStack;
	
	
	/**
	 * Apply Delaunay triangulation
	 */
	public static void triangulate(Mesh m) {
		mesh = m;
		mesh.triangles.clear();
		swapStack = new ArrayList();

		int numv = mesh.getNumVertices();
		calcMinMaxPosition();
		
		// 皮膚領域全体を囲む大きな四角形を生成する
		generateSuperRectangle();
		// 頂点を1個ずつ処理し、Delaunay三角メッシュ生成アルゴリズムを適用する
		for(int i = 0; i < numv; i++) {
			Vertex v = mesh.getVertex(i);
			placeOneVertex(v);
		}
		
		// 領域の外側にある皮丘を削除する
		removeOuterTriangle();
		
	}
	

	
	/**
	 * 領域の四隅の座標値を求める
	 */
	static void calcMinMaxPosition() {
		for(int i = 0; i < mesh.getNumVertices(); i++) {
			Vertex v = mesh.getVertex(i);
			double pos[] = v.getPosition();
			mesh.minx = (pos[0] < mesh.minx) ? pos[0] : mesh.minx;
			mesh.miny = (pos[1] < mesh.miny) ? pos[1] : mesh.miny;
			mesh.maxx = (pos[0] > mesh.maxx) ? pos[0] : mesh.maxx;
			mesh.maxy = (pos[1] > mesh.maxy) ? pos[1] : mesh.maxy;
		}
	}
	
	
	
	/**
	 * 毛穴1個を処理し、Delaunay三角メッシュ生成アルゴリズムを適用する
	 * @param Vertex
	 */
	static void placeOneVertex(Vertex vertex) {

		Triangle triangle;
		int  ret;

		// 毛穴を内包する皮丘を見つける
		triangle = findTriangleEncloseCurrentVertex(vertex);
		if( triangle == null ) {
			System.out.println("   ???  Triangle is NULL");
			return;
		}

		// 皮丘を3個に割る
		divideTriangle(vertex, triangle);

		// 三角形の辺組み換えを再帰的に実行する
		recursiveSwap();
		

	}
	
 
	/**
	 * 皮膚領域を囲む大きな四角形領域を生成する
	 */
    static void generateSuperRectangle() {

    	// 皮膚領域を囲む2個の大きな三角形の4頂点を生成する
    	Vertex dv1 = mesh.addOneVertex();
    	Vertex dv2 = mesh.addOneVertex();
    	Vertex dv3 = mesh.addOneVertex();
    	Vertex dv4 = mesh.addOneVertex();
    	
    	// 4頂点の座標値を算出する
    	double x1 = mesh.minx - (mesh.maxx - mesh.minx);
    	double x2 = mesh.maxx + (mesh.maxx - mesh.minx);
    	double y1 = mesh.miny - (mesh.maxy - mesh.miny);
    	double y2 = mesh.maxy + (mesh.maxy - mesh.miny);
    	dv1.setPosition(x1, y1, 0.0);
    	dv2.setPosition(x2, y1, 0.0);
    	dv3.setPosition(x2, y2, 0.0);
    	dv4.setPosition(x1, y2, 0.0);
    	
    	// 皮膚領域を囲む2個の大きな三角形を生成する
    	Triangle t1 = mesh.addOneTriangle();
    	Triangle t2 = mesh.addOneTriangle();

    	// 2個の三角形の隣接関係を構築する
    	t1.setVertices(dv1, dv2, dv3);
		t2.setVertices(dv3, dv4, dv1);
		t1.setAdjacents(null, null, t2);
		t2.setAdjacents(null, null, t1);
		
	}

    
    /**
     * 領域の外側にある三角形を削除する
     */
    static void removeOuterTriangle() {
    	double center[] = new double[2];
    	
    	// 各々の三角形について内外判定を実施する
       	for(int i = 0; i < mesh.getNumTriangles(); i++) {

       		// 三角形の重心を算出する
       		Triangle triangle = mesh.getTriangle(i);
    		Vertex vertices[] = triangle.getVertices();
    		double pos0[] = vertices[0].getPosition();
    		double pos1[] = vertices[1].getPosition();
    		double pos2[] = vertices[2].getPosition();
    		center[0] = (pos0[0] + pos1[0] + pos2[0]) / 3.0;
    		center[1] = (pos0[1] + pos1[1] + pos2[1]) / 3.0;
    		
    		// 重心が領域の外側にあれば、三角形を削除する
    		if (center[0] < mesh.minx || center[0] > mesh.maxx || center[1] < mesh.miny || center[1] > mesh.maxy) {
    			mesh.removeOneTriangle(triangle);
    			i--;
    		}
       	}
       	
       	// 一番外側の４頂点を削除する
       	for(int i = 0; i < 4; i++) {
       		mesh.vertices.remove(mesh.vertices.size() - 1);
       	}
    }
    
    /**
     * 現在処理中の毛穴を囲む皮丘を特定する
     */
    static Triangle findTriangleEncloseCurrentVertex(Vertex Vertex) {
	
    	Triangle  Triangle = mesh.getTriangle(0);
    	int   ret;
	

    	//  1回目の挑戦: 隣接性に頼った高速な検索 
    	for(int i = 0; i < mesh.getNumTriangles(); i++) {
    		if(Triangle == null ) break;
    		ret = checkTriangleEncloseCurrentVertex(Vertex, Triangle);
    		if( ret < 0 ) return Triangle;
    		Triangle adjacents[] = Triangle.getAdjacents();
    		Triangle = adjacents[ret];
    	} 
	
    	//  2回目の挑戦: Straightforward Search
    	for(int i = 0; i < mesh.getNumTriangles(); i++) { 
    		Triangle = mesh.getTriangle(i);
    		ret = checkTriangleEncloseCurrentVertex(Vertex, Triangle);
    		if( ret < 0 ) return Triangle;
    	}
	
    	// 全く皮丘が見つからなければnullを返す
    	return null;
    }
    
	/**
	 * 皮丘が毛穴を囲んでいるか否かをチェックする
	 */
    static int checkTriangleEncloseCurrentVertex(Vertex Vertex, Triangle Triangle) {
    	Vertex Vertices[] = Triangle.getVertices();
    	double pos0[] = Vertex.getPosition();
    	
    	// 皮丘の各辺について
    	for (int i = 0; i < 3; i++) {	
    		int i1 = (i == 2) ? 0 : (i + 1);	
    		double pos1[] = Vertices[i].getPosition();
    		double pos2[] = Vertices[i1].getPosition();
    		
    		double a = (pos1[1] - pos0[1]) * (pos2[0] - pos0[0]);
    		double b = (pos1[0] - pos0[0]) * (pos2[1] - pos0[1]);
	 
    		// これが成立するなら、i番目の隣接皮丘を探索すべき
    		if( a - b > 0 ) return i;
    	}

    	// これが成立するなら、皮丘が毛穴を囲んでいる
    	return -1;
    }
    
    
    /**
     * 新しい毛穴で皮丘を3分割する
     */
    static void divideTriangle(Vertex Vertex, Triangle Triangle) {
	
	    Triangle  new0, new1, new2; 
	    Triangle  adjacents[] = Triangle.getAdjacents();
	    Triangle  adj0 = adjacents[0];
	    Triangle  adj1 = adjacents[1];
	    Triangle  adj2 = adjacents[2];
    	Vertex  Vertices[] = Triangle.getVertices();
    	Vertex  h0 = Vertices[0];
    	Vertex  h1 = Vertices[1];
    	Vertex  h2 = Vertices[2];

    	// 新しい皮丘を確保する
    	new0 = Triangle;
    	new1 = mesh.addOneTriangle();
    	new2 = mesh.addOneTriangle();
	
    	new0.setVertices(Vertex, h0, h1); 
    	new0.setAdjacents(new2, adj0, new1);
	
    	new1.setVertices(Vertex, h1, h2); 
    	new1.setAdjacents(new0, adj1, new2);
	
    	new2.setVertices(Vertex, h2, h0); 
    	new2.setAdjacents(new1, adj2, new0);
    	
    	// 新しい皮丘をスタックすると同時に、隣接関係を更新する
    	if( adj0 != null ) {
    		//replaceAdjacency(adj0, Triangle, new0);
    		pushTriangleToStack(new0);
    	}
    	if( adj1 != null ) {
    		replaceAdjacency(adj1, Triangle, new1);
    		pushTriangleToStack(new1);
    	}
    	if( adj2 != null ) {
    		replaceAdjacency(adj2, Triangle, new2);
    		pushTriangleToStack(new2);
    	}
    }
    
    
	/**
	 * 皮丘の隣接関係を更新する
	 */
	static void replaceAdjacency(Triangle Triangle, Triangle oldr, Triangle newr) {
		Triangle adjacents[] = Triangle.getAdjacents();
		
		for( int i = 0; i < 3; i++ ) {
			if(adjacents[i] == oldr) {
				adjacents[i] = newr;
				Triangle.setAdjacents(adjacents[0], adjacents[1], adjacents[2]);
				return; 
			}
		}
	} 


	/**
	 * スタックに皮丘を登録する
	 */
	static void pushTriangleToStack(Triangle Triangle) { 
		swapStack.add(Triangle);
	} 

	/**
	 * スタックから皮丘を抽出する
	 */
	static Triangle popTriangleFromStack( ) {
		if( swapStack.size() <= 0 ) return null;
		Triangle ret = (Triangle)swapStack.get(swapStack.size() - 1);
		swapStack.remove((Object)ret);
		return ret;
	} 


	/**
	 * 皮丘の辺の組み換えを再帰的に反復する
	 */
	static void recursiveSwap( ) {

		Triangle ltri, rtri, adj1, adj2, adj3, adj4;
		Vertex snod1, snod2, lnod, rnod;
		int      i;
		boolean  ret;

		//
		// Repeat until no triangles are needed to swap edges
		//
		while( true ) {

			//
			//  Extract a triangle from a stack
			//
			ltri = popTriangleFromStack();
			if( ltri == null ) break;

			//
			//  Check the shared edge
			//     of the adjacent two triangle,
			//     "ltri" and "rtri", should be swapped
			//
			//                  * snod1
			//         adj3   / | \     adj2
			//              /   |   \
			//            /     |     \
			//     lnod *  ltri | rtri  * rnod
			//            \     |     /
			//              \   |   /
			//         adj4   \ | /     adj1
			//                  * snod2
			//
			Triangle ladjacents[] = ltri.getAdjacents();
			Triangle la0 = ladjacents[0];
			Triangle la1 = ladjacents[1];
			Triangle la2 = ladjacents[2];
			Vertex lVertices[] = ltri.getVertices();
			Vertex lh0 = lVertices[0];
			Vertex lh1 = lVertices[1];
			Vertex lh2 = lVertices[2];
			rtri = la1;
			if(rtri == null) {
				System.out.println("   ??? RecursiveSwap Adjacent Triangle is NULL");
				continue;
			}
			lnod = lh0;
			adj3 = la2;
			adj4 = la0;
			Triangle radjacents[] = rtri.getAdjacents();
			Vertex rVertices[] = rtri.getVertices();
			for(i = 0; i < 3; i++) {
				if(radjacents[i] == ltri)
					break;
			}
			if(i == 3) continue;
			snod1 = rVertices[i];
			i = (i == 2) ? 0 : (i + 1);
			snod2 = rVertices[i];
			adj1 = radjacents[i];
			i = (i == 2) ? 0 : (i + 1);
			rnod  = rVertices[i];
			adj2 = radjacents[i];

			//System.out.println("   swap: snod1=" + snod1.getId() + " snod2=" + snod2.getId() + " lnod=" + lnod.getId() + " rnod=" + rnod.getId());
			
			//
			//  Check if the triangles may not be swapped
			//
			//System.out.println("     swap snod1=" + snod1.getId() + " snod2=" + snod2.getId() + " lnod=" + lnod.getId() + " rnod=" + rnod.getId());
			if( (adj1 == adj4) || (adj2 == adj3) ) {
				continue;
			}
			ret = checkHillOverwrap( lnod, rnod, snod1, snod2 );
			//if(ret == true) System.out.println("       ret1=" + ret);
			if(ret == true) continue;

			//
			//  Check if the triangles are
			//    geometrically better to be swapped
			//	
			ret = checkShouldSwap( lnod, rnod, snod1, snod2 );
			//if(ret == true) System.out.println("       ret2=" + ret);
			if(ret == false) continue;

			//
			//  Swap the shared edge and
			//	 reset two triangles
			//
			ltri.setVertices(lnod, snod2, rnod);
			ltri.setAdjacents(adj4, adj1, rtri);
			
			rtri.setVertices(lnod, rnod, snod1);
			rtri.setAdjacents(ltri, adj2, adj3);
			
			if( adj1 != null ) {
				replaceAdjacency(adj1, rtri, ltri);
			}
			if( adj3 != null ) {
				replaceAdjacency(adj3, ltri, rtri);
			}

			//
			//  Register the swapped triangles ino a stack
			//
			if( adj1 != null ) {
				pushTriangleToStack( ltri );
			}
			if( adj2 != null ) {
				pushTriangleToStack( rtri );
			}	
	    
		} 
	}


	/**
	 * 皮丘間の重なりを判定する
	 */
	static boolean checkHillOverwrap(
		Vertex lnod, Vertex rnod, Vertex snod1, Vertex snod2  ) {

		double	ulr, vlr, ans1, ans2;
		double lpos[] = lnod.getPosition();
		double rpos[] = rnod.getPosition();
		double s1pos[] = snod1.getPosition();
		double s2pos[] = snod2.getPosition();

		ulr = lpos[0] - rpos[0];
		vlr = lpos[1] - rpos[1];
		ans1 = (s1pos[0] - lpos[0]) * vlr - 
		       (s1pos[1] - lpos[1]) * ulr;
		ans2 = (s2pos[0] - lpos[0]) * vlr - 
		       (s2pos[1] - lpos[1]) * ulr;

		if( ans1 * ans2 >= 0.0 ) return true;
		else                     return false;

	}


	/**
	 * 2個の皮丘間の辺を組み替えるべきか否か、幾何的に判定する
	 */
	static boolean checkShouldSwap( 
		Vertex lnod, Vertex rnod, Vertex snod1, Vertex snod2  ) {

		double    s1l[] = new double[2];
		double    s1r[] = new double[2];
		double    s2l[] = new double[2];
		double    s2r[] = new double[2];
		double    cosl, cosr, sinl, sinr, sinlr;
		double lpos[] = lnod.getPosition();
		double rpos[] = rnod.getPosition();
		double s1pos[] = snod1.getPosition();
		double s2pos[] = snod2.getPosition();
		
		s1l[0] = s1pos[0] - lpos[0];	
		s1l[1] = s1pos[1] - lpos[1];	
		s2l[0] = s2pos[0] - lpos[0];	
		s2l[1] = s2pos[1] - lpos[1];	
		s1r[0] = s1pos[0] - rpos[0];	
		s1r[1] = s1pos[1] - rpos[1];	
		s2r[0] = s2pos[0] - rpos[0];	
		s2r[1] = s2pos[1] - rpos[1];	

		cosl = s1l[0] * s2l[0] + s1l[1] * s2l[1];
		cosr = s1r[0] * s2r[0] + s1r[1] * s2r[1];

		if((cosl > 0.0) && (cosr > 0.0)) return false;
		if((cosl < 0.0) && (cosr < 0.0)) return true;

		sinl = s2l[0] * s1l[1] - s1l[0] * s2l[1];
		sinr = s1r[0] * s2r[1] - s2r[0] * s1r[1];
		sinlr = sinl * cosr + sinr * cosl;

		if (sinlr < 0.0) return true;
		else             return false;

	} 

}
