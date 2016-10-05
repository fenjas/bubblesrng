package bubblesRNG;

import java.awt.EventQueue;

import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import org.bytedeco.javacpp.opencv_core.IplImage;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.swing.JTextField;
import java.awt.Font;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import java.awt.Color;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.JCheckBox;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class GUI extends JFrame{


	List<Integer> Data = new ArrayList<Integer>();     //Bubble coordinates
	List<Integer> rndData = new ArrayList<Integer>();  //Here we store our random numbers
	IplImage processedImg;                             //Processed image
	String workspacePath;	                           //Used to set workspace folder
	String rndDataFn="";	                           //Used to store filename of last exported random data file
	String entValues [];	                           //Array to hold values returned by the ENT utilty

	//Init class instances
	final camProc camProc = new camProc();
	final imgProc imgProc = new imgProc();

	//Filename declarations
	final String defaultWSPath = "c:/bubbles/";		   //Default workspace path
	final String entCSV  ="ENT_CSV";
	final String entFull ="ENT_Full";
	final String webcamgrabFN = "captImage.jpg";
	final String procimageFN="procImage.jpg";

	//Boolean vars for whitening parameters
	boolean wXor = true;
	boolean wEncrypt = true;
	boolean wShuffle = false;

	//Hough Transform
	final int hp1=20,hp2=1,hp3=10,hp4=1,hp5=30,hp6=13; //Hough transform default settings
	int[] HTParams = {hp1,hp2,hp3,hp4,hp5,hp6}; 	   //Array holding Hough parameters. We pass this to imageProcessor
	byte HoughSettingsType=0; 						   //Set default type to standard settings (User settings=1) 
	int totalNumofBubbles; 							   //Keeps track of the total number of bubbles detected
	int imgCaptureDelay=150; 						   //Wait time between each image capture (ms)
 
	//Misc
	boolean halt=false;								   //Use to exit Multigrab thread
	boolean isScrollingDownRequired = true;			   //Use to control scollpane scrolling behaviour
	int bytesToCapture=1048576;						   //Use to determine the number of bytes generated
	boolean showRndData=false;						   //Decide whether to show data in scrollpane (may slow down GUI)
	boolean showCtrData=false;						   //Decide whether to show data in scrollpane (may slow down GUI)


	//Program entry point
	//****************************************************************************************************************************
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					System.loadLibrary("opencv_java2411");	//OpenCV DLL library. Make sure it's reachable through %path%
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	//****************************************************************************************************************************

	//Constructor
	//****************************************************************************************************************************
	public GUI() {
		setType(Type.UTILITY);
		setLocationByPlatform(true);
		setResizable(false);

		workspacePath=defaultWSPath;
		workSpace.setPath(workspacePath); //Keep track of current workspace folder
		try {
			fileTools ft = new fileTools();
			ft.copyENTtoDisk(workspacePath); //Copy ent.exe to workspace folder
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		//JFrame
		setTitle("BubblesRNG: A Random Number Generator by Jason Fenech");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1056, 791);

		//JPanels
		final JPanel contentPane = new JPanel();
		final JPanel panelWebCam = new JPanel();
		final JPanel panelHoughTransform = new JPanel();
		final JPanel panelWorkSpace = new JPanel();
		final JPanel panelCommands = new JPanel();
		final JPanel panelData = new JPanel();
		final JPanel panelENT = new JPanel();

		//JButtons	
		final JButton btnHoughApply = new JButton("Apply");
		final JButton btnHoughDefaults = new JButton("Defaults");
		final JButton btnWSpacePathApply = new JButton("Apply");
		final JButton btnSingleGrab = new JButton("Single Grab");
		final JButton btnDetectBubbles = new JButton("Detect");		
		final JButton btnClearAll = new JButton("Clear All");
		final JButton btnLoadImage = new JButton("Load Image");
		final JButton btnMultiGrab = new JButton("Multiple Grab");
		final JButton btnExport = new JButton("Export");
		final JButton btnEntAnalysis = new JButton("Analyse");
		final JButton btnStop = new JButton("");
		btnStop.setEnabled(false);
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				halt=true;
			}
		});

		btnHoughApply.setBounds(110, 108, 106, 28);
		btnWSpacePathApply.setBounds(286, 27, 106, 28);
		btnSingleGrab.setBounds(10, 67, 106, 28);
		btnDetectBubbles.setBounds(133, 67, 106, 28);
		btnHoughDefaults.setBounds(227, 108, 106, 28);
		btnClearAll.setBounds(133, 108, 106, 28);
		btnLoadImage.setBounds(10, 108, 106, 28);
		btnMultiGrab.setBounds(10, 27, 106, 28);
		btnExport.setBounds(118, 300, 93, 28);
		btnEntAnalysis.setBounds(109, 202, 99, 28);
		btnStop.setBounds(212, 27, 27, 28);

		btnStop.setBackground(Color.ORANGE);
		btnStop.setDefaultCapable(false);
		btnStop.setBorder(null);
		btnStop.setBorderPainted(false);
		btnStop.setIcon(new ImageIcon(GUI.class.getResource("/javax/swing/plaf/metal/icons/ocean/paletteClose-pressed.gif")));

		btnHoughDefaults.setToolTipText("Set the Hough transform parameters to their default settings");
		btnDetectBubbles.setToolTipText("Apply Hough transform to the currently selected image. For Multi Grab, this is carried out automatically");
		btnHoughApply.setToolTipText("Apply the user defined Hough transform parameters");
		btnSingleGrab.setToolTipText("Grab a single image from the webcam (useful for calibration)");
		btnLoadImage.setToolTipText("Load an image from file (useful for experimentation)");
		btnExport.setToolTipText("Export the generated data to file");
		btnEntAnalysis.setToolTipText("Perform a statistical analysis on the generated data using the Ent utility");
		btnWSpacePathApply.setToolTipText("Set the default workspace path");
		btnClearAll.setToolTipText("Re-initialize text boxes and variables");
		btnMultiGrab.setToolTipText("Grab multiple images using the webcam and process them");
		btnStop.setToolTipText("Stop");

		//JSpinners (Hough Transform settings)
		final JSpinner spinnerCenters = new JSpinner();
		spinnerCenters.setModel(new SpinnerNumberModel(20, 1, 300, 1));
		spinnerCenters.setToolTipText("Minimum distance between the centers of the detected circles");
		spinnerCenters.setBounds(110, 20, 65, 28);
		panelHoughTransform.add(spinnerCenters);

		final JSpinner spinnerMinRad = new JSpinner();
		spinnerMinRad.setModel(new SpinnerNumberModel(1, 1, 300, 1));
		spinnerMinRad.setToolTipText("Minimum circle radius");
		spinnerMinRad.setBounds(110, 48, 65, 28);
		panelHoughTransform.add(spinnerMinRad);

		final JSpinner spinnerMaxRad = new JSpinner();
		spinnerMaxRad.setModel(new SpinnerNumberModel(10, 1, 300, 1));
		spinnerMaxRad.setToolTipText("Maximum circle radius");
		spinnerMaxRad.setBounds(110, 76, 65, 28);
		panelHoughTransform.add(spinnerMaxRad);

		final JSpinner spinnerAccum = new JSpinner();
		spinnerAccum.setModel(new SpinnerNumberModel(1, 1, 2, 1));
		spinnerAccum.setToolTipText("Inverse ratio of the accumulator resolution to the image resolution");
		spinnerAccum.setBounds(268, 20, 65, 28);
		panelHoughTransform.add(spinnerAccum);

		final JSpinner spinnerParam1 = new JSpinner();
		spinnerParam1.setModel(new SpinnerNumberModel(30, 1, 300, 1));
		spinnerParam1.setToolTipText("First method-specific parameter which is the higher threshold\r\nof the two passed to the Canny() edge detector (the lower one\r\nis twice smaller)");
		spinnerParam1.setBounds(268, 48, 65, 28);
		panelHoughTransform.add(spinnerParam1);

		final JSpinner spinnerParam2 = new JSpinner();
		spinnerParam2.setModel(new SpinnerNumberModel(13, 1, 300, 1));
		spinnerParam2.setToolTipText("Second method-specific parameter corresponding to the accumulator threshold\r\nfor the circle centers at the detection stage. " +
				"The smaller it is, the more false circles \r\nmay be detected. Circles, corresponding to the larger accumulator values, will be \r\nreturned first");
		spinnerParam2.setBounds(268, 76, 65, 28);
		panelHoughTransform.add(spinnerParam2);

		final JSpinner spinnerBytes = new JSpinner();
		spinnerBytes.setFont(new Font("Arial Narrow", Font.PLAIN, 12));
		spinnerBytes.setModel(new SpinnerNumberModel(new Integer(1048576), new Integer(1), null, new Integer(256)));
		spinnerBytes.setToolTipText("Set the number of bytes generated by run");
		spinnerBytes.setBounds(133, 27, 78, 28);
		panelCommands.add(spinnerBytes);

		JLabel lblBubbleDistance = new JLabel("Centers Dist.");
		lblBubbleDistance.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblBubbleDistance.setBounds(21, 27, 82, 14);
		panelHoughTransform.add(lblBubbleDistance);

		JLabel lblMinRadius = new JLabel("Min. Radius");
		lblMinRadius.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblMinRadius.setBounds(21, 55, 82, 14);
		panelHoughTransform.add(lblMinRadius);

		JLabel lblMaxRadius = new JLabel("Max. Radius");
		lblMaxRadius.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblMaxRadius.setBounds(21, 83, 82, 14);
		panelHoughTransform.add(lblMaxRadius);

		JLabel lblAccumulator = new JLabel("Accumulator");
		lblAccumulator.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblAccumulator.setBounds(190, 27, 81, 14);
		panelHoughTransform.add(lblAccumulator);

		JLabel lblParam = new JLabel("Param 1");
		lblParam.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblParam.setBounds(190, 55, 81, 14);
		panelHoughTransform.add(lblParam);

		JLabel lblParam_1 = new JLabel("Param 2");
		lblParam_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblParam_1.setBounds(190, 83, 81, 14);
		panelHoughTransform.add(lblParam_1);

		final JScrollPane scrollPaneStatus = new JScrollPane();
		scrollPaneStatus.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
		scrollPaneStatus.setAutoscrolls(true);
		scrollPaneStatus.setBounds(56, 60, 336, 75);
		panelWorkSpace.add(scrollPaneStatus);

		final JTextArea txtStatus = new JTextArea();
		txtStatus.setFont(new Font("Arial Narrow", Font.PLAIN, 12));
		txtStatus.setWrapStyleWord(true);
		txtStatus.setLineWrap(true);
		scrollPaneStatus.setViewportView(txtStatus);
		txtStatus.setEditable(false);

		JLabel lblWorkSpace = new JLabel("Path");
		lblWorkSpace.setBounds(11, 27, 42, 28);
		lblWorkSpace.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panelWorkSpace.add(lblWorkSpace);

		final JLabel lblStatus = new JLabel("Status");
		lblStatus.setBackground(Color.LIGHT_GRAY);
		lblStatus.setBounds(11, 78, 42, 28);
		panelWorkSpace.add(lblStatus);

		final JLabel lblwebcam =new JLabel();
		lblwebcam.setBorder(null);
		lblwebcam.setBackground(Color.BLACK);
		lblwebcam.setForeground(Color.BLACK);

		final JTextField txtWorkSpace = new JTextField();
		txtWorkSpace.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				btnWSpacePathApply.setBackground(Color.GREEN);
			}
		});
		txtWorkSpace.setText("c:\\bubbles");
		txtWorkSpace.setColumns(10);
		txtWorkSpace.setBounds(56, 27, 224, 28);
		panelWorkSpace.add(txtWorkSpace);

		//JScrollpanes and JTextPanes
		final JScrollPane scrollPaneCoordinates = new JScrollPane();
		scrollPaneCoordinates.setBounds(13, 121, 93, 157);
		panelData.add(scrollPaneCoordinates);

		final JScrollPane scrollPaneRndList = new JScrollPane();
		scrollPaneRndList.setBounds(118, 121, 93, 157);
		panelData.add(scrollPaneRndList);

		final JTextPane textPaneCoordinates = new JTextPane();
		textPaneCoordinates.setToolTipText("Sum of (x,y) center coordinates");
		textPaneCoordinates.setEditable(false);
		textPaneCoordinates.setLocation(10, 0);
		scrollPaneCoordinates.setViewportView(textPaneCoordinates);

		final JTextPane textPaneRndList = new JTextPane();
		textPaneRndList.setToolTipText("Generated random 8 bit integers {0..255}");
		textPaneRndList.setLocation(10, 0);
		scrollPaneRndList.setViewportView(textPaneRndList);

		//JLabels
		JLabel lblBubbles = new JLabel("Current Bubbles");
		lblBubbles.setBounds(13, 17, 87, 28);
		lblBubbles.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panelData.add(lblBubbles);

		JLabel lblRndData = new JLabel("f(x,y)");
		lblRndData.setForeground(Color.DARK_GRAY);
		lblRndData.setAutoscrolls(true);
		lblRndData.setBounds(13, 107, 73, 15);
		lblRndData.setFont(new Font("Tahoma", Font.BOLD, 12));
		panelData.add(lblRndData);

		JLabel lblRandomNumbersList = new JLabel("Rnd(f(x,y))");
		lblRandomNumbersList.setForeground(Color.DARK_GRAY);
		lblRandomNumbersList.setBounds(118, 107, 73, 15);
		lblRandomNumbersList.setFont(new Font("Tahoma", Font.BOLD, 12));
		panelData.add(lblRandomNumbersList);

		JLabel lblArithmeticMean = new JLabel("Arithmetic Mean");
		lblArithmeticMean.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblArithmeticMean.setBounds(13, 80, 100, 15);
		panelData.add(lblArithmeticMean);

		JLabel lblTotalBubbles = new JLabel("Total Bubbles");
		lblTotalBubbles.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblTotalBubbles.setBounds(13, 45, 87, 28);
		panelData.add(lblTotalBubbles);

		JLabel lblFileBytes = new JLabel("File Bytes");
		lblFileBytes.setBounds(14, 25, 100, 15);
		lblFileBytes.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panelENT.add(lblFileBytes);

		JLabel lblEntropy = new JLabel("Entropy");
		lblEntropy.setBounds(14, 55, 100, 15);
		lblEntropy.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panelENT.add(lblEntropy);

		JLabel lblChisquare = new JLabel("Chi-Square");
		lblChisquare.setBounds(14, 85, 100, 15);
		lblChisquare.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panelENT.add(lblChisquare);

		JLabel lblMean = new JLabel("Mean");
		lblMean.setBounds(14, 115, 100, 15);
		lblMean.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panelENT.add(lblMean);

		JLabel lblMontecarloPi = new JLabel("Monte-Carlo Pi");
		lblMontecarloPi.setBounds(14, 145, 100, 15);
		lblMontecarloPi.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panelENT.add(lblMontecarloPi);

		JLabel lblSerialCorrelation = new JLabel("Serial Correlation");
		lblSerialCorrelation.setBounds(14, 175, 100, 15);
		lblSerialCorrelation.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panelENT.add(lblSerialCorrelation);

		//JTextFields
		final JTextField txtBubbles = new JTextField();
		txtBubbles.setToolTipText("Bubbles detected in current image");
		txtBubbles.setEditable(false);
		txtBubbles.setText("0");
		txtBubbles.setForeground(Color.BLACK);
		txtBubbles.setBounds(112, 17, 99, 28);
		txtBubbles.setColumns(10);
		panelData.add(txtBubbles);

		final JTextField txtArithMean = new JTextField();
		txtArithMean.setToolTipText("Arithmetic average based on current image");
		txtArithMean.setEditable(false);
		txtArithMean.setText("0");
		txtArithMean.setForeground(Color.BLACK);
		txtArithMean.setColumns(10);
		txtArithMean.setBounds(112, 73, 99, 28);
		panelData.add(txtArithMean);

		final JTextField txtTotalBubbles = new JTextField();
		txtTotalBubbles.setToolTipText("Sum of bubbles detected so far");
		txtTotalBubbles.setEditable(false);
		txtTotalBubbles.setForeground(Color.BLACK);
		txtTotalBubbles.setText("0");
		txtTotalBubbles.setColumns(10);
		txtTotalBubbles.setBounds(112, 45, 99, 28);
		panelData.add(txtTotalBubbles);

		final JTextField txtFileBytes=new JTextField();
		txtFileBytes.setToolTipText("The number of bytes read from file");
		txtFileBytes.setBounds(109, 18, 99, 28);
		txtFileBytes.setEditable(false);
		txtFileBytes.setText("0");
		txtFileBytes.setForeground(Color.BLACK);
		txtFileBytes.setColumns(10);
		panelENT.add(txtFileBytes);

		final JTextField txtEntropy=new JTextField();
		txtEntropy.setToolTipText("The information density of the contents of the file, expressed as a number of bits per character");
		txtEntropy.setBounds(109, 48, 99, 28);
		txtEntropy.setEditable(false);
		txtEntropy.setText("0");
		txtEntropy.setForeground(Color.BLACK);
		txtEntropy.setColumns(10);
		panelENT.add(txtEntropy);

		final JTextField txtChiSquare=new JTextField();
		txtChiSquare.setToolTipText("http://en.wikipedia.org/wiki/Chi-squared_test");
		txtChiSquare.setBounds(109, 78, 99, 28);
		txtChiSquare.setEditable(false);
		txtChiSquare.setText("0");
		txtChiSquare.setForeground(Color.BLACK);
		txtChiSquare.setColumns(10);
		panelENT.add(txtChiSquare);

		final JTextField txtMean=new JTextField();
		txtMean.setToolTipText(" The sum of a sample of bubbles divided by the number of bubbles in the sample");
		txtMean.setBounds(109, 108, 99, 28);
		txtMean.setEditable(false);
		txtMean.setText("0");
		txtMean.setForeground(Color.BLACK);
		txtMean.setColumns(10);
		panelENT.add(txtMean);

		final JTextField txtMonteCarlo=new JTextField();
		txtMonteCarlo.setToolTipText("Given the probability, P, that an event will occur in certain conditions, the number of times the event occurs divided by the number of times the conditions are generated should be approximately equal to P");
		txtMonteCarlo.setBounds(109, 138, 99, 28);
		txtMonteCarlo.setEditable(false);
		txtMonteCarlo.setText("0");
		txtMonteCarlo.setForeground(Color.BLACK);
		txtMonteCarlo.setColumns(10);
		panelENT.add(txtMonteCarlo);

		final JTextField txtSerialCorrelation=new JTextField();
		txtSerialCorrelation.setToolTipText("This quantity measures the extent to which each byte in the file depends upon the previous byte");
		txtSerialCorrelation.setBounds(109, 168, 99, 28);
		txtSerialCorrelation.setEditable(false);
		txtSerialCorrelation.setText("0");
		txtSerialCorrelation.setForeground(Color.BLACK);
		txtSerialCorrelation.setColumns(10);
		panelENT.add(txtSerialCorrelation);


		//JCheckBoxes
		final JCheckBox cbXor = new JCheckBox("Xor");
		cbXor.setToolTipText("Carry out a XOR operation on the generated data");
		final JCheckBox cbEncrypt = new JCheckBox("Encrypt");
		cbEncrypt.setToolTipText("Encrypt the generated data");
		final JCheckBox cbshuffle = new JCheckBox("Shuffle");
		cbshuffle.setToolTipText("Shuffles the generated data before writing it to file");
		final JCheckBox cbshowCtrData = new JCheckBox("");
		cbshowCtrData.setToolTipText("Show");
		final JCheckBox cbshowRndData = new JCheckBox("");
		cbshowRndData.setToolTipText("Show");


		//JPanel settings
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);

		panelWorkSpace.setBorder(new TitledBorder(null, "Workspace", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(64, 64, 64)));
		panelWorkSpace.setBounds(639, 616, 410, 150);
		panelWorkSpace.setLayout(null);
		panelWebCam.setBorder(new TitledBorder(null, "Image Pane", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(64, 64, 64)));
		panelWebCam.setBounds(10, 10, 810, 606);
		panelHoughTransform.setToolTipText("Hough Transform Parameters");
		panelHoughTransform.setBorder(new TitledBorder(null, "Hough Transform Parameters", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(64, 64, 64)));
		panelHoughTransform.setBounds(278, 616, 350, 150);
		panelHoughTransform.setLayout(null);
		panelHoughTransform.setFont(new Font("Tahoma", Font.PLAIN, 11));
		panelCommands.setBorder(new TitledBorder(null, "Actions", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(64, 64, 64)));
		panelCommands.setBounds(10, 616, 258, 150);
		panelCommands.setLayout(null);
		panelCommands.add(btnStop);
		panelData.setBorder(new TitledBorder(null, "Data", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(64, 64, 64)));
		panelData.setBounds(826, 10, 223, 359);
		panelData.setLayout(null);
		panelENT.setBounds(826, 369, 222, 247);
		panelENT.setBorder(new TitledBorder(null, "Ent Results", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panelENT.setLayout(null);
		panelENT.add(btnEntAnalysis);

		//JCheckBoxes		
		cbXor.setSelected(true);
		cbEncrypt.setSelected(true);
		cbXor.setBounds(13, 284, 40, 18);
		cbEncrypt.setBounds(13, 305, 62, 18);
		cbshuffle.setBounds(13, 325, 60, 18);
		cbshowCtrData.setBounds(87, 105, 18, 18);
		cbshowRndData.setBounds(193, 105, 18, 18);
		panelData.add(cbshowCtrData);
		panelData.add(cbshuffle);
		panelData.add(cbEncrypt);
		panelData.add(cbXor);
		panelData.add(cbshowRndData);

		GridBagLayout gbl_panelWebCam = new GridBagLayout();
		gbl_panelWebCam.columnWidths = new int[]{782, 0};
		gbl_panelWebCam.rowHeights = new int[]{588, 0};
		gbl_panelWebCam.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panelWebCam.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelWebCam.setLayout(gbl_panelWebCam);
		GridBagConstraints gbc_lblwebcam = new GridBagConstraints();
		gbc_lblwebcam.gridx = 0;
		gbc_lblwebcam.gridy = 0;
		panelWebCam.add(lblwebcam, gbc_lblwebcam);

		panelCommands.add(btnClearAll);
		panelCommands.add(btnLoadImage);
		panelCommands.add(btnDetectBubbles);
		panelCommands.add(btnMultiGrab);
		panelCommands.add(btnSingleGrab);
		panelWorkSpace.add(btnWSpacePathApply);
		panelHoughTransform.add(btnHoughDefaults);
		panelHoughTransform.add(btnHoughApply);
		panelData.add(btnExport);

		contentPane.add(panelENT);
		contentPane.add(panelCommands);
		contentPane.add(panelHoughTransform);
		contentPane.add(panelWorkSpace);
		contentPane.add(panelWebCam);
		contentPane.add(panelData);		

		//Inner Classes
		//****************************************************************************************************************************

		//Update captured image on screen;
		class imageUpdate{
			public void go(){
				ImageIcon icon = new ImageIcon(camProc.getImage());
				lblwebcam.setIcon(icon);
				panelWebCam.revalidate();
				panelWebCam.repaint();
				icon.getImage().flush();
			}
		}

		//Analyse image		
		class analyseImage{

			String curr;
			Integer p=0, noBubs=0;
			int i=0;
			int rndValue=0;

			public void go(String fn) throws NoSuchPaddingException, NoSuchAlgorithmException, Exception{

				File GrabbedFile = new File(fn);

				//Clear previous results
				textPaneCoordinates.setText("");
				textPaneRndList.setText("");

				 //Check file exists just in case user change default folder after grab
				if (GrabbedFile.exists())
				{
					//Process Image and save it
					processedImg=imgProc.processImage(fn,HTParams,HoughSettingsType);
					imgProc.saveProcessedImage(processedImg,procimageFN);

					txtStatus.append("Processing image ...\n");

					//Display processed image in webcam panel
					ImageIcon icon = new ImageIcon(workspacePath+procimageFN);
					lblwebcam.setIcon(icon);
					panelWebCam.revalidate();
					panelWebCam.repaint();
					icon.getImage().flush();

					//Generate arraylist of coordinates. These will be piped into the functions class
					Data=imgProc.getData();

					//Shuffle the list if the option is selected by the user 
					if (wShuffle) {
						long seed = System.nanoTime();
						Collections.shuffle(Data,new Random(seed));
					}

					//Extract coordinates from currently processed image and call random generator
					for (i=0; i<Data.size();i++)
					{
						p = Data.get(i);
						rndValue = functions.generateRnd(p, wXor, wEncrypt); 
						rndData.add(rndValue);

						//Update GUI components
						if (showCtrData) {
							curr=textPaneCoordinates.getText();
							//textPaneCoordinates.setText(curr  + p.toString() + "," + q.toString() + "\n");}
							textPaneCoordinates.setText(curr  + p.toString() + "\n");}

						if (showRndData) {
							curr=textPaneRndList.getText();
							textPaneRndList.setText(curr  + rndValue + "\n");}
					}

					//Display no. of bubbles found in current image
					noBubs = imgProc.getNoOfBubblesFound();

					if (noBubs==null || noBubs==0) {
						noBubs=0;
						textPaneCoordinates.setText("No data acquired!");
						textPaneRndList.setText("No data acquired!");
						txtBubbles.setText("0");
					};
					txtBubbles.setText(Integer.toString(noBubs));

				}

				//Increment global counter to keep count of total number of detected bubbles
				totalNumofBubbles=totalNumofBubbles+noBubs;

				//Clear to avoid appending previous runs to rndData
				Data.clear();

				//Update GUI components
				txtArithMean.setText(functions.meanValue(rndData));
				txtTotalBubbles.setText(Integer.toString(totalNumofBubbles));
				txtStatus.append("Image processed\n");
				btnDetectBubbles.setBackground(null);
			}
		}

		//Continuous image capture
		class multiGrab implements Runnable{  

			@Override
			public void run() {
				//Start timing
				long startTime = System.nanoTime();

				//Counter for no. of images grabbed
				int i=0; 

				//Create class instance
				analyseImage ai = new analyseImage();

				//Start grabbing images ...
				while (totalNumofBubbles<bytesToCapture && !halt){ //Stop if bytes total match that set by user or user press stop button	
					camProc.grabFrame(1);
					camProc.saveFrame(camProc.getImage(), workspacePath+webcamgrabFN);

					txtStatus.append("Grabbed image no. " + (i+1) + "\n");

					try {
						//Analyse image ....
						ai.go(workspacePath+webcamgrabFN);
					} catch (NoSuchPaddingException e1) {
						e1.printStackTrace();
					} catch (NoSuchAlgorithmException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						e1.printStackTrace();
					}

					try {
						//Time interval between captured images. Also prevents i/o errors when writing to disk.
						Thread.sleep(imgCaptureDelay); 
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					i++;
				}

				//Re-enable GUI components
				btnSingleGrab.setEnabled(true);
				btnDetectBubbles.setEnabled(true);
				btnLoadImage.setEnabled(true);
				btnMultiGrab.setEnabled(true);
				btnWSpacePathApply.setEnabled(true);
				btnExport.setEnabled(true);				
				btnExport.setBackground(Color.GREEN);
				btnClearAll.setEnabled(true);
				spinnerBytes.setEnabled(true);
				btnStop.setEnabled(false);
				cbshowRndData.setEnabled(true);
				cbshowCtrData.setEnabled(true);
				halt=false;
				isScrollingDownRequired = false; 

				//Return Arithmetic Mean
				if (rndData.size()!=0) txtArithMean.setText(functions.meanValue(rndData));

				//Return total time taken for execution
				long endTime = System.nanoTime();
				txtStatus.append("Time taken : " +  (endTime - startTime) / 1000000000 + " secs\n");
				txtStatus.append("Process interrupted by user or byte upperbound reached\n");

				//Export automatically
				try {
					Thread.sleep(250);
					btnExport.doClick();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}



				//Analyse automatically
				try {
					Thread.sleep(250);
					btnEntAnalysis.doClick();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		//Clear arraylists, counters and panes
		class reInit{
			public void go(){
				textPaneCoordinates.setText("");
				textPaneRndList.setText("");
				txtBubbles.setText("");
				txtTotalBubbles.setText("");
				txtArithMean.setText("");
				txtStatus.setText("");
				txtFileBytes.setText("");
				txtEntropy.setText("");
				txtChiSquare.setText("");
				txtMean.setText("");
				txtMonteCarlo.setText("");
				txtSerialCorrelation.setText("");
				btnHoughApply.setBackground(null);
				btnEntAnalysis.setBackground(null);
				btnExport.setBackground(null);
				Data.clear();
				rndData.clear();
				totalNumofBubbles=0;
				bytesToCapture=(int) spinnerBytes.getValue();
			}
		}


		//Action Listeners
		//****************************************************************************************************************************

		//Launch ent statistical tool if present
		btnEntAnalysis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				//Get workspace path
				String ws=workSpace.workspace;

				//Check if the ENT executable is present
				File ent = new File(ws+"ent.exe");

				if (ent.exists() && !ent.isDirectory() && !rndDataFn.isEmpty())
				{
					try {
						String fSuffix=fileTools.writeEntReport(ws, rndDataFn, entCSV, entFull);

						Thread.sleep(1000); //Allow 1s for the file to be flushed to disk

						//Parse CSV value from ENT results file 
						entValues=fileTools.parseCSV(ws + entCSV+fSuffix);

						txtStatus.append("Analysing results ...\n");

						//Display values obtained from ENT results file 
						txtFileBytes.setText(entValues[0]);
						txtEntropy.setText(entValues[1]);
						txtChiSquare.setText(entValues[2]);
						txtMean.setText(entValues[3]);
						txtMonteCarlo.setText(entValues[4]);
						txtSerialCorrelation.setText(entValues[5]);

						btnEntAnalysis.setBackground(null);

					} catch (Exception e1) {

						e1.printStackTrace();
					}


				} else 	txtStatus.append("Ent.exe not found or no data has been written to file\n");
			}	
		});


		//Clear all 
		btnClearAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reInit a = new reInit();
				a.go();
			}
		});

		//Write random numbers arraylist to file
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rndData.size()!=0) {
					try{

						StringBuilder options = new StringBuilder("---");

						if (wXor) options.setCharAt(0, 'X'); 
						if (wEncrypt) options.setCharAt(1, 'E');
						if (wShuffle) options.setCharAt(2, 'S');

						//Refresh static var otherwise it keeps returning same date/time
						new fileTools();
						fileTools.d=new Date(); 
						rndDataFn=fileTools.writeToBinFile(workspacePath, rndData, bytesToCapture,wShuffle, options.toString());

						//Generate sample random data files using java prngs for comparison
						//fileTools.genSecRnd(workspacePath,bytesToCapture);
						//fileTools.genStdRnd(workspacePath,bytesToCapture);

						txtStatus.setText(txtStatus.getText() + "File exported to " + rndDataFn + "\n");
						btnEntAnalysis.setBackground(Color.GREEN);
						btnExport.setBackground(null);
					}
					catch (Exception ex){
						txtStatus.setText(txtStatus.getText() + "File export failed due to " + ex.getMessage() + "\n");
					}
				} else txtStatus.setText(txtStatus.getText() + "Nothing to export!\n");
			}
		});

		//Grab single image
		btnSingleGrab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Check for default folder
				workSpace.setPath(workspacePath);

				//Enable scrollpane auto scrolling
				isScrollingDownRequired = true; 

				if (camProc.isCameraPresent()){
					camProc.grabFrame(1);
					camProc.saveFrame(camProc.getImage(), workspacePath+webcamgrabFN);
					imageUpdate imu = new imageUpdate();
					imu.go();
					txtStatus.append("Grabbed " + workspacePath+webcamgrabFN + "\n");
					btnDetectBubbles.setBackground(Color.GREEN);
				}
				else {txtStatus.append("No camera detected!\n");};

				//Disable auto scrolling
				isScrollingDownRequired = false;
			}
		});

		//Grab multiple images and analyse
		btnMultiGrab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				//Instantiate classes - webcamStream is run in its own thread.
				reInit clear = new reInit();
				Thread t = new Thread(new multiGrab());

				if (camProc.isCameraPresent()){	
					//Clear all and get workspace path
					clear.go();
					workSpace.setPath(workspacePath); 

					//Disable GUI components
					btnSingleGrab.setEnabled(false);
					btnDetectBubbles.setEnabled(false);
					btnLoadImage.setEnabled(false);
					btnMultiGrab.setEnabled(false);
					btnWSpacePathApply.setEnabled(false);
					btnExport.setEnabled(false);
					btnClearAll.setEnabled(false);
					spinnerBytes.setEnabled(false);
					cbshowRndData.setEnabled(false);
					cbshowCtrData.setEnabled(false);
					btnStop.setEnabled(true);
					isScrollingDownRequired = true;

					//Start separate thread	
					t.setName("multiGrabThread");
					t.start();
				}
				else
					txtStatus.append("No camera detected!\n");
			}
		});

		//Analyse images using Hough Transform
		btnDetectBubbles.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				workSpace.setPath(workspacePath); 
				analyseImage ai = new analyseImage();
				try {
					ai.go(workspacePath+webcamgrabFN);
				} catch (NoSuchPaddingException e1) {
					e1.printStackTrace();
				} catch (NoSuchAlgorithmException e1) {
					e1.printStackTrace();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				//Return current Arithmetic Mean
				txtArithMean.setText(functions.meanValue(rndData));
				bytesToCapture=totalNumofBubbles;
			}});


		//Use defaults Hough transform settings
		btnHoughDefaults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HoughSettingsType=0;

				//Use default settings, reset spinner values and apply
				int[] HTDefParams = {hp1,hp2,hp3,hp4,hp5,hp6};
				spinnerCenters.setValue(HTDefParams[0]);
				spinnerMinRad.setValue(HTDefParams[1]);
				spinnerMaxRad.setValue(HTDefParams[2]);
				spinnerAccum.setValue(HTDefParams[3]);
				spinnerParam1.setValue(HTDefParams[4]);
				spinnerParam2.setValue(HTDefParams[5]);

				btnHoughApply.setBackground(null);
				btnHoughApply.doClick();
			}
		});


		//Apply user defined Hough transform settings
		btnHoughApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HoughSettingsType=1;
				HTParams[0]=(int) spinnerCenters.getValue();
				HTParams[1]=(int) spinnerMinRad.getValue();
				HTParams[2]=(int) spinnerMaxRad.getValue();
				HTParams[3]=(int) spinnerAccum.getValue();
				HTParams[4]=(int) spinnerParam1.getValue();
				HTParams[5]=(int) spinnerParam2.getValue();

				btnHoughApply.setBackground(null);
			}
		});


		//Set workspace path
		btnWSpacePathApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String temp=txtWorkSpace.getText();

				//Append a slash so content is saved to the new directory
				if (!temp.endsWith("/")) temp=temp+"/";
				if (temp!="") {
					workSpace.setPath(temp);
					workspacePath=workSpace.workspace;
				};

				btnWSpacePathApply.setBackground(null);

				//Copy ent.exe to path
				try {
					fileTools ft = new fileTools();
					ft.copyENTtoDisk(workspacePath);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});


		//File Dialog allowing user to set workspace folder of choice
		final JFileChooser fileChooser = new JFileChooser();
		btnLoadImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Thread queryThread = new Thread() {
					public void run(){
						fileChooser.setCurrentDirectory(new File(workspacePath));
						int result = fileChooser.showOpenDialog(panelCommands);

						if (result == JFileChooser.APPROVE_OPTION) {
							File selectedFile = fileChooser.getSelectedFile().getAbsoluteFile();

							//Return path and replace slashes in path string to avoid exception error
							String absPath = selectedFile.getAbsolutePath().replaceAll("\\\\","/"); 

							try {	
								BufferedImage srcImg = ImageIO.read(new File(absPath));

								//Get width and height of image
								final int w=800,h=600;
								int imageWidth  = srcImg.getWidth();
								int imageHeight = srcImg.getHeight();

								//Scale image to fit in the 800x600 label / pane
								double scaleX = (double)w/imageWidth;
								double scaleY = (double)h/imageHeight;
								AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
								AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);

								//Save scaled image to disk
								camProc.saveFrame(bilinearScaleOp.filter(srcImg,  new BufferedImage(w, h, srcImg.getType())), workspacePath+webcamgrabFN);

								//Display
								ImageIcon icon = new ImageIcon(camProc.getImage());
								lblwebcam.setIcon(icon);
								panelWebCam.revalidate();
								panelWebCam.repaint();
								icon.getImage().flush();

								txtStatus.append("Image " + absPath + " loaded\n");

							}
							catch (IOException ex) {ex.getMessage();}  
						}
					}
				};
				queryThread.start();
			}
		});


		//Enable scrollpane autoscrolling
		scrollPaneStatus.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
			public void adjustmentValueChanged(AdjustmentEvent e) {  
				if(isScrollingDownRequired) {
					e.getAdjustable().setValue(e.getAdjustable().getMaximum());
				}  
			}
		});

		scrollPaneCoordinates.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
			public void adjustmentValueChanged(AdjustmentEvent e) {  
				if(isScrollingDownRequired) {
					e.getAdjustable().setValue(e.getAdjustable().getMaximum());}  
			}
		});

		scrollPaneRndList.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
			public void adjustmentValueChanged(AdjustmentEvent e) {  
				if(isScrollingDownRequired) {
					e.getAdjustable().setValue(e.getAdjustable().getMaximum());}  
			}
		}); 

		spinnerMinRad.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {

				//Make sure max circle radius is always larger then the min radius to avoid exception errors
				int min = (int) spinnerMinRad.getValue();
				int max = (int) spinnerMaxRad.getValue();
				if (min==max || min > max) {spinnerMinRad.setValue(max-1);};

				btnHoughApply.setBackground(Color.GREEN);
			}
		});
		spinnerMaxRad.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {

				//Make sure max circle radius is always larger then the min radius to avoid exception errors
				int min = (int) spinnerMinRad.getValue();
				int max = (int) spinnerMaxRad.getValue();
				if (min==max || max < min) {spinnerMaxRad.setValue(min+1);};
				btnHoughApply.setBackground(Color.GREEN);
			}
		});

		//Change the "Apply" button colour to remind user to apply new settings
		spinnerCenters.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				btnHoughApply.setBackground(Color.GREEN);
			}
		});

		spinnerAccum.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				btnHoughApply.setBackground(Color.GREEN);
			}
		});
		spinnerParam1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				btnHoughApply.setBackground(Color.GREEN);
			}
		});
		spinnerParam2.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				btnHoughApply.setBackground(Color.GREEN);
			}
		});

		//No. of bytes to capture before halting
		spinnerBytes.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				bytesToCapture=(int) spinnerBytes.getValue();
			}
		});

		//JCheckBoxes Listeners
		cbXor.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange()==ItemEvent.SELECTED) wXor=true; else wXor=false;
			}
		});

		cbEncrypt.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange()==ItemEvent.SELECTED) wEncrypt=true; else wEncrypt=false;
			}
		});

		cbshuffle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange()==ItemEvent.SELECTED) wShuffle=true; else wShuffle=false;
			}
		});

		//Display data in scrollpanes 
		cbshowCtrData.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange()==ItemEvent.SELECTED) showCtrData=true; else showCtrData=false;
			}
		});

		cbshowRndData.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange()==ItemEvent.SELECTED) showRndData=true; else showRndData=false;}
		});
	}
}

