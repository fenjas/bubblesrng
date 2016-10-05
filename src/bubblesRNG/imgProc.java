package bubblesRNG;

//Using OpenCV and JavaCV (wrapper) libraries for image processing

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.util.ArrayList;

public class imgProc {

	private ArrayList<Integer> data = new ArrayList<Integer>(); //Arraylist for holding bubble co-ordinates
	private int noOfBubblesFound = 0; //Number of bubbles found by Hough transform


	//Hough Transform vars
	private int method=CV_HOUGH_GRADIENT;
	private double minDist;		
	private int minRadius;	   	
	private int maxRadius;		
	private double dp;					
	private double param1;  			 
	private double param2; 				

	void HoughDefaultSettings(){
		minDist=20;
		minRadius=1;
		maxRadius=10;
		dp=1;		
		param1=30;  
		param2=13; 	
	}

	IplImage processImage(String srcImage,int [] HTParams, byte type){

		if (type==0) {this.HoughDefaultSettings();} else
			if (type==1) {
				minDist=HTParams[0];	
				minRadius=HTParams[1];		
				maxRadius=HTParams[2];
				dp=HTParams[3];
				param1=HTParams[4];  		 
				param2=HTParams[5]; 			
			}	

		//Misc definitions
		int radius=0;												//Circle radius.
		int totalCircles=0;											//Total number of circles (bubbles) found.
		int skipped=0;												//Circles whose centers fall out of range

		CvMemStorage mem = CvMemStorage.create();					//Memory storage containing the output sequence of found circles.
		IplImage srcImg  = cvLoadImage(srcImage); 		 			//Load source Image.
		IplImage gsImg   = cvCreateImage(cvGetSize(srcImg),8,1);	//Create destination image.

		//Convert  image from one color space to another
		cvCvtColor(srcImg, gsImg, CV_BGR2GRAY); 

		//Smoothen objects to facilitate detection 
		cvSmooth(gsImg, gsImg, CV_GAUSSIAN, 3,0,0,0); 

		//Call Hough transform method
		CvSeq circles = cvHoughCircles(gsImg,mem,method,dp,minDist,param1,param2,minRadius,maxRadius);

		CvPoint2D32f c = new CvPoint2D32f();

		//For each circle detected, retrieve and store its center coordinates
		for(totalCircles = 0; totalCircles < circles.total(); totalCircles++)
		{
			CvPoint3D32f circle = new CvPoint3D32f(cvGetSeqElem(circles, totalCircles));
			c.put(circle.x(), circle.y()); //not documented ... found correct usage by trial and error

			CvPoint center = cvPointFrom32f(c);
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
		mem.release();

		//Return Image (IplImage)
		return srcImg;
	}

	void saveProcessedImage(IplImage procImg, String procimgFN){
		cvSaveImage(workSpace.workspace + procimgFN,procImg);
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
