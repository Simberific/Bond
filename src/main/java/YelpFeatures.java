/**
 * Created by Simone on 9/11/16.
 */

public class YelpFeatures {
    private int reviewCount;
    private double rating;
    private boolean isClaimed;
    private boolean isOpen;

    public YelpFeatures(int reviewCount, double rating, boolean isClaimed, boolean isOpen) {
        this.reviewCount = reviewCount;
        this.rating = rating;
        this.isClaimed = isClaimed;
        this.isOpen = isOpen;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public double getRating() {
        return rating;
    }

    // We convert these into binomial features.
    public int getIsClaimed() {
        return isClaimed ? 1 : 0;
    }

    public int getIsOpen() {
        return isOpen ? 1 : 0;
    }
}