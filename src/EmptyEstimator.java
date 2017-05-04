
import ij.process.FloatProcessor;
import java.util.Vector;
import javax.swing.JPanel;

/**
 * This estimator does no estimation, it just packs {@code Point}s found in
 * detection phase into {@code PSFModel} objects.
 *
 * This is suitable for quick preview of filtering/detection or for some basic
 * molecule counting applications where the pixel precision is not an issue.
 */
public class EmptyEstimator  {

    public Vector<Molecule> estimateParameters(FloatProcessor fp, Vector<Point> detections) {
        Vector<Molecule> locations = new Vector<Molecule>();

        for (Point detection : detections) {
            locations.add(new Molecule(new PSFModel.Params(new int[]{PSFModel.Params.X, PSFModel.Params.Y},
                    new double[]{detection.x.doubleValue()+0.5, detection.y.doubleValue()+0.5}, false)));
        }
        return locations;
    }
}
