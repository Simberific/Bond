/**
 * Created by Simone on 9/7/16.
 */
public class Business {
    private String businessName;
    private String ownerName;
    private String url;
    private String location;

    public String getBusinessName() {
        return businessName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getUrl() {
        return url;
    }

    public String getLocation() {
        return location;
    }

    public Business(String businessName, String ownerName, String url, String location) {
        this.businessName = businessName;
        this.ownerName = ownerName;
        this.url = url;
        this.location = location;
    }
}
