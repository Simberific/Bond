/**
 * Created by Simone on 9/4/16.
 */

import java.io.IOException;
import java.util.logging.Logger;

public class YelpDataSource implements CreditScoreDataSource {
    private Logger logger;
    private YelpAPI yelpAPI;

    public YelpDataSource() {
        this.logger = Logger.getLogger(TwitterDataSource.class.getName());
        this.yelpAPI = new YelpAPI();
    }

    public double getWeightedScore(Business business) {
        try {
            YelpFeatures yelpFeatures = getYelpFeatures(business);
            return yelpFeatures.getRating() * ModelReader.readParameterFromFile(CoefficientNames.YELP_RATING) +
                    yelpFeatures.getReviewCount() *  ModelReader.readParameterFromFile(CoefficientNames.YELP_REVIEWS) +
                    yelpFeatures.getIsClaimed() *  ModelReader.readParameterFromFile(CoefficientNames.YELP_IS_CLAIMED) +
                    yelpFeatures.getIsOpen() *  ModelReader.readParameterFromFile(CoefficientNames.YELP_IS_OPEN);
        } catch (Exception e) {
            logger.warning("Unexpected exception, " + e.toString());
            return 0.0;
        }
    }

    public double[] getRawScores(Business business) {
        YelpFeatures yelpFeatures = getYelpFeatures(business);
        return new double[]{
                yelpFeatures.getRating(),
                yelpFeatures.getReviewCount(),
                yelpFeatures.getIsClaimed(),
                yelpFeatures.getIsOpen()
        };
    }

    private YelpFeatures getYelpFeatures(Business business) {
        try {
            return yelpAPI.queryAPI(yelpAPI, business.getBusinessName(), business.getLocation());
        } catch (IOException e) {
            logger.warning("IO Exception while trying to connect to Yelp API, " + e.toString());
            return new YelpFeatures(0, 0, false, false);

        }
    }
}
