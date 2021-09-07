package codercamp.com.earningapps.Model;

public class AmazonCardModel {
    private String id;
    private String amazonCode;

    public AmazonCardModel() {
    }

    public AmazonCardModel(String id, String amazonCode) {
        this.id = id;
        this.amazonCode = amazonCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAmazonCode() {
        return amazonCode;
    }

    public void setAmazonCode(String amazonCode) {
        this.amazonCode = amazonCode;
    }
}
