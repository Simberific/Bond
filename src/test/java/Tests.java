/**
 * Created by Simone on 9/3/16.
 */

import static org.junit.Assert.*;
import org.junit.Test;

public class Tests {

    // We want to make sure that a business name with a Twitter profile ranks higher than a business name without a matching Twitter profile.
    @Test
    public void twitterTest(){
        TwitterDataSource twitterDataSource = new TwitterDataSource();
        Business businessHasProfile = new Business("SymbiNY", "Simone Kalmakis", "www.symbi.nyc", "New York, NY");
        double scoreBusinessHasProfile = twitterDataSource.getWeightedScore(businessHasProfile);
        Business businessNoProfile = new Business("SymbiNYC", "Simone Kalmakis", "www.symbi.nyc", "New York, NY");
        double scoreBusinessNoProfile = twitterDataSource.getWeightedScore(businessNoProfile);
        assertTrue(scoreBusinessHasProfile > scoreBusinessNoProfile);
    }

    // We want to make sure that a business name with a Yelp profile has a positive score.
    @Test
    public void yelpTest(){
        YelpDataSource yelpDataSource = new YelpDataSource();
        Business yelpBusiness = new Business("CrossFit NYC", "Hari Singh", "http://www.crossfitnyc.com", "New York, NY");
        double score = yelpDataSource.getWeightedScore(yelpBusiness);
        assertTrue(score > 0);
    }

    // We want to make sure that a known highly-ranked url has a defined positive ranking.
    @Test
    public void alexaTest(){
        AlexaDataSource alexaDataSource = new AlexaDataSource();
        Business alexaBusiness = new Business("Google", null, "www.google.com", "New York, NY");
        double score = alexaDataSource.getWeightedScore(alexaBusiness);
        assertTrue(score > 0);
    }
}
