package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.jayway.jsonpath.JsonPath;


@JsonPropertyOrder({"locality","product"})
public class BusinessCategory {

    private String product;
    private String locality;

    private boolean isOwner = false;

    public BusinessCategory() {

    }

    public BusinessCategory(String json) {
        this.product = JsonPath.read(json,"$.product");
        this.locality = JsonPath.read(json, "$.locality");
        this.isOwner = JsonPath.read(json, "$.isOwner");
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getLocality() {
        return locality;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getProduct() {
        return product;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{\"locality\":\"").append(this.locality).append("\"")
                .append(",\"product\":\"").append(this.product).append("\"");
        if (isOwner) buf.append(",\"isOwner\":true");
        buf.append("}");
        return buf.toString();
    }
}
