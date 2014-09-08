package ocha.itolab.koala.applet.koalaview;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.gl2.GLUgl2;

import com.jogamp.opengl.util.gl2.GLUT;

import ocha.itolab.koala.core.data.*;
import ocha.itolab.koala.core.mesh.*;


/**
 * 描画処理のクラス
 * 
 * @author itot
 */
public class Drawer implements GLEventListener {
	
	private GL gl;
	private GL2 gl2;
	private GLU glu;
	private GLUgl2 glu2;
	private GLUT glut;
	GLAutoDrawable glAD;
	GLCanvas glcanvas;
	Graph graph;
	
	Transformer trans = null;
	ViewingPanel vp = null;

	DoubleBuffer modelview, projection, p1, p2, p3, p4;
	IntBuffer viewport;
	int windowWidth, windowHeight;
	
	boolean isMousePressed = false, isAnnotation = true;

	double edgeDensityThreshold = 0.1;
	double linewidth = 1.0;
	double bundleShape = 0.7;
	double transparency = 0.9;
	boolean colorSwitch[] = null;
	int edgeDensityMode = 1;
	int colorMode = 1;
	double xmin, xmax, ymin, ymax;
	int rangeX1 = 0, rangeX2 = 0, rangeY1 = 0, rangeY2 = 0;
	DrawerUtility du = null;

	int dragMode = 1;
	private double angleX = 0.0;
	private double angleY = 0.0;
	private double shiftX = 0.0;
	private double shiftY = 0.0;
	private double scaleX = 0.5;
	private double scaleY = 0.5;
	private double centerX = 0.0;
	private double centerY = 0.0;
	private double centerZ = 0.0;
	private double size = 0.5;
	
	Node pickedNode = null;
	
	
	
	/**
	 * Constructor
	 * 
	 * @param width
	 *            描画領域の幅
	 * @param height
	 *            描画領域の高さ
	 */
	public Drawer(int width, int height, GLCanvas c) {
		glcanvas = c;
		windowWidth = width;
		windowHeight = height;
		du = new DrawerUtility(width, height);

		viewport = IntBuffer.allocate(4);
		modelview = DoubleBuffer.allocate(16);
		projection = DoubleBuffer.allocate(16);

		p1 = DoubleBuffer.allocate(3);
		p2 = DoubleBuffer.allocate(3);
		p3 = DoubleBuffer.allocate(3);
		p4 = DoubleBuffer.allocate(3);

		glcanvas.addGLEventListener(this);
	}

	public GLAutoDrawable getGLAutoDrawable() {
		return glAD;
	}

	/**
	 * ダミーメソッド
	 */
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	/**
	 * Transformerをセットする
	 * 
	 * @param transformer
	 */
	public void setTransformer(Transformer view) {
		this.trans = view;
		du.setTransformer(view);
	}

	/**
	 * 描画領域のサイズを設定する
	 * 
	 * @param width
	 *            描画領域の幅
	 * @param height
	 *            描画領域の高さ
	 */
	public void setWindowSize(int width, int height) {
		// windowWidth = width;
		// windowHeight = height;
		// du.setWindowSize(width, height);
	}

	/**
	 * マウスボタンのON/OFFを設定する
	 * 
	 * @param isMousePressed
	 *            マウスボタンが押されていればtrue
	 */
	public void setMousePressSwitch(boolean isMousePressed) {
		this.isMousePressed = isMousePressed;
		if (isMousePressed == true) {
			// drawCategoryTextField();
		}
	}

	/**
	 * 線の太さをセットする
	 * 
	 * @param lw
	 *            線の太さ（画素数）
	 */
	public void setLinewidth(double lw) {
		linewidth = lw;
	}

	public void setGraph(Graph g) {
		graph = g;
		calcMeshColor();
	}

	public void setEdgeThreshold(double ratio) {
		edgeDensityThreshold = ratio;
	}
	
	public void setBundleShape(double ratio) {
		bundleShape = ratio;
	}
	
