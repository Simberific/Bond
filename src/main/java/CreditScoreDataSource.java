/**
 * Created by Simone on 9/4/16.
 */

public interface CreditScoreDataSource {
    // The weighted score is the linear combination of the raw feature values and the weights of each feature.
    double getWeightedScore(Business business);
    // The raw score is the set of feature scores for each feature from this data source.
    double[] getRawScores(Business business);
}
