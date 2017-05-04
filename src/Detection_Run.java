import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.SwingUtilities;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.SaveDialog;
import ij.measure.Measurements;
import ij.plugin.filter.PlugInFilter;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.FloatStatistics;
import ij.process.ImageProcessor;
import ij.gui.NewImage;
import ij.gui.Roi;

public class Detection_Run implements PlugInFilter {

	private ImagePlus window,filt_ip,det_ip;
	private ImageStack filter_stack,detection_stack; // use to save and show the filter image and detection image
	private ImageProcessor[] rawImg_pros;
	private FloatProcessor[] filtImg_pros;
	private FloatProcessor[] detImg_pros;
	private int nStack;  // the size of stack
	private int imgX,imgY; // the image size of x , y

	public int setup(String arg0, ImagePlus imp) {
		imgX=imp.getWidth();
		imgY=imp.getHeight();
		window = imp;
		nStack=window.getStackSize();
		filter_stack = window.createEmptyStack();
		detection_stack=window.createEmptyStack();
		rawImg_pros=new ImageProcessor[nStack];
		filtImg_pros=new FloatProcessor[nStack];
		detImg_pros=new FloatProcessor[nStack];
		for(int i=0;i<nStack;i++){
			rawImg_pros[i]=window.getStack().getProcessor(i+1);
			detImg_pros[i]=new FloatProcessor(imgX,imgY);
			detection_stack.addSlice(detImg_pros[i]);
		}
		
		det_ip=new ImagePlus("detect image",detection_stack);
		
		return ROI_REQUIRED + SUPPORTS_MASKING + STACK_REQUIRED + NO_CHANGES + DOES_16 + DOES_32 + DOES_8G;
	}

	public void run(ImageProcessor ip) {

		
/**************************write the detection data when the B-order is 3*******************************************/
/*****		File file_3=new File("B_order_3.txt");
		PrintStream ps_3 = null;
		try {
			ps_3 = new PrintStream(new FileOutputStream(file_3));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		**************/

		
/**************************write the detection data when the B-order is 3*******************************************/
		File file_4=new File("B_order_4.txt");
		PrintStream ps_4 = null;
		try {
			ps_4 = new PrintStream(new FileOutputStream(file_4));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < nStack; i++) {
			
/********************filter the image****************************************************************/
			CompoundWaveletFilter filter = new CompoundWaveletFilter(4,2.0,5);
			filtImg_pros[i]=filter.filterImage(rawImg_pros[i].convertToFloatProcessor());
			filter_stack.addSlice(filtImg_pros[i]);
			
			HashMap<String, FloatProcessor> filtResults=filter.exportVariables();
			FloatProcessor F1=filtResults.get("F1");
			
			// in paper the thresholdValue is from 0.5 to 2.0 times of the standard deviation of the noise image 
			float localThresholdValue = (float) (1.5*(FloatStatistics.getStatistics(F1, Measurements.STD_DEV, null)).stdDev);
			System.out.println(localThresholdValue);
			
/********************detect the image****************************************************************/
			LocalMaximaDetector detector = new LocalMaximaDetector();
			Vector<Point> detections = detector.detectMoleculeCandidates(filtImg_pros[i],localThresholdValue);
			
			ps_4.println("the frame"+i+" detections size is -->"+detections.size()+"\n");
			// draw detections in the image
			for(Point detection:detections){
				detImg_pros[i].setf(detection.x.intValue(), detection.y.intValue(), detection.val.floatValue());
			}

/********************fitting the image****************************************************************/
//			double default_sigma=1.6;
//			IntegratedSymmetricGaussianPSF psf=new IntegratedSymmetricGaussianPSF(default_sigma);
//			int fitradius=3;
			
			
/******************render the image   there is something wrong to be corrected*************************/
//			EmptyEstimator estimator=new EmptyEstimator();
//			List<Molecule> fits=estimator.estimateParameters(fp,detections);
//			AbstractRendering render=new AbstractRendering( window);
//			ImagePlus renderedImage=new ImagePlus("render image", image_pro);
//			if(fits.size()>0){
//				render.drawDetection(detections);
//				renderedImage=render.getRenderedImage();
//			}
		}

		filt_ip= new ImagePlus("filting image",filter_stack);
		
		filt_ip.show();
		det_ip.show();
	}

	public static void main(String[] args) {
		ImagePlus test_ip = new ImagePlus("G:/lab/workspace_x64/Simba_Plugin_x64/test_data/sequence.tif");
		Detection_Run dr = new Detection_Run();
		dr.setup("analysis", test_ip);
		dr.run(test_ip.getProcessor());
	}
}