	public void setEdgeDensityMode(int mode) {
		edgeDensityMode = mode;
	}
	
	public void setBackgroundTransparency(double t) {
		transparency = t;
		calcMeshColor();
	}
	
	public void setColorMode(int mode) {
		colorMode = mode;
		calcMeshColor();
	}
		

	/**
	 * マウスドラッグのモードを設定する
	 * 
	 * @param dragMode
	 *            (1:ZOOM 2:SHIFT 3:ROTATE)
	 */
	public void setDragMode(int newMode) {
		dragMode = newMode;
	}


	public void setColorSwitch(boolean[] st) {
		colorSwitch = st;
	}

	
	/**
	 * ViewingPanelを設定する
	 */
	public void setViewingPanel(ViewingPanel v) {
		vp = v;
	}

	

	
	void calcMeshColor() {
		Mesh mesh = graph.mesh;

		
		// for each vertex
		for(int i = 0; i < mesh.getNumVertices(); i++) {
			Vertex v = mesh.getVertex(i);
			double color[] = {0.0, 0.0, 0.0};
			int counter = 0;
			
			// if colorMode is COLOR_DEGREE
			if(colorMode == Canvas.COLOR_DEGREE) {
				v.setColor(transparency, transparency, transparency);
				continue;
			}
				
			// determine the color of the vertex
			ArrayList<Node> nodes = v.getNodes();
			for(int k = 0; k < nodes.size(); k++) {
				int colorId = nodes.get(k).getColorId();
				if(colorId >= 0) {
					Color cc = VectorParettePanel.calcColor(colorId, graph.vectorname.length);
					double rr = (double)cc.getRed() / 255.0;
					double gg = (double)cc.getGreen() / 255.0;
					double bb = (double)cc.getBlue() / 255.0;
					color[0] += rr;
					color[1] += gg;
					color[2] += bb;
					counter++;
				}
			}
		
			if(counter > 0) {
				color[0] /= (double)counter;
				color[1] /= (double)counter;
				color[2] /= (double)counter;
				color[0] = transparency + (1.0 - transparency) * color[0];
				color[1] = transparency + (1.0 - transparency) * color[1];
				color[2] = transparency + (1.0 - transparency) * color[2];
				v.setColor(color[0], color[1], color[2]);
			}
			else
				v.setColor(transparency, transparency, transparency);
			
		}
			
	}

	double calcZ(Node node) {
		double z = 0.0;
		double degratio = (double)(node.getNumConnectedEdge() + node.getNumConnectingEdge()) /  (double)graph.maxDegree;
		double ke = graph.mesh.keyEmphasis;
		if(degratio >  0.1 && ke > 0.1) 
			z = degratio * ke;

		return z;
	}
	
