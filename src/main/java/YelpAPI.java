/**
 * Created by Simone on 9/4/16.
 */

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * This class is based off of Yelp's sample code found here: https://github.com/Yelp/yelp-api/tree/master/v2/java.
 * I changed things like exception handling, changing from OAuthService to OAuth10aService, and returning specific fields from the query response.
 */
public class YelpAPI {
    private static final String API_HOST = "api.yelp.com";
    private static final int SEARCH_LIMIT = 3;
    private static final String SEARCH_PATH = "/v2/search";
    private static final String BUSINESS_PATH = "/v2/business";

    private OAuth10aService service;
    private OAuth1AccessToken accessToken;

    /**
     * Set up the Yelp API OAuth credentials.
     *
     */
    public YelpAPI() {
        this.service =
                new ServiceBuilder().
                        apiKey(Config.getYelpConsumerKey()).
                        apiSecret(Config.getYelpConsumerSecret()).
                        build(TwoStepOAuth.instance());
        this.accessToken = new OAuth1AccessToken(Config.getYelpToken(), Config.getYelpTokenSecret());
    }

    /**
     * Creates and sends a request to the Search API by term and location.
     * <p>
     * See <a href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
     * for more info.
     *
     * @param term <tt>String</tt> of the search term to be queried
     * @param location <tt>String</tt> of the location
     * @return <tt>String</tt> JSON Response
     */
    public String searchForBusinessesByLocation(String term, String location) throws IOException {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", term);
        request.addQuerystringParameter("location", location);
        request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and sends a request to the Business API by business ID.
     * <p>
     * See <a href="http://www.yelp.com/developers/documentation/v2/business">Yelp Business API V2</a>
     * for more info.
     *
     * @param businessID <tt>String</tt> business ID of the requested business
     * @return <tt>String</tt> JSON Response
     */
    public String searchByBusinessId(String businessID) throws IOException {
        OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/" + businessID);
        return sendRequestAndGetResponse(request);
    }
    /**
     * Creates and returns an {@link OAuthRequest} based on the API endpoint specified.
     *
     * @param path API endpoint to be queried
     * @return <tt>OAuthRequest</tt>
     */
    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://" + API_HOST + path, this.service);
        return request;
    }

    /**
     * Sends an {@link OAuthRequest} and returns the {@link Response} body.
     *
     * @param request {@link OAuthRequest} corresponding to the API request
     * @return <tt>String</tt> body of API response
     */
    private String sendRequestAndGetResponse(OAuthRequest request) throws IOException {
        System.out.println("Querying " + request.getCompleteUrl() + " ...");
        this.service.signRequest(this.accessToken, request);
        Response response = request.send();
        return response.getBody();
    }

    /**
     * Queries the Search API based on the command line arguments and takes the first result to query
     * the Business API.
     *
     * @param yelpApi <tt>YelpAPI</tt> service instance
     * @param queryTerm Term to search against
     * @param location Location of the business
     */
    public YelpFeatures queryAPI(YelpAPI yelpApi, String queryTerm, String location) throws IOException {
        String searchResponseJSON =
                yelpApi.searchForBusinessesByLocation(queryTerm, location);

        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            response = (JSONObject) parser.parse(searchResponseJSON);
        } catch (ParseException pe) {
            System.out.println("Error: could not parse JSON response:");
            System.out.println(searchResponseJSON);
            System.exit(1);
        }

        JSONArray businesses = (JSONArray) response.get("businesses");
        if (businesses.isEmpty()) {
            return new YelpFeatures(0, 0, false, false);
        }
        JSONObject firstBusiness = (JSONObject) businesses.get(0);
        String firstBusinessID = firstBusiness.get("id").toString();
        System.out.println(String.format(
                "%s businesses found, querying business info for the top result \"%s\" ...",
                businesses.size(), firstBusinessID));

        // Select the first business and display business details
        String businessResponseJSON = yelpApi.searchByBusinessId(firstBusinessID.toString());
        System.out.println(String.format("Result for business \"%s\" found:", firstBusinessID));
        System.out.println(businessResponseJSON);
        int reviewCount = Integer.parseInt(firstBusiness.get("review_count").toString());
        double rating = Double.parseDouble(firstBusiness.get("rating").toString());
        boolean isClaimed = Boolean.parseBoolean(firstBusiness.get("is_claimed").toString());
        boolean isOpen = !Boolean.parseBoolean(firstBusiness.get("is_closed").toString()); // Negating this boolean to avoid having a negative feature

        return new YelpFeatures(reviewCount, rating, isClaimed, isOpen);
    }
}
