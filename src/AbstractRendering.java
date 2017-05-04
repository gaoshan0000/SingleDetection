
import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;
import java.awt.Color;
import java.util.Arrays;
import java.util.Vector;

/**
 * A common abstract superclass implementing RenderingMethod and
 * IncrementalRenderingMethod. You can override the single abstract method
 * drawPoint to quickly add another Rendering Method.
 */
public  class AbstractRendering{

    protected double xmin, xmax, ymin, ymax;
    protected double resolution;
    protected int imSizeX, imSizeY;
    protected double defaultDX;
    protected double defaultDZ;
    protected boolean forceDefaultDX;
    protected boolean forceDefaultDZ;
    protected double zFrom, zTo, zStep;
    protected int zSlices;
    protected boolean threeDimensions;
    protected boolean colorizeZ;
    protected ImageProcessor[] slices;
    protected ImagePlus image;
    private ImageStack stack;


    protected int shifts = 2;
    protected int zShifts = 1;
    /**
     * A class for creating objects of sublasses of AbstractRendering.
     */
   
    protected AbstractRendering(ImagePlus imgPlus) {
        this.xmin = 0;
        this.ymin = 0;
        this.resolution = 80;
        this.forceDefaultDX = true;
        this.forceDefaultDZ = true;
        this.defaultDX = 0.2;
        this.defaultDZ = 5;
        this.zFrom = Double.NEGATIVE_INFINITY;
        this.zSlices=imgPlus.getStackSize();
        this.zStep =  Double.POSITIVE_INFINITY;
        this.zTo =  Double.POSITIVE_INFINITY;
        this.threeDimensions = false;
        this.colorizeZ = false;
        this.imSizeX=imgPlus.getProcessor().getWidth();
        this.imSizeY=imgPlus.getProcessor().getHeight();
        slices = new ImageProcessor[zSlices];
        stack = imgPlus.createEmptyStack();
        for(int i = 0; i < zSlices; i++) {
            slices[i] = new FloatProcessor(imSizeX,imSizeY);
            stack.addSlice(slices[i]);
        }
        image = new ImagePlus("render stack", stack);
        Calibration calibration = new Calibration();
        double pixelSize = resolution * 80.0 / 1000;
        calibration.pixelHeight = pixelSize;
        calibration.pixelWidth = pixelSize;
        if(threeDimensions) {
            calibration.pixelDepth = zStep / 1000;
        }
        calibration.setUnit("um");
        image.setCalibration(calibration);
        if(colorizeZ) {
            image.setDimensions(zSlices, 1, 1);
            CompositeImage image2 = new CompositeImage(image);
            image = image2;
            setupLuts();
        }
    }

    public void addToImage(double[] x, double[] y, double[] z, double[] dx, double[] dz) {
        for(int i = 0; i < x.length; i++) {
            double zVal = z != null ? z[i] : 0;
            double dxVal = dx != null && !forceDefaultDX ? dx[i] : defaultDX;
            double dzVal = dz != null && !forceDefaultDZ ? dz[i] : defaultDZ;
            drawPoint(x[i], y[i], zVal, dxVal, dzVal);
        }
    }
    
    public void drawDetection(Vector<Point> detections) {
        if(detections.isEmpty()) {
            return;
        }
       
        // use defaultDX and defaultDZ   zVal=0.0
        for(int i = 0, im = detections.size(); i < im; i++) {
            Point p = detections.elementAt(i);
            double zVal = 0.0;
            double dxVal = defaultDX;
            double dzVal = defaultDZ;
            //
            drawPoint(p.getX().doubleValue(), p.getY().doubleValue(), zVal, dxVal, dzVal);
        }
    }
    

    public ImagePlus getRenderedImage() {
        return image;
    }

    public ImagePlus getRenderedImage(double[] x, double[] y, double[] z, double[] dx, double[] dz) {
        reset();
        addToImage(x, y, z, dx, dz);
        return getRenderedImage();
    }

    protected void drawPoint(double x, double y, double z, double dx, double dz) {
        if (isInBounds(x, y)) {
            int u = (int) ((x - xmin) / resolution);
            int v = (int) ((y - ymin) / resolution);
            int w = 0;
            if (threeDimensions) {
                if (z == zTo) {
                    w = zSlices - 1;
                } else {
                    w = ((int) ((z - zFrom) / zStep));
                }
            }
            for (int k = -zShifts + 1; k < zShifts; k++) {
                if (w + k < zSlices && w + k >= 0) {
                    ImageProcessor img = slices[w + k];
                    for (int i = -shifts + 1; i < shifts; i++) {
                        for (int j = -shifts + 1; j < shifts; j++) {
                            if (u + i < imSizeX && u + i >= 0 && v + j < imSizeY && v + j >= 0) {
                                img.setf(u + i, v + j, img.getf(u + i, v + j) + (shifts - Math.abs(i)) * (shifts - Math.abs(j)) * (zShifts - Math.abs(k)));
                            }
                        }
                    }
                }
            }
        }
    }

    public void reset() {
        for(int i = 0; i < slices.length; i++) {
            float[] px = (float[]) slices[i].getPixels();
            Arrays.fill(px, 0);
        }
        stack = new ImageStack(imSizeX, imSizeY);
        for(int i = 0; i < zSlices; i++) {
            stack.addSlice((i * zStep + zFrom) + " to " + ((i + 1) * zStep + zFrom), slices[i]);
        }
        image.setStack(stack);
        setupLuts();
    }

    protected boolean isInBounds(double x, double y) {
        return x >= xmin && x < xmax && y >= ymin && y < ymax && !Double.isNaN(x) && !Double.isNaN(y);
    }

    private void setupLuts() {
        if(image.isComposite()) {
            CompositeImage image2 = (CompositeImage) image;
            LUT[] channeLuts = new LUT[zSlices];
            for(int i = 0; i < channeLuts.length; i++) {
                //Colormap for slices: (has constant grayscale intensity, unlike jet and similar)
                //r:      /
                //     __/
                //g:    /\
                //     /  \
                //b:   \
                //      \__
                float norm = (float) i / zSlices;
                float r, g, b;
                if(norm < 0.5) {
                    b = 1 - 2 * norm;
                    g = 2 * norm;
                    r = 0;
                } else {
                    b = 0;
                    g = -2 * norm + 2;
                    r = 2 * norm - 1;
                }
                channeLuts[i] = LUT.createLutFromColor(new Color(r, g, b));
            }
            image2.setLuts(channeLuts);
        }
    }
}
