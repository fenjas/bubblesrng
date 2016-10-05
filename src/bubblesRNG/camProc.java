package bubblesRNG;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

public class camProc {

	private BufferedImage image;

	//Save captured image to disk
	void saveFrame(BufferedImage img, String fName) {
		try {

			File out = new File(fName);
			ImageIO.write(img, "jpg", out);
			this.setImage(img);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	//Checks if a webcam is present
	boolean isCameraPresent() {
		VideoCapture camera = new VideoCapture(0);
		boolean camOn = false;
		if (camera.read(new Mat())) {
			camOn = true;
			camera.release();
		} else
			camOn = false;
		return camOn;
	}

	//Grab a single frame if grabtype==0 else keep on grabbing if 1
	void grabFrame(int grabType) {

		VideoCapture camera = new VideoCapture(0);
		Mat frame = new Mat();
		
		//camera.set(1, 800);
		//camera.set(2, 600);
		camera.read(frame);

		if (!camera.isOpened()) {
			System.out.println("Error");
		} else {
			while (true) {
				if (camera.read(frame)) {
					setImage(matToBufferedImage(frame));
					if (grabType == 1)
						break;
				}
			}
		}
		camera.release();
	}
	
	//IPLImage to BufferedImage conversion algorithm
	static BufferedImage matToBufferedImage(Mat matrix) {
		int cols = matrix.cols();
		int rows = matrix.rows();
		int elemSize = (int) matrix.elemSize();
		byte[] data = new byte[cols * rows * elemSize];
		int type;
		matrix.get(0, 0, data);
		switch (matrix.channels()) {
		case 1:
			type = BufferedImage.TYPE_BYTE_GRAY;
			break;
		case 3:
			type = BufferedImage.TYPE_3BYTE_BGR;
			// bgr to rgb
			byte b;
			for (int i = 0; i < data.length; i = i + 3) {
				b = data[i];
				data[i] = data[i + 2];
				data[i + 2] = b;
			}
			break;
		default:
			return null;
		}
		BufferedImage image2 = new BufferedImage(cols, rows, type);
		image2.getRaster().setDataElements(0, 0, cols, rows, data);
		return image2;
	}

	BufferedImage getImage() {
		return image;
	}

	private void setImage(BufferedImage image) {
		this.image = image;
	}

}