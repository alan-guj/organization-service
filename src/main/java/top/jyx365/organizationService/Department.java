package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.naming.Name;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;
import org.springframework.ldap.support.LdapNameBuilder;

@Entry(objectClasses = {"organizationalUnit"})
public final class Department {
    @Id
    @JsonIgnore private Name id;


    @Attribute(name="ou")
    private String name;
    private String description;

    //@Attribute(name="st")
    //private List<Name> provinces;

    @Attribute(name="businessCategory")
    private List <BusinessCategory> businessCategories;


    public void setBusinessCategories(List<BusinessCategory> businessCategories) {
        this.businessCategories = businessCategories;
    }

    public List<BusinessCategory> getBusinessCategories() {
        return businessCategories;
    }

    public void addBusinessCategory(BusinessCategory businessCategory) {
        if(this.businessCategories == null)
            this.businessCategories = new ArrayList<BusinessCategory>();
        this.businessCategories.add(businessCategory);
    }

    public void removeBusinessCategory(BusinessCategory businessCategory) {
        if(this.businessCategories != null)
            this.businessCategories.remove(businessCategory);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Name getId() {
        return id;
    }

    public void setCompany(Name company) {
        if(this.id == null)
            this.id = LdapNameBuilder.newInstance(company)
                .add("ou","departments")
                .add("ou",this.name)
                .build();
    }

    public Name getCompany() {
        return (id==null)?null:id.getPrefix(1);
    }

    public void setParent(Name parent) {
        this.id = LdapNameBuilder.newInstance(parent)
            .add("ou",this.name)
            .build();
    }

    public Name getParent() {
        return (id==null)?null:id.getPrefix(id.size()-1);
    }
}
