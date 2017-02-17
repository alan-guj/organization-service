package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.jayway.jsonpath.JsonPath;


@JsonPropertyOrder({"locality","product"})
public class BusinessCategory {

    private String product;
    private String locality;

    public BusinessCategory() {

    }

    public BusinessCategory(String json) {
        this.product = JsonPath.read(json,"$.product");
        this.locality = JsonPath.read(json, "$.locality");
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

    public String toString() {
        return "{\"locality\":\""+this.locality+"\","+
            "\"product\":\""+this.product+"\"}";
    }
}
