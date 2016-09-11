/**
 * Created by Simone on 9/7/16.
 */
public class RegressionInput {
    private double[] regressand;
    private double[][] regressors;

    public double[] getRegressand() {
        return regressand;
    }

    public double[][] getRegressors() {
        return regressors;
    }

    public RegressionInput(double[] regressand, double[][] regressors) {
        this.regressand = regressand;
        this.regressors = regressors;
    }
}
