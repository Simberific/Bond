/**
 * Created by Simone on 9/3/16.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * location: Location is specified by a particular neighborhood, address or city. More details here: https://www.yelp.com/developers/documentation/v2/search_api
 */

public class CreditScore {

    public static void main(String[] args) throws IOException {
        TwitterDataSource twitterDataSource = new TwitterDataSource();
        YelpDataSource yelpDataSource = new YelpDataSource();
        FacebookDataSource facebookDataSource = new FacebookDataSource();
        AlexaDataSource alexaDataSource = new AlexaDataSource();
        ModelBuilder modelBuilder = new ModelBuilder(twitterDataSource, yelpDataSource, alexaDataSource);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Hello! Would you like to (1) train the model or (2) get the credit score for a business? Please enter 1 or 2.");
        try {
            int userFlow = Integer.parseInt(br.readLine());
            if (userFlow == 1) {
                modelBuilder.trainModel();
            } else if (userFlow == 2) {
                System.out.println("Enter name of business, eg, `Symbi NY`");
                String businessName = br.readLine();
                System.out.println("Enter name of owner, eg, `Simone Kalmakis`");
                String ownerName = br.readLine();
                System.out.println("Enter Url of business, eg, `http://www.symbi.nyc`");
                String url = br.readLine();
                System.out.println("Enter location of business, eg, `New York, NY`");
                String location = br.readLine();

                Business business = new Business(businessName, ownerName, url, location);

                System.out.println(String.format("For (%s, %s, %s, %s): ", businessName, ownerName, url, location));

                double twitterScore = twitterDataSource.getWeightedScore(business);
                double yelpScore = yelpDataSource.getWeightedScore(business);
                double alexaScore = alexaDataSource.getWeightedScore(business);
                System.out.println("Twitter weighted score: " + twitterScore);
                System.out.println("Yelp weighted score: " + yelpScore);
                System.out.println("Alexa weighted score: " + alexaScore);
                double totalScore = ModelReader.getTotalScore(twitterScore, yelpScore, alexaScore);
                System.out.println("Total score: " + totalScore);
            } else {
                System.out.println("Input was neither a 1 nor a 2 - please try again.");
                main(args);
            }
        } catch (NumberFormatException e) {
            System.out.println("Input was neither a 1 nor a 2 - please try again.");
            main(args);
        }
    }
}
