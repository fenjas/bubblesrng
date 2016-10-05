package bubblesRNG;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

public class camProc {

	private BufferedImage captImg;
	static BufferedImage convImg; 
	static Mat frame = new Mat();
	static VideoCapture camera1,camera2;
	
	// Save captured image to disk
	void saveFrame(BufferedImage img, String fName) {
		try {

			File out = new File(fName);
			ImageIO.write(img, "jpg", out);
			this.setImage(img);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	//Checks if a webcam is present and ready
	boolean isCameraPresent() {
		camera1 = new VideoCapture(0);
		boolean camOn = false;
		Mat cMat = new Mat();
		if (camera1.read(cMat)) {
			camOn = true;
			camera1.release();
		} else
			camOn = false;
		
		cMat.release();
		return camOn;
	}

	//Take a single picture
	void grabFrame() {
		camera2 = new VideoCapture(0);
		camera2.read(frame);

		if (!camera2.isOpened()) {
			System.out.println("Error. Camera not ready!");
		} else {
			if (camera2.read(frame)) {
			 captImg=matToBuffered(frame);
				
			}
		}
		camera2.release();
	}

	//Convert MAT to BufferedImage format
	BufferedImage matToBuffered(Mat mat) {
	    BufferedImage image = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_3BYTE_BGR);  
	     WritableRaster raster = image.getRaster();  
	     DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();  
	     byte[] data = dataBuffer.getData();  
	     mat.get(0, 0, data); 
	     mat.release();
	     return image;  
	}
	
	
	//Return Image
	BufferedImage getImage() {
		return captImg;
	}

	void setImage(BufferedImage image) {
		this.captImg = image;
	}
    
	//Garbage collection
	void nullAll(){
	    captImg=null;
		convImg=null;
		camera1=null;
		camera2=null;
		frame.release();
	}

}