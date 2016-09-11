/**
 * Created by Simone on 9/4/16.
 */

import facebook4j.*;
import facebook4j.auth.AccessToken;

import java.util.logging.Logger;

public class FacebookDataSource implements CreditScoreDataSource {
    private Facebook facebook;
    private Logger logger;

    private static final String NO_FACEBOOK_PROFILE_FOUND = "NO_PAGE_FOUND";

    public FacebookDataSource() {
        this.facebook = new FacebookFactory().getInstance();
        this.logger = Logger.getLogger(FacebookDataSource.class.getName());
    }

    public double getWeightedScore(Business business) {
        double[] rawScores = getRawScores(business);
        return rawScores[0] * ModelReader.readParameterFromFile(CoefficientNames.FACEBOOK_CHECKINS) +
                rawScores[1] * ModelReader.readParameterFromFile(CoefficientNames.FACEBOOK_FANS) +
                rawScores[2] * ModelReader.readParameterFromFile(CoefficientNames.FACEBOOK_LIKES) +
                rawScores[3] * ModelReader.readParameterFromFile(CoefficientNames.FACEBOOK_FRIENDS);
    }

    public double[] getRawScores(Business business) {
        try {
            // Facebook access token.
            // Note that I didn't implement a web app for the user to log in to Facebook and receive the code in a callback, which is used to generate a user access token, so this won't work.
            String oauthCode = "placeholder";
            AccessToken accessToken = facebook.getOAuthAccessToken(oauthCode);
            facebook.setOAuthAccessToken(accessToken);

            String userIdBiz = businessNameToId(business.getBusinessName(), business.getUrl());
            String userIdOwner = userNameToId(business.getOwnerName());
            double[] score = new double[4];
            if (!userIdBiz.equals(NO_FACEBOOK_PROFILE_FOUND)) {
                PageCounts pageCounts = pageCounts(userIdBiz);
                score[0] = pageCounts.checkins;
                score[1] = pageCounts.fans;
                score[2] = pageCounts.likes;
            }
            if (!userIdOwner.equals(NO_FACEBOOK_PROFILE_FOUND)) {
                score[3] = friendsCount(userIdOwner);
            }
            return score;
        } catch (Exception e) {
            logger.warning("Unexpected exception, " + e.toString());
            return new double[4];
        }
    }

    private String businessNameToId(String businessName, String url) {
        try {
            ResponseList<Page> pages = facebook.search().searchPages(businessName);
            for (Page page : pages) {
                if (page.getWebsite() != null && page.getWebsite().equals(url)) {
                    return page.getId();
                }
            }
            if (pages.size() != 0) {
                // If no matching url is found, return the top hit.
                return pages.get(0).getId();
            } else {
                logger.info("No Facebook page found for " + businessName);
                return NO_FACEBOOK_PROFILE_FOUND;
            }
        } catch (FacebookException e) {
            logger.warning("Exception trying to contact Facebook API, " + e.toString());
            return NO_FACEBOOK_PROFILE_FOUND;
        }
    }

    private String userNameToId(String userName) {
        try {
            ResponseList<User> users = facebook.search().searchUsers(userName);
            if (users.size() != 0) {
                // If no matching url is found, return the top hit.
                return users.get(0).getId();
            } else {
                logger.info("No Facebook user found for " + userName);
                return NO_FACEBOOK_PROFILE_FOUND;
            }
        } catch (FacebookException e) {
            logger.warning("Exception trying to contact Facebook API, " + e.toString());
            return NO_FACEBOOK_PROFILE_FOUND;
        }
    }

    // Owner's number of friends. This can indicate the owner's influence and network.
    private double friendsCount(String userId) {
        try {
            int friendsCount = facebook.friends().getFriends(userId).getSummary().getTotalCount();
            logScore("friendsCount", friendsCount, userId);
            return friendsCount;
        } catch (FacebookException e) {
            logger.warning("Exception trying to contact Facebook API, " + e.toString());
            return 0;
        }
    }

    // User engagement with the business's Facebook page.
    private PageCounts pageCounts (String pageId) {
        try {
            Page page = facebook.pages().getPage(pageId);
            int checkins = page.getCheckins();
            int fans = page.getFanCount();
            int likes = page.getLikes();
            logScore("checkins", checkins, pageId);
            logScore("fans", fans, pageId);
            logScore("likes", likes, pageId);

            return new PageCounts(
                    checkins,
                    fans,
                    likes);
        } catch (FacebookException e) {
            logger.warning("Exception trying to contact Facebook API, " + e.toString());
            return new PageCounts(0, 0, 0);
        }
    }

    private class PageCounts {
        private double checkins;
        private double fans;
        private double likes;

        private PageCounts(double checkins, double fans, double likes) {
            this.checkins = checkins;
            this.fans = fans;
            this.likes = likes;
        }
    }

    private void logScore(String countName, int count, String userId) {
        logger.info(String.format("%s has a score of %d for userId %s", countName, count, userId));
    }
}
