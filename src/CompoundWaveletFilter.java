import ij.process.FloatProcessor;
import java.util.HashMap;

/**
 * This wavelet filter is implemented as an undecimated wavelet transform using
 * scaled B-spline of k-th order.
 *
 * This filter uses the separable kernel feature. Note that in the convolution
 * with the wavelet kernels we use padding twice to simulate {@code conv2}
 * function from Matlab to keep the results identical to the results we got in
 * Matlab version of ThunderSTORM.
 *
 * @see WaveletFilter
 * @see ConvolutionFilter
 */
public final class CompoundWaveletFilter{

    private FloatProcessor input = null, result = null, result_F1 = null, result_F2 = null;
    private HashMap<String, FloatProcessor> export_variables = null;
    private int margin;
    private int spline_order, spline_n_samples ;
    private double spline_scale;
    private WaveletFilter w1, w2;

    public CompoundWaveletFilter() {
        this(3, 2.0, 5);    // spline_order =3, spline_n_samples = 5,  spline_scale = 2.0
    }

    /**
     * Initialize the filter with all the wavelet kernels needed to create the
     * wavelet transform.
     */
    public CompoundWaveletFilter(int spline_order, double spline_scale, int spline_n_samples) {
        w1 = new WaveletFilter(1, spline_order, spline_scale, spline_n_samples, Padding.PADDING_ZERO);
        w2 = new WaveletFilter(2, spline_order, spline_scale, spline_n_samples, Padding.PADDING_ZERO);
        //
        this.margin = w2.getKernelX().getWidth() / 2;
        this.spline_n_samples = spline_n_samples;
        this.spline_order = spline_order;
        this.spline_scale = spline_scale;
    }

    public FloatProcessor filterImage(FloatProcessor image) {
        input = image;
        FloatProcessor padded = Padding.addBorder(image, Padding.PADDING_DUPLICATE, margin);
        FloatProcessor V1 = w1.filterImage(padded);
        FloatProcessor V2 = w2.filterImage(V1);

        result_F1 = ImageMath.subtract(input, ImageMath.crop(V1, margin, margin, image.getWidth(), image.getHeight()));
        result_F2 = ImageMath.subtract(input, ImageMath.crop(V2, margin, margin, image.getWidth(), image.getHeight()));
        result = ImageMath.crop(ImageMath.subtract(V1, V2), margin, margin, image.getWidth(), image.getHeight());
        
        return result;
    }

    public HashMap<String, FloatProcessor> exportVariables( ) {
        if(export_variables == null) {
            export_variables = new HashMap<String, FloatProcessor>();
        }
        //ÐÞ¸Ä
//        if(reevaluate) {
//            filterImage(Thresholder.getCurrentImage());
//        }
        //
        export_variables.put("I", input);
        export_variables.put("F", result);
        export_variables.put("F1", result_F1);
        export_variables.put("F2", result_F2);
        return export_variables;
    }
}
