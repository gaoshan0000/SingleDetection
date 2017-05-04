
public interface OneLocationFitter {

    public static class SubImage {

        public int[] xgrid;
        public int[] ygrid;
        public double[] values;
        public double detectorX;
        public double detectorY;
        public int size_y;
        public int size_x;

        public SubImage() {
        }

        public SubImage(int sizeX, int sizeY, int[] xgrid, int[] ygrid, double[] values, double detectorX, double detectorY) {
            this.size_x = sizeX;
            this.size_y = sizeY;
            this.xgrid = xgrid;
            this.ygrid = ygrid;
            this.values = values;
            this.detectorX = detectorX;
            this.detectorY = detectorY;
        }

        public double getMax() {
            return VectorMath.max(values);
        }

        public double getMin() {
            return VectorMath.min(values);
        }
        
        public double getSum() {
            return VectorMath.sum(values);
        }
        
        // note: the function changes the input array!
        public double [] subtract(double [] values) {
            assert(this.values.length == values.length);
            for(int i = 0; i < values.length; i++) {
                values[i] = this.values[i] - values[i];
            }
            return values;
        }
    }

    public Molecule fit(SubImage img);
}
