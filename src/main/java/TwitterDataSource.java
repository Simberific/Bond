/**
 * Created by Simone on 9/3/16.
 */

import twitter4j.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TwitterDataSource implements CreditScoreDataSource {
    private twitter4j.Twitter twitter;
    private Logger logger;

    private static final int NO_TWITTER_USER_FOUND = -2;

    public TwitterDataSource() {
        this.twitter = TwitterFactory.getSingleton();
        this.logger = Logger.getLogger(TwitterDataSource.class.getName());
    }

    public double getWeightedScore(Business business) {
        double[] rawScores = getRawScores(business);
        return rawScores[0] * ModelReader.readParameterFromFile(CoefficientNames.TWITTER_FRIENDS) +
                rawScores[1] * ModelReader.readParameterFromFile(CoefficientNames.TWITTER_FAVORITES) +
                rawScores[2] * ModelReader.readParameterFromFile(CoefficientNames.TWITTER_FOLLOWERS) +
                rawScores[3] * ModelReader.readParameterFromFile(CoefficientNames.TWITTER_STATUSES) +
                rawScores[4] * ModelReader.readParameterFromFile(CoefficientNames.TWITTER_RECENT_STATUS_NUM) +
                rawScores[5] * ModelReader.readParameterFromFile(CoefficientNames.TWITTER_RECENT_STATUS_FAVORITES) +
                rawScores[6] * ModelReader.readParameterFromFile(CoefficientNames.TWITTER_RECENT_STATUS_RETWEETS);
    }

    public double[] getRawScores(Business business) {
        try {
            long userIdBiz = nameToId(business.getBusinessName(), business.getUrl(), business.getLocation());
            long userIdOwner = nameToId(business.getOwnerName(), null, business.getLocation());
            List<Long> userIds = new ArrayList<Long>();
            if (userIdBiz != NO_TWITTER_USER_FOUND) {
                userIds.add(userIdBiz);
            }
            if (userIdOwner != NO_TWITTER_USER_FOUND) {
                userIds.add(userIdOwner);
            }
            double[] scores = new double[7];
            for (Long userId : userIds) {
                RecentStatusCounts recentStatusCounts = recentStatusCounts(userId);
                scores[0] += friendsCount(userId);
                scores[1] += favoritesCount(userId);
                scores[2] += followersCount(userId);
                scores[3] += statusesCount(userId);
                scores[4] += recentStatusCounts.recentStatuses;
                scores[5] += recentStatusCounts.favorites;
                scores[6] += recentStatusCounts.retweets;
            }
            return scores;
        } catch (Exception e) {
            logger.warning("Unexpected exception, " + e.toString());
            return new double[7];
        }
    }

    /** Looks through the top 100 Twitter users that match this name.
     * If we find one with a matching URL to the user-inputted URL, we stop. If we find none, we return the top match.
     *
     * @param userName User-inputted name of owner or business.
     * @param url The url to check each returned user against; null if not applicable.
     * @return
     */
    private long nameToId(String userName, String url, String location) throws Exception {
        try {

            List<User> topUsers = twitter.users().searchUsers(userName, 1);
            if (topUsers.size() == 0) {
                // No matching users have been found.
                return NO_TWITTER_USER_FOUND;
            }

            // If there's a URL provided, we want to use the URL as an additional criteria since name matching can be noisy.
            if (url != null) {
                User locationMatch = null;
                List<User> users = topUsers;
                int page = 1;
                while (users.size() > 0) {
                    for (User user : users) {
                        URLEntity urlEntity = user.getURLEntity();
                        if (url.equals(user.getURL()) ||
                                    (urlEntity != null && url.equals(urlEntity.getExpandedURL()))) {
                            // If we get a url match, then the user in question is the one we want.
                            return user.getId();
                        }
                        // Location can be a signal but a less definitive one than url.
                        // Therefore we keep track of the first user with a matching location and try to use that if no url is found.
                        if (locationMatch == null && user.getLocation() != null && user.getLocation().equals(location)) {
                            locationMatch = user;
                        }
                    }
                    if (users.size() == 20 && page < 5) {
                        // Then there may be more users
                        page++;
                        users = twitter.users().searchUsers(userName, page);
                    } else {
                        break;
                    }
                }
                if (locationMatch != null) {
                    return locationMatch.getId();
                }
            }
            // If there's no url provided or if we haven't found a user with a matching url, return the top hit.
            return topUsers.get(0).getId();

        } catch (TwitterException e){
            logger.warning("Exception trying to contact Twitter API, " + e.toString());
            return NO_TWITTER_USER_FOUND;
        }
    }

    /** Returns the number of users the user follows
     *
     * @param userId
     * @return
     */
    private double friendsCount(long userId) {
        try {
            int count = twitter.users().showUser(userId).getFriendsCount();
            logScore("friendsCount", count, userId);
            return count;
        } catch (TwitterException e){
            logger.warning("Exception trying to contact Twitter API, " + e.toString());
            return 0;
        }
    }

    private double followersCount(long userId) {
        try {
            int count = twitter.users().showUser(userId).getFollowersCount();
            logScore("followersCount", count, userId);
            return count;
        } catch (TwitterException e){
            logger.warning("Exception trying to contact Twitter API, " + e.toString());
            return 0;
        }
    }

    private double statusesCount(long userId) {
        try {
            int count = twitter.users().showUser(userId).getStatusesCount();
            logScore("statusesCount", count, userId);
            return count;
        } catch (TwitterException e){
            logger.warning("Exception trying to contact Twitter API, " + e.toString());
            return 0;
        }
    }

    // User activity metrics: these methods capture how active the user/business is on Twitter.

    /** Score based upon the number of statuses the business has favorited.
     *  This may signal the business's level of engagement with its clientele.
     *  We only check up to 100 favorites, so we give an extra bump if the business has reached that many (since there
     *  could be many more).
     *
     * @param userId
     * @return
     */
    private double favoritesCount(long userId) {
        try {
            Paging paging = new Paging(1, 100);
            List<Status> statuses = twitter.favorites().getFavorites(userId, paging);
            int count = (statuses.size() < 100) ? statuses.size() : 200;
            logScore("favoritesCount", count, userId);
            return count;
        } catch (TwitterException e){
            logger.warning("Exception trying to contact Twitter API, " + e.toString());
            return 0;
        }
    }

    /** This score captures a business/owner's recent activity: it is based upon the number of statuses the user has posted in the last 24 hours.
     * This method only checks up to 20 statuses, so we give an extra bump if the user has reached that many
     * (since there could be many more).
     *
     * @param userId
     * @return
     */
    private RecentStatusCounts recentStatusCounts(long userId) {
        try {
            List<Status> statuses = twitter.getUserTimeline(userId);

            int numStatuses = 0;
            int favoriteCount = 0;
            int retweetCount = 0;
            numStatuses = (statuses.size() < 20) ? statuses.size() : 40;

            // Loop through the statuses to count the number of favoritesCount and retweets among them.
            for (Status status: statuses) {
                favoriteCount += status.getFavoriteCount();
                retweetCount += status.getRetweetCount();
            }
            logScore("numStatuses", numStatuses, userId);
            logScore("favoriteCount", favoriteCount, userId);
            logScore("retweetCount", retweetCount, userId);

            return new RecentStatusCounts(
                    numStatuses,
                    favoriteCount,
                    retweetCount);
        } catch (TwitterException e){
            logger.warning("Exception trying to contact Twitter API, " + e.toString());
            return new RecentStatusCounts(0, 0, 0);
        }
    }

    private class RecentStatusCounts {
        private double recentStatuses;
        private double favorites;
        private double retweets;

        public RecentStatusCounts(double numStatuses, double favoriteCount, double retweetCount) {
            this.recentStatuses = numStatuses;
            this.favorites = favoriteCount;
            this.retweets = retweetCount;
        }
    }

    private void logScore(String countName, int count, long userId) {
        logger.info(String.format("%s has a score of %d for userId %d", countName, count, userId));
    }
}
