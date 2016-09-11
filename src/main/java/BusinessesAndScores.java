/**
 * Created by Simone on 9/7/16.
 */

import java.util.List;

public class BusinessesAndScores {
    private List<Business> businesses;
    private List<Double> regressands;

    public List<Business> getBusinesses() {
        return businesses;
    }

    public List<Double> getRegressands() {
        return regressands;
    }

    public BusinessesAndScores( List<Business> businesses, List<Double> regressands) {
        this.businesses = businesses;
        this.regressands = regressands;
    }
}
