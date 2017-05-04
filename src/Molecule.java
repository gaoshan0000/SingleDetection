import ij.IJ;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;



public final class Molecule {

//    public MoleculeDescriptor descriptor;
    private List<Molecule> detections;    // for merging of re-appearing molecules (post-processing)
    public List<Molecule> neighbors;    // for molecule matching (performance evaluation)
    public double[] values;

    public Molecule(PSFModel.Params params) {
        assert(params.hasParam(PSFModel.Params.X) && params.hasParam(PSFModel.Params.Y));
        
//        this.descriptor = new MoleculeDescriptor(params);
        this.values = new double[params.values.length];
        for(int i = 0; i < params.values.length; i++) {
            values[i]=params.values[i];
        }
    }
  
    @Override
    public Molecule clone() {
        assert(true) : "Use clone(MoleculeDescriptor) instead!!!";
        throw new UnsupportedOperationException("Use `Molecule.clone(MoleculeDescriptor)` instead!!!");
    }
    
    /**
     * Clone the molecule.
     * 
     * Caller has to duplicate the descriptor if it is required!
     */
   
    
    

    public void addDetection(Molecule mol) {
        if(detections == null){
            detections = new Vector<Molecule>();
        }
        if(mol.isSingleMolecule()) {
            detections.add(mol);
        } else {    // if it is not empty, it already contains, at least, itself
            for(Molecule m : mol.detections) {
                detections.add(m);
            }
        }
    }

    public List<Molecule> getDetections() {
        return detections;
    }

    public void setDetections(Vector<Molecule> detections) {
        this.detections = detections;
    }
    
    public int getDetectionsCount(){
        return detections == null  ? 1: detections.size();
    }

    public boolean isSingleMolecule() {
        return (detections == null || detections.size() <= 1);
    }

   
    
    // ================================================================
    //       Ground-truth testing
    // ================================================================
    
    private DetectionStatus status = DetectionStatus.UNSPECIFIED;
    
    public void setStatus(DetectionStatus status) {
        this.status = status;
    }
    
    public DetectionStatus getStatus() {
        return status;
    }

    public static enum DetectionStatus {
        UNSPECIFIED, TRUE_POSITIVE, FALSE_POSITIVE, FALSE_NEGATIVE;
    }

}
