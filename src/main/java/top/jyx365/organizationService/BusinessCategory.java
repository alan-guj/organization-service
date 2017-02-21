package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;


@JsonPropertyOrder({"locality","product"})
public class BusinessCategory {

    private String product;
    private String locality;

    @JsonProperty("isOwner")
    private Boolean isOwner;

    public BusinessCategory() {

    }

    public BusinessCategory(String json) {
        this.product = JsonPath.read(json,"$.product");
        this.locality = JsonPath.read(json, "$.locality");
        try {
            this.isOwner = JsonPath.read(json, "$.isOwner");
        } catch (PathNotFoundException e) {
            System.err.println(e);
        }
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

    public Boolean isOwner() {
        return isOwner;
    }

    public void setIsOwner(Boolean isOwner) {
        this.isOwner = isOwner;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{\"locality\":\"").append(this.locality).append("\"")
                .append(",\"product\":\"").append(this.product).append("\"");
        if (null != isOwner && isOwner) buf.append(",\"isOwner\":true");
        buf.append("}");
        return buf.toString();
    }
}
