package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.naming.Name;
import javax.naming.ldap.LdapName;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

@Entry(objectClasses= {"document"})
@JsonIgnoreProperties(ignoreUnknown=true)
public final class Product {
    @Id
    @JsonIgnore
    private Name id;

    @Attribute(name="documentIdentifier")
    private String productId="null";

    @Attribute(name="cn")
    @DnAttribute(value="cn", index=2)
    private String name;

    private String description;

    @DnAttribute(value="dc", index=0)
    @Transient
    @JsonIgnore
    private String domain;

    @Attribute(name="o")
    @JsonIgnore
    private Name company;

    @DnAttribute(value="ou", index=1)
    @Transient
    @JsonIgnore
    private final String type = "products";

    public Name getId() {
        return id;
    }

    public void setProductId(String productId) {
        this.productId = productId==null?"null":productId;
    }

    public String getProductId() {
        return productId.equals("null")?null:productId;
    }

    public void setName(String name) {
        this.name= name;
    }

    public String getName() {
        return name;
    }

    public void setCompany(Name company) {
        this.company = company;
        this.domain = (String)((LdapName)company).getRdn(0).getValue();
    }

    public Name getCompany() {
        return company;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