	/**
	 * 初期化
	 */
	public void init(GLAutoDrawable drawable) {

		gl = drawable.getGL();
		gl2 = drawable.getGL().getGL2();
		glu = new GLU();
		glu2 = new GLUgl2();
		glut = new GLUT();
		this.glAD = drawable;

		gl.glEnable(GL.GL_RGBA);
		gl.glEnable(GL2.GL_DEPTH);
		gl.glEnable(GL2.GL_DOUBLE);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_NORMALIZE);
		gl2.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE);
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

	}

	/**
	 * 再描画
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {

		windowWidth = width;
		windowHeight = height;

		// ビューポートの定義
		gl.glViewport(0, 0, width, height);

		// 投影変換行列の定義
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		gl2.glOrtho(-width / 200.0, width / 200.0, -height / 200.0,
				height / 200.0, -1000.0, 1000.0);

		gl2.glMatrixMode(GL2.GL_MODELVIEW);

	}

	/**
	 * 描画を実行する
	 */
	public void display(GLAutoDrawable drawable) {

		long mill1 = System.currentTimeMillis();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		// 視点位置を決定
		gl2.glLoadIdentity();
		glu.gluLookAt(centerX, centerY, (centerZ + 20.0), centerX, centerY,
				centerZ, 0.0, 1.0, 0.0);

		shiftX = trans.getViewShift(0);
		shiftY = trans.getViewShift(1);
		scaleX = trans.getViewScaleX() * windowWidth / (size * 600.0);
		scaleY = trans.getViewScaleY() * windowHeight / (size * 600.0);
		angleX = trans.getViewRotateY() * 45.0;
		angleY = trans.getViewRotateX() * 45.0;

		// 行列をプッシュ
		gl2.glPushMatrix();

		// いったん原点方向に物体を動かす
		gl2.glTranslated(centerX, centerY, centerZ);

		// マウスの移動量に応じて回転
		gl2.glRotated(angleX, 1.0, 0.0, 0.0);
		gl2.glRotated(angleY, 0.0, 1.0, 0.0);

		// マウスの移動量に応じて移動
		gl2.glTranslated(shiftX, shiftY, 0.0);

		// マウスの移動量に応じて拡大縮小
		gl2.glScaled(scaleX, scaleY, 1.0);

		// 物体をもとの位置に戻す
		gl2.glTranslated(-centerX, -centerY, -centerZ);

		// 変換行列とビューポートの値を保存する
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport);
		gl2.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, modelview);
		gl2.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projection);

		// 描画
		paintMesh();
		if(edgeDensityMode == Canvas.EDGE_DENSITY_DISSIMILARITY)
			drawEdgesDissimilarity();
		if(edgeDensityMode == Canvas.EDGE_DENSITY_DEGREE)
			drawEdgesDegree();
		drawPickedEdges();
		
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		drawNodes();
		gl.glDisable(GL2.GL_LIGHTING);
		
		// 行列をポップ
		gl2.glPopMatrix();

		long mill2 = System.currentTimeMillis();
		// System.out.println("  drawer.display() time=" + (mill2 - mill1));
	}

	

	/**
	 * 背景色となるMeshを塗りつぶす
	 */
	void paintMesh() {
		if(graph == null) return;
		if(graph.mesh == null) return;

		// for each triangle
		Mesh mesh = graph.mesh;
		float colf[] = new float[3]; 
		for(int i = 0; i < mesh.getNumTriangles(); i++) {
			Triangle t = mesh.getTriangle(i);
		
			gl2.glBegin(gl2.GL_POLYGON);
			
			// for each vertex
			Vertex v[] = t.getVertices();
			for(int j = 0; j < 3; j++) {
				double pos[] = v[j].getPosition();
				double col[] = v[j].getColor();
				colf[0] = (float)col[0];
				colf[1] = (float)col[1];
				colf[2] = (float)col[2];
				gl2.glColor3d(col[0], col[1], col[2]);
				gl2.glVertex3d(pos[0], pos[1], -0.05);	
			}
				
			gl2.glEnd();
		}

	}
	
	
	/**
	 * Select edges to be drawn based on dissimilarity between two vertices 
	 */
	void drawEdgesDissimilarity() {
		if(graph == null) return;
		if(graph.mesh == null) return;
		
		// Draw bundled edges
		gl2.glColor3d(0.7, 0.7, 0.7);

		Mesh mesh = graph.mesh;
		for(int i = 0; i < mesh.getNumVertices(); i++) {
			Vertex v1 = mesh.getVertex(i);
			ArrayList<Node> nodes1 = v1.getNodes();
			double dissim[] = v1.getDissim();

			for(int j = (i + 1); j < mesh.getNumVertices(); j++) {
				if(dissim[j] > edgeDensityThreshold) continue;
				Vertex v2 = mesh.getVertex(j);	
				ArrayList<Node> nodes2 = v2.getNodes();
					
				for(int ii = 0; ii < nodes1.size(); ii++) {
					Node n1 = nodes1.get(ii);
					for(int jj = 0; jj < nodes2.size(); jj++) {
						Node n2 = nodes2.get(jj);
						if(graph.isTwoNodeConnected(n1, n2) == false)
							continue;
						drawBundledEdges(v1, v2, n1, n2);
					}
				}
			}
			
		}
		
	}
		
	/**
	 * Select edges to be drawn based on degrees of nodes
	 */
	void drawEdgesDegree() {
		if(graph == null) return;
		if(graph.mesh == null) return;
		
		// Draw bundled edges
		gl2.glColor3d(0.7, 0.7, 0.7);
		int mindeg = (int)((double)graph.maxDegree * (1.0 - edgeDensityThreshold));
		
		Mesh mesh = graph.mesh;
		for(int i = 0; i < mesh.getNumVertices(); i++) {
			Vertex v1 = mesh.getVertex(i);
			ArrayList<Node> nodes1 = v1.getNodes();
			
			for(int j = (i + 1); j < mesh.getNumVertices(); j++) {
				Vertex v2 = mesh.getVertex(j);
				ArrayList<Node> nodes2 = v2.getNodes();
				
				for(int ii = 0; ii < nodes1.size(); ii++) {
					Node n1 = nodes1.get(ii);
					int deg1 = n1.getNumConnectedEdge() + n1.getNumConnectingEdge();
					
					for(int jj = 0; jj < nodes2.size(); jj++) {
						Node n2 = nodes2.get(jj);
						int deg2 = n2.getNumConnectedEdge() + n2.getNumConnectingEdge();
						if(deg1 < mindeg && deg2 < mindeg) continue;
						if(graph.isTwoNodeConnected(n1, n2) == false)
							continue;
						drawBundledEdges(v1, v2, n1, n2);	
					}
				}
			}	
		}
	}
	
	
	
	void drawPickedEdges() {
		// Draw edges of the picked node
		gl2.glColor3d(0.8, 0.6, 0.8);
		gl2.glLineWidth(2.0f);
		if(pickedNode != null) {
			double z = calcZ(pickedNode);
		
			for(int i = 0; i < pickedNode.getNumConnectedEdge(); i++) {
				Edge e = pickedNode.getConnectedEdge(i);
				Node enode[] = e.getNode();
				gl2.glBegin(GL.GL_LINES);
				if(enode[0] == pickedNode) {
					double z2 = calcZ(enode[1]);
					gl2.glVertex3d(enode[0].getX(), enode[0].getY(), z);
					gl2.glVertex3d(enode[1].getX(), enode[1].getY(), z2);
				}
				else {
					double z2 = calcZ(enode[0]);
					gl2.glVertex3d(enode[0].getX(), enode[0].getY(), z2);
					gl2.glVertex3d(enode[1].getX(), enode[1].getY(), z);
				}
				gl2.glEnd();
			}
			for(int i = 0; i < pickedNode.getNumConnectingEdge(); i++) {
				Edge e = pickedNode.getConnectingEdge(i);
				Node enode[] = e.getNode();
				gl2.glBegin(GL.GL_LINES);
				if(enode[0] == pickedNode) {
					double z2 = calcZ(enode[1]);
					gl2.glVertex3d(enode[0].getX(), enode[0].getY(), z);
					gl2.glVertex3d(enode[1].getX(), enode[1].getY(), z2);
				}
				else {
					double z2 = calcZ(enode[0]);
					gl2.glVertex3d(enode[0].getX(), enode[0].getY(), z2);
					gl2.glVertex3d(enode[1].getX(), enode[1].getY(), z);
				}
				gl2.glEnd();
			}
		}
		gl2.glLineWidth(1.0f);
	}
	
	
	void drawBundledEdges(Vertex v1, Vertex v2, Node n1, Node n2) {
		int NUM_T = 10;
		double ONE_THIRD = 0.33333333333;
		
		int cid1 = n1.getColorId();
		int cid2 = n1.getColorId();
		if(colorSwitch != null 
				&& cid1 >= 0 && colorSwitch[cid1] == false
				&& cid2 >= 0 && colorSwitch[cid2] == false) {
			return;
		}
		
		
		double p0[] = new double[2];
		double p1[] = new double[2];
		double p2[] = new double[2];
		double p3[] = new double[2];
		double v1pos[] = v1.getPosition();
		double v2pos[] = v2.getPosition();
		
		p0[0] = n1.getX();    p0[1] = n1.getY();
		p3[0] = n2.getX();    p3[1] = n2.getY();
		
		double z1 = calcZ(n1);
		double z2 = calcZ(n2);
		
		if(bundleShape > 0.5) {
			double ratio = (bundleShape + 0.5) * 2.0 * ONE_THIRD;
			p1[0] = v1pos[0] * ratio + v2pos[0] * (1.0 - ratio);
			p1[1] = v1pos[1] * ratio + v2pos[1] * (1.0 - ratio);
			p2[0] = v2pos[0] * ratio + v1pos[0] * (1.0 - ratio);
			p2[1] = v2pos[1] * ratio + v1pos[1] * (1.0 - ratio);
		}
		else {
			double ratio = bundleShape * 2.0;
			p1[0] = (v1pos[0] * 2.0 + v2pos[0]) * ONE_THIRD * ratio
					  + (p0[0] * 2.0 + p3[0]) * ONE_THIRD * (1.0 - ratio);
			p1[1] = (v1pos[1] * 2.0 + v2pos[1]) * ONE_THIRD * ratio
					  + (p0[1] * 2.0 + p3[1]) * ONE_THIRD * (1.0 - ratio);
			p2[0] = (v2pos[0] * 2.0 + v1pos[0]) * ONE_THIRD * ratio
					  + (p3[0] * 2.0 + p0[0]) * ONE_THIRD * (1.0 - ratio);
			p2[1] = (v2pos[1] * 2.0 + v1pos[1]) * ONE_THIRD * ratio
					  + (p3[1] * 2.0 + p0[1]) * ONE_THIRD * (1.0 - ratio);
		}
		
		
		double pt[] = new double[2];
		gl2.glBegin(GL2.GL_LINE_STRIP);
		for(int i = 0; i <= NUM_T; i++) {
			double interval = 1.0 / (double)NUM_T;
			double t0 = interval * (double)i;
			double t1 = 1.0 - t0;
			
			for(int j = 0; j < 2; j++) 
				pt[j] = p0[j] * t1 * t1 * t1 + p1[j] * 3.0 * t0 * t1 * t1
				      + p2[j] * 3.0 * t0 * t0 * t1 + p3[j] * t0 * t0 * t0; 
			
			double z = (z1 * (NUM_T - i) + z2 * i) / (double)NUM_T;
			gl2.glVertex3d(pt[0], pt[1], z);
			
		}
		gl2.glEnd();
		
	}
	
	
	
	void drawNodes() {
		if(graph == null) return;
		float colf[] = new float[3];
		
		// Draw plots
		double SQUARE_SIZE = 0.0075 / trans.getViewScaleX();
		for(int i = 0; i < graph.nodes.size(); i++) {
			Node node = (Node)graph.nodes.get(i);
			double x = node.getX();
			double y = node.getY();			
			
			if(colorMode == Canvas.COLOR_DEGREE) {
				double dratio = (double)(node.getNumConnectedEdge() + node.getNumConnectingEdge()) / (double)graph.maxDegree;
				//dratio = Math.sqrt(dratio);
				double rr = 1.0 * dratio + 0.5 * (1.0 - dratio);
				double gg = 0.0 * dratio + 0.5 * (1.0 - dratio);
				double bb = 0.0 * dratio + 0.5 * (1.0 - dratio);
				double z = calcZ(node);
				if(z > 0.01) {
					colf[0] = (float)rr;
					colf[1] = (float)gg;
					colf[2] = (float)bb;
					gl2.glMaterialfv(GL.GL_FRONT_AND_BACK,
							GL2.GL_AMBIENT_AND_DIFFUSE, colf, 0);
					drawOneBarWithHeight(x, y, z, SQUARE_SIZE);
					
				}
				else {
					double ke2 = graph.mesh.keyEmphasis * 0.5;
					colf[0] = (float)(rr * (1.0 - ke2) + ke2);
					colf[1] = (float)(gg * (1.0 - ke2) + ke2);
					colf[2] = (float)(bb * (1.0 - ke2) + ke2);
					gl2.glMaterialfv(GL.GL_FRONT_AND_BACK,
							GL2.GL_AMBIENT_AND_DIFFUSE, colf, 0);
					gl2.glBegin(GL2.GL_POLYGON);
					gl2.glVertex3d(x - SQUARE_SIZE, y + SQUARE_SIZE, 0.0);
					gl2.glVertex3d(x - SQUARE_SIZE, y - SQUARE_SIZE, 0.0);
					gl2.glVertex3d(x + SQUARE_SIZE, y - SQUARE_SIZE, 0.0);
					gl2.glVertex3d(x + SQUARE_SIZE, y + SQUARE_SIZE, 0.0);
					gl2.glEnd();
				}
				continue;
			}
			
			int colorId = node.getColorId();
			if(colorId < 0) {
				colf[0] = colf[1] = colf[2] = 0.5f;
				gl2.glMaterialfv(GL.GL_FRONT_AND_BACK,
						GL2.GL_AMBIENT_AND_DIFFUSE, colf, 0);
				gl2.glBegin(GL2.GL_POLYGON);
				gl2.glVertex3d(x - SQUARE_SIZE, y + SQUARE_SIZE, 0.0);
				gl2.glVertex3d(x - SQUARE_SIZE, y - SQUARE_SIZE, 0.0);
				gl2.glVertex3d(x + SQUARE_SIZE, y - SQUARE_SIZE, 0.0);
				gl2.glVertex3d(x + SQUARE_SIZE, y + SQUARE_SIZE, 0.0);
				gl2.glEnd();
			}
			else if(colorSwitch != null && colorSwitch[colorId] == false) {
				continue;
			}
			else {
				Color color = VectorParettePanel.calcColor(colorId, graph.vectorname.length);
				double rr = (double)color.getRed() / 255.0;
				double gg = (double)color.getGreen() / 255.0;
				double bb = (double)color.getBlue() / 255.0;
				double z = calcZ(node);
				if(z > 0.01) {
					colf[0] = (float)rr;
					colf[1] = (float)gg;
					colf[2] = (float)bb;
					gl2.glMaterialfv(GL.GL_FRONT_AND_BACK,
							GL2.GL_AMBIENT_AND_DIFFUSE, colf, 0);
					drawOneBarWithHeight(x, y, z, SQUARE_SIZE);
					
				}
				else {
					double ke2 = graph.mesh.keyEmphasis * 0.5;
					colf[0] = (float)(rr * (1.0 - ke2) + ke2);
					colf[1] = (float)(gg * (1.0 - ke2) + ke2);
					colf[2] = (float)(bb * (1.0 - ke2) + ke2);
					gl2.glMaterialfv(GL.GL_FRONT_AND_BACK,
							GL2.GL_AMBIENT_AND_DIFFUSE, colf, 0);
					gl2.glBegin(GL2.GL_POLYGON);
					gl2.glVertex3d(x - SQUARE_SIZE, y + SQUARE_SIZE, 0.0);
					gl2.glVertex3d(x - SQUARE_SIZE, y - SQUARE_SIZE, 0.0);
					gl2.glVertex3d(x + SQUARE_SIZE, y - SQUARE_SIZE, 0.0);
					gl2.glVertex3d(x + SQUARE_SIZE, y + SQUARE_SIZE, 0.0);
					gl2.glEnd();
				}
			}
			
		}
		
		
		// Draw annotation
		if(pickedNode != null && pickedNode.getNumDescription() > 0) {
			String line = pickedNode.getDescription(0);
			for(int i = 1; i < pickedNode.getNumDescription(); i++) 
				line += " " + pickedNode.getDescription(i);
			glu2.gluUnProject(0.0, 0.0, 0.0, modelview, projection, viewport, p1);
			gl2.glColor3d(0.7, 0.0, 0.0);
			writeOneString(p1.get(0), p1.get(1), line);
		}
	}


	void drawOneBarWithHeight(double x, double y, double z, double SQUARE_SIZE) {
	
		gl2.glBegin(GL2.GL_POLYGON);
		gl2.glVertex3d(x - SQUARE_SIZE, y + SQUARE_SIZE, z);
		gl2.glVertex3d(x - SQUARE_SIZE, y - SQUARE_SIZE, z);
		gl2.glVertex3d(x + SQUARE_SIZE, y - SQUARE_SIZE, z);
		gl2.glVertex3d(x + SQUARE_SIZE, y + SQUARE_SIZE, z);
		gl2.glEnd();
		gl2.glBegin(GL2.GL_POLYGON);
		gl2.glVertex3d(x - SQUARE_SIZE, y + SQUARE_SIZE, 0.0);
		gl2.glVertex3d(x - SQUARE_SIZE, y - SQUARE_SIZE, 0.0);
		gl2.glVertex3d(x - SQUARE_SIZE, y - SQUARE_SIZE, z);
		gl2.glVertex3d(x - SQUARE_SIZE, y + SQUARE_SIZE, z);
		gl2.glEnd();
		gl2.glBegin(GL2.GL_POLYGON);
		gl2.glVertex3d(x + SQUARE_SIZE, y + SQUARE_SIZE, 0.0);
		gl2.glVertex3d(x + SQUARE_SIZE, y - SQUARE_SIZE, 0.0);
		gl2.glVertex3d(x + SQUARE_SIZE, y - SQUARE_SIZE, z);
		gl2.glVertex3d(x + SQUARE_SIZE, y + SQUARE_SIZE, z);
		gl2.glEnd();
		gl2.glBegin(GL2.GL_POLYGON);
		gl2.glVertex3d(x - SQUARE_SIZE, y - SQUARE_SIZE, 0.0);
		gl2.glVertex3d(x + SQUARE_SIZE, y - SQUARE_SIZE, 0.0);
		gl2.glVertex3d(x + SQUARE_SIZE, y - SQUARE_SIZE, z);
		gl2.glVertex3d(x - SQUARE_SIZE, y - SQUARE_SIZE, z);
		gl2.glEnd();
		gl2.glBegin(GL2.GL_POLYGON);
		gl2.glVertex3d(x - SQUARE_SIZE, y + SQUARE_SIZE, 0.0);
		gl2.glVertex3d(x + SQUARE_SIZE, y + SQUARE_SIZE, 0.0);
		gl2.glVertex3d(x + SQUARE_SIZE, y + SQUARE_SIZE, z);
		gl2.glVertex3d(x - SQUARE_SIZE, y + SQUARE_SIZE, z);
		gl2.glEnd();
	}
	
	public Object pick(int cx, int cy) {
		double PICK_DIST = 20.0;
		
		if(graph == null) return null;
		pickedNode = null;
		double dist = 1.0e+30;
		cy = viewport.get(3) - cy + 1;
		
		for(int i = 0; i < graph.nodes.size(); i++) {
			Node node = (Node)graph.nodes.get(i);
			double x = node.getX();
			double y = node.getY();
			glu2.gluProject(x, y, 0.0, modelview, projection, viewport, p1);
			double xx = p1.get(0);
			double yy = p1.get(1);
			double dd = (cx - xx) * (cx - xx) + (cy - yy) * (cy - yy);
			if(dd < PICK_DIST && dd < dist) {
				dist = dd;    pickedNode = node;
			}
		}
		
		return (Object)pickedNode;
		
	}
	
	
	void writeOneString(double x, double y, String word) {
		gl2.glRasterPos3d(x, y, 0.01);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, word);
	}
	
	
	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub

	}

}