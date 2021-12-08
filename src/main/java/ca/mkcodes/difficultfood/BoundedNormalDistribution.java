package ca.mkcodes.difficultfood;

import org.apache.commons.math3.distribution.NormalDistribution;

public class BoundedNormalDistribution {

    private final NormalDistribution n;

    private final int min;
    private final int max;

    public BoundedNormalDistribution(double mean, int min, int max) {
        double sigma = (max-min)/4.0;
        this.n = new NormalDistribution(mean, sigma);
        this.min = min;
        this.max = max;
    }

    public int sample(){
        double sample = n.sample();
        return (int) Math.max(this.min, Math.min(this.max, Math.round(sample)));
    }

    public double sampleRaw(){
        double sample = n.sample();
        return Math.max(this.min, Math.min(this.max, sample));
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
