package mage.client;

import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.swing.*;

import mage.utils.Base64;


public class FractalViewer extends ResultViewerAdaptor {
	JDialog myDialog = null;
	Vector<String> resultList = new Vector<String>();
	
	private void rearrangeResult() {
		StringTokenizer st = new StringTokenizer(resultStr, "\n", false);
		while (st.hasMoreTokens()) {
			resultList.add(st.nextToken());
		}
	}
	
	Image image;
	MemoryImageSource source;
	
	CardLayout cardLayout = new CardLayout();
	JCheckBox chkAutoPlay = new JCheckBox("Auto Play", true);
	JButton btnPrev = new JButton("Prev");
	JButton btnNext = new JButton("Next");
	JButton btnClose = new JButton("Close");
	int imageCount = 0;
	JPanel pnlOverall = null;
	
	Thread myThread = null;
	boolean isCont = false;
	int delayMillis = 2000;
	
	private void setSizeAndLocation() {
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();		
		myDialog.setSize(600, 650);
	    myDialog.setLocation(screenSize.width / 2 - myDialog.getWidth() / 2,
	    			screenSize.height / 2 - myDialog.getHeight() / 2);
		myDialog.setVisible(true);
	}
	
	private void makeUI() {
		myDialog = new JDialog(mainFrame, "Fractal Viewer");
		
		pnlOverall = new JPanel();
		myDialog.getContentPane().setLayout(new BorderLayout());
		myDialog.getContentPane().add(pnlOverall, BorderLayout.CENTER);
		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
		pnlButtons.add(chkAutoPlay);
		pnlButtons.add(new JLabel("    "));
		pnlButtons.add(btnPrev);
		pnlButtons.add(new JLabel("    "));
		pnlButtons.add(btnNext);
		pnlButtons.add(new JLabel("    "));
		pnlButtons.add(btnClose);
		myDialog.getContentPane().add(pnlButtons, BorderLayout.SOUTH);
		
		pnlOverall.setLayout(cardLayout);
		
		imageCount = 0;
		for (String resultStr : resultList) {
			StringTokenizer stline = new StringTokenizer(resultStr, ":", false);
	
			if (stline.countTokens() < 4) continue;
			String jobNo = stline.nextToken();
			String address = stline.nextToken();
			String port = stline.nextToken();
			StringTokenizer stmsg = new StringTokenizer(new String(Base64.decode(stline.nextToken())), ":", false);
			if (stmsg.countTokens() < 2) continue;
			int msgCode = parseInt(stmsg.nextToken());
			if (msgCode == 0) continue;
			String msgStr = stmsg.nextToken(); // TODO: 결과 데이타에 세미콜론이 있으면 문제가 됨.
			
			int pixels[] = null;
			int cnt = 0;
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(msgStr.trim()));
				GZIPInputStream gzin = new GZIPInputStream(bais);
//				DataInputStream dis = new DataInputStream(gzin);
//				ObjectInputStream ois = new ObjectInputStream(gzin);
				int width = readIntData(gzin);
				int height = readIntData(gzin);
				pixels = new int[width * height];
				System.out.println("width = " + width + ", height = " + height);
				for (int i = 0; i < pixels.length; i++) {
					pixels[i] = readIntData(gzin);
				}
				bais.close();
				gzin.close();
				
				FractalDrawPanel pnl = new FractalDrawPanel(pixels, width, height, address + ":" + port);
				pnlOverall.add("" + cnt, pnl);
				cnt++;
				imageCount++;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		myDialog.setVisible(false);
		cardLayout.show(pnlOverall, "0");

		setSizeAndLocation();
		checkAndRunThread();
	}
	
	private int readIntData(InputStream in) throws Exception {
		int data = 0;
		data = in.read();
		data += (in.read() << 8);
		data += (in.read() << 16);
		data += (in.read() << 24);
		
		return data;
	}
	
	private int parseInt(String data) {
		int result = 0;
		try {
			result = Integer.parseInt(data);
		} catch (Exception ex) {
			ex.printStackTrace();
			result = 0;
		}
		return result;
	}
	
	@Override
	public void viewResult() {
		rearrangeResult();
		makeUI();
		makeEvents();
	}
	
	private void makeEvents() {
		btnPrev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				cardLayout.previous(pnlOverall);
			}
		});
		
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				cardLayout.next(pnlOverall);
			}
		});
		
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				myDialog.setVisible(false);
				myDialog.dispose();
			}
		});
		
		chkAutoPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				checkAndRunThread();
			}
		});
		
		if (imageCount < 2) {
			chkAutoPlay.setEnabled(false);
			btnPrev.setEnabled(false);
			btnNext.setEnabled(false);
		}
	}

	private void checkAndRunThread() {
		if (imageCount < 2) return;
		if (chkAutoPlay.isSelected()) {
			if (myThread != null) return;
			
			myThread = new Thread() {
				public void run() {
					try {
						while (isCont) {
							cardLayout.next(pnlOverall);
							Thread.sleep(delayMillis);
						}
					} catch (Exception ex) {
						isCont = false;
					}
				}
			};
			isCont = true;
			myThread.start();
		} else {
			isCont = false;
			myThread.interrupt();
			myThread = null;
		}
	}
}
