package bubblesRNG;

//Uses OpenCV and JavaCV (wrapper) libraries for image processing

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import java.util.ArrayList;

public class imgProc {

	//Arraylist for holding bubble co-ordinates
	private ArrayList<Integer> data = new ArrayList<Integer>();
	private int noOfBubblesFound = 0;

	IplImage srcImg=null; //Source Image
	IplImage gsImg=null;  //Gray scale image
	CvPoint2D32f c = null;
	CvPoint3D32f circle=null;
	CvPoint center=null;
	CvSeq circles=null;
	
	//Hough Transform vars
	private int method=CV_HOUGH_GRADIENT;
	private double minDist;		
	private int minRadius;	   	
	private int maxRadius;		
	private double dp;					
	private double param1;  			 
	private double param2; 				

	//CHT Default Settings
	void HoughDefaultSettings(){
		minDist=20;
		minRadius=1;
		maxRadius=13;
		dp=1;		
		param1=25;  
		param2=12; 	
	}

	//This method uses a CHT method to detect circles and extract the required data
	IplImage processImage(String srcImage,int [] HTParams, byte type){

		//Use default cht parameters or those set  by user 
		if (type==0) this.HoughDefaultSettings(); else
			if (type==1) {
				minDist=HTParams[0];	
				minRadius=HTParams[1];		
				maxRadius=HTParams[2];
				dp=HTParams[3];
				param1=HTParams[4];  		 
				param2=HTParams[5]; 			
			}	

		int radius=0;										//Circle radius.
		int totalCircles=0;									//Total number of circles (bubbles) found.
		int skipped=0;										//Circles whose centers fall out of range
		CvMemStorage mem = CvMemStorage.create();			//Memory storage containing the output sequence of found circles.
		srcImg  = cvLoadImage(srcImage); 		 			//Load source Image.
		gsImg   = IplImage.create(cvGetSize(srcImg),8,1);	//Create destination grayscale image.
	    
	    //Convert  image to grayscale
		cvCvtColor(srcImg, gsImg, CV_BGR2GRAY); 

		//Smoothen objects to facilitate detection 
		cvSmooth(gsImg, gsImg, CV_GAUSSIAN, 3,0,0,0); 

		//Call Hough transform method
		circles = cvHoughCircles(gsImg,mem,method,dp,minDist,param1,param2,minRadius,maxRadius);
		c = new CvPoint2D32f();
		
		//For each circle detected, retrieve and store its center coordinates
		for(totalCircles = 0; totalCircles < circles.total(); totalCircles++)
		{
			circle = new CvPoint3D32f(cvGetSeqElem(circles, totalCircles));
			c.put(circle.x(), circle.y()); //not documented ... found correct usage by trial and error

			center = cvPointFrom32f(c);
			radius = Math.round(circle.z());      

			//Populate array lists with bubble co-ordinates
			int r1 = (int) circle.x();
			int r2 = (int) circle.y();

			//Reduce bias by applying bounds -- x=1..512 and y=1..450
			if ( ((r1>63 && r1<576) && (r2>15 && r2<466)) ){
				this.data.add((r1-63) + (r2-15));
				cvCircle(srcImg, center, radius, CvScalar.GREEN, 2, CV_AA, 0);
			}
			else {
				skipped++;
				cvCircle(srcImg, center, radius, CvScalar.RED, 2, CV_AA, 0);
			}
		}

		//Return number of circular bubbles found
		this.setNoOfBubblesFound(totalCircles-skipped);
		
		//Release memory storage. Important!!
		mem.release();
	    
		//Return Image (IplImage)
		return srcImg;
	}

	void saveProcessedImage(IplImage procImg, String procimgFN){
		cvSaveImage(workSpace.workspace + procimgFN,procImg);
		cvReleaseImage(procImg);
	}
	
	//Garbage collection
	void nullAll(){
		c=null;
		circle=null;
		center=null;
		circles=null;
	    cvReleaseImage(srcImg); //IMPORTANT!!
		srcImg=null;
		gsImg=null;
	}
	
	
	//Setters and Getters
	int getNoOfBubblesFound() {
		return noOfBubblesFound;
	}

	void setNoOfBubblesFound(int noOfBubblesFound) {
		this.noOfBubblesFound = noOfBubblesFound;
	}

	ArrayList<Integer> getData() {
		return data;
	}
}
