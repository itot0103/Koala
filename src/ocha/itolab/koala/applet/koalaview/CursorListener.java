package ocha.itolab.koala.applet.koalaview;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.jogamp.opengl.awt.GLCanvas;


public class CursorListener implements MouseListener, MouseMotionListener, MouseWheelListener {

	Canvas canvas = null;
	GLCanvas glcanvas = null;
	ViewingPanel  viewingPanel = null;
	int initX = 0, initY = 0;
	int rangeX0, rangeX1, rangeX2, rangeY0, rangeY1, rangeY2;
	int itotal = 0;
	long icount = 0;
	boolean pickByMove = true;
	
	/**
	 * Canvas���Z�b�g����
	 * @param c Canvas
	 */
	public void setCanvas(Object c, Object glc) {
		canvas = (Canvas) c;
		glcanvas = (GLCanvas) glc;
		glcanvas.addMouseListener(this);
		glcanvas.addMouseMotionListener(this);
		glcanvas.addMouseWheelListener(this);
	}
	
	
	/**
	 * ViewingPanel���Z�b�g����
	 * @param v ViewingPanel
	 */
	public void setViewingPanel(ViewingPanel v) {
		viewingPanel = v;
		viewingPanel.setCursorListener(this);
	}
	
	public void pickByMove(boolean flag) {
		pickByMove = flag;
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	/**
	 * �}�E�X�̃N���b�N�����o���郊�X�i�[
	 */
	public void mouseClicked(MouseEvent e) {
		
		if(canvas == null) return;
		if(glcanvas == null) return;
		
		int cX = e.getX();
		int cY = e.getY();

		Object picked = canvas.pick(cX, cY);
		canvas.display();
		
		if(picked != null && viewingPanel != null)
			viewingPanel.setPickedObject(picked);
	}

	/**
	 * �}�E�X�{�^���������ꂽ���Ƃ����o���郊�X�i�[
	 */
	public void mousePressed(MouseEvent e) {
		
		if(canvas == null) return;
		if(glcanvas == null) return;

		initX = e.getX();
		initY = e.getY();
		canvas.mousePressed();
		
		// �͈͎w��
		if(canvas.getDragMode() == 4) {
			rangeX0 = initX;   rangeY0 = initY;
		}
	}

	/**
	 * �}�E�X�{�^���������ꂽ���Ƃ����o���郊�X�i�[
	 */
	public void mouseReleased(MouseEvent e) {
		
		if(canvas == null) return;
		if(glcanvas == null) return;
		
		int cX = e.getX();
		int cY = e.getY();
		
		canvas.mouseReleased();
		canvas.display();
	}

	/**
	 * �}�E�X�J�[�\�������������Ƃ����o���郊�X�i�[
	 */
	public void mouseMoved(MouseEvent e) {
		
		if(canvas == null) return;
		if(glcanvas == null) return;
		//if(canvas.getDragMode() > 0) return;
		
		int cX = e.getX();
		int cY = e.getY();

		if(pickByMove == true) {
			Object picked = canvas.pick(cX, cY);
			canvas.display();
			if(picked != null && viewingPanel != null)
				viewingPanel.setPickedObject(picked);
		}
	
	}

	
	/**
	 * �}�E�X�J�[�\�����h���b�O�������Ƃ����o���郊�X�i�[
	 */
	public void mouseDragged(MouseEvent e) {

		if(canvas == null) return;
		if(glcanvas == null) return;
		if(canvas.getDragMode() <= 0) return;

		int m = e.getModifiers();
		if((m & MouseEvent.BUTTON1_MASK) != 0)
			canvas.setDragMode(Transformer.VIEWING_SHIFT);
		if((m & MouseEvent.BUTTON3_MASK) != 0)
			canvas.setDragMode(Transformer.VIEWING_ROTATE);
			
		int cX = e.getX();
		int cY = e.getY();
		
		canvas.drag(initX, cX, initY, cY);
		canvas.display();
	}
	
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(canvas == null) return;
		if(glcanvas == null) return;
		
		if (e.getComponent() == glcanvas) {
			icount++;
			canvas.mousePressed();
			canvas.setDragMode(Transformer.VIEWING_SCALE); // ZOOM mode
			int r = e.getWheelRotation();
			itotal -= (r * 20);
			canvas.drag(0, itotal, 0, itotal);
			canvas.display();
			WheelThread wt = new WheelThread(icount);
			wt.start();
		}
		
	}
	
	class WheelThread extends Thread {
		long count;
		WheelThread(long c) {
             this.count = c;
        }
 
         public void run() {
        	 try {
        		 Thread.sleep(100);
        	 } catch(Exception e) {
        	 	e.printStackTrace();
        	 }
        	 if(count != icount) return;
        	 
        	 itotal = 0;
         }
	}
	

}
