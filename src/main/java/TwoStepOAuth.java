import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1RequestToken;

/**
 * Simone: This class is from Yelp's sample code found here: https://github.com/Yelp/yelp-api/tree/master/v2/java.
 * With some additional massaging to get it to work.
 */

/*
 * Generic service provider for two-step OAuth10a.
 */
public class TwoStepOAuth extends DefaultApi10a {

    protected TwoStepOAuth() {
    }

    private static class InstanceHolder {
        private static final TwoStepOAuth INSTANCE = new TwoStepOAuth();
    }

    public static TwoStepOAuth instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return null;
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken oAuth1RequestToken) {
        return null;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return null;
    }
}
