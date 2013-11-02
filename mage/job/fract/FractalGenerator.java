package mage.job.fract;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import mage.job.JobState;
import mage.utils.Base64;


public class FractalGenerator extends Thread {
	int width, height;
	int i, ix, iy;
	int pixels[], buffer[];
	int cmask, color;
	int red, grn, blu;
	double rFactor, gFactor, bFactor;
	int tmp, nPoints, kicker;
	int nColorMode;
	int index;
	int mx;
	int my;
	int bug;
	double x, xs, y, ys, wx, a, b, c, sign, scale, tmp2, z, W;
	double AA, BB, xn, yn;
	double LSum;
//	Image image;
//	MemoryImageSource source;
	HashMap arguments;
	
	int jobState = JobState.JOB_INIT;
	
	public int getJobState() {
		return jobState;
	}
	
	public FractalGenerator(HashMap arguments) {
		System.out.println("FractalGenerator created");
		this.arguments = arguments;
		width = 600;
		height = 600;
		init();
	}
	
	public int getResultCode() {
		return jobState == JobState.JOB_FINISHED ? 100 : 0;
	}
	
	public String getResultData() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream gzout = new GZIPOutputStream(baos);

			writeIntData(gzout, width);
			writeIntData(gzout, height);
			for (int i = 0; i < pixels.length; i++) {
				writeIntData(gzout, pixels[i]);
			}
			
			gzout.flush();
			baos.flush();
			gzout.close();			
			baos.close();

			String data = Base64.encode(baos.toByteArray());
			
		
			return data;
		} catch (Exception ex) {
			return "";
		}
	}
	
	private void writeIntData(OutputStream out, int data) throws Exception {
		out.write(data & 0xff);
		out.write((data >> 8) & 0xff);
		out.write((data >> 16) & 0xff);
		out.write((data >> 24) & 0xff);
	}

	Random rand;
	
	public void init() {
		pixels = new int[width * height];
		buffer = new int[width * height];

		rand = new Random(System.currentTimeMillis());
		
		for (i = 0; i < width * height; i++) {
			pixels[i] = 0xff000000;
			buffer[i] = 64;
		}

//		source = new MemoryImageSource(width, height, pixels, 0, width);
//		image = Panel.createImage(source);
		nColorMode = 1;
		cmask = 0xff000000;

		mx = width / 2;
		my = height / 2;

		bug = 0;
		scale = 5 + rand.nextInt(10);

		initXY();
	}

	public void initXY() {
		tmp2 = width * height;

		for (i = 0; i < tmp2; i++) {
			// intialize image to black
			pixels[i] = 0xff000000;
			buffer[i] = 64;
		}

		RandomConstants();

	}

	public void RandomConstants() {
		rFactor = 100 * (Math.random() - 0.5);
		gFactor = 100 * (Math.random() - 0.5);
		bFactor = 100 * (Math.random() - 0.5);

		a = (Math.random() - 0.5);
		b = (Math.random() - 0.5);
		c = (Math.random() - 0.5);

		//AA = 0.5 + 0.2 * (1 + 2 * (Math.random() - 0.5));
		AA = (Math.random()-0.5);
		BB = 1.00;

		a = Math.abs(a);

		if (Math.abs(AA) < 0.5) {
			if (AA > 0)
				AA = AA + 0.5;
			else
				AA = AA - 0.5;
		}

		b = a + 0.01;
		c = -1;

		x = -1.1;
		y = 0;

		xs = 1;
		ys = 1;

		W = 1;

		nPoints = rand.nextInt() / 50 * 3000;
		kicker = 0;

	}

	public void run() {
		try {
			jobState = JobState.JOB_START;
			initXY();
			jobState = JobState.JOB_PROGRESS;
			for (nColorMode = 1; nColorMode <= 50; nColorMode++) {
				// Modify the values in the pixels array at (x, y, w, h)
				for (int i = 0; i < 30000; i++) {
					nextGeneration();
					nPoints += 1;
					ix = (int) (scale * x + mx);
					iy = (int) (scale * y + my);

					if ((Math.abs(scale * x) < mx * scale)
							&& (Math.abs(scale * y) < my * scale)
							&& (Math.abs(xs - x) > 1e-6)
							&& (Math.abs(ys - y) > 1e-6)) {
						if ((Math.abs(scale * x) < mx)
								&& (Math.abs(scale * y) < my)) {
							coloring();
							// if (sign > 0)
							pixels[ix + iy * (width)] = color;
						}
					} else {
						bug = bug + 1;

						if ((Math.abs(xs - x) < 1e-6)
								&& (Math.abs(ys - y) < 1e-6)) {
							RandomConstants();
						} else {
							initXY();
						}
					}
				}
				coloring();
			}
			jobState = JobState.JOB_FINISHED;
		} catch (Exception ex) {
			jobState = JobState.JOB_FAILED;
			ex.printStackTrace();
		}
	}
	
	public void nextGeneration() {
		if (nPoints % 100000 == 0) {
			kicker = kicker + 1;
		}

		xs = x;
		ys = y;

		if (x >= 0)
			sign = 1;
		else
			sign = -1;

		x = x + (x * sign) / 50000;
		xn = BB * y + AA * x + 2 * x * x * ((1 - AA) / (1 + x * x));
		y = -x + AA * xn + 2 * xn * xn * ((1 - AA) / (1 + xn * xn));
		x = xn;

	}

	void coloring() {

		if ((Math.abs(scale * x) < mx) && (Math.abs(scale * y) < my)) {

			LSum = 20 * Math.abs(Math.sin(Math.PI
					* Math.atan((1 + x - xs) / (1 + y - ys))));

			index = ix + iy * (width);
			buffer[index] = buffer[index] + (int) LSum;
			color = (int) (10 * (Math.sqrt(10 * buffer[index])));

			if (color > 767)
				color = 767;

			if (color < 256)
				red = color;
			else
				red = 255;

			if (color < 512 && color > 255) {
				grn = color - 256;
			} else {
				if (color >= 512)
					grn = 255;
				else
					grn = 0;
			}

			if (color <= 768 && color > 511)
				blu = color - 512;
			else {
				if (color >= 768)
					blu = 255;
				else
					blu = 0;
			}

			tmp = (int) ((red + grn + blu) * 0.33333);
			if (tmp > 255)
				tmp = 255;

			if (tmp < 0)
				tmp = 0;

			red = tmp + (int) rFactor;
			grn = tmp + (int) gFactor;
			blu = tmp + (int) bFactor;

			if (red > 255)
				red = 255;

			if (red < 0)
				red = 0;

			if (grn > 255)
				grn = 255;

			if (grn < 0)
				grn = 0;

			if (blu > 255)
				blu = 255;

			if (blu < 0)
				blu = 0;

			red = red & 0xFF;
			grn = grn & 0xFF;
			blu = blu & 0xFF;

			red = (red << 16) & 0x00ff0000; // shift left 16 places
			grn = (grn << 8) & 0x0000ff00; // shift left 8 places

			color = cmask | red | grn | blu;

		}
	}	
}