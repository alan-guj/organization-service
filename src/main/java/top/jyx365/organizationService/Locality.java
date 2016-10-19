package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.odm.annotations.Attribute;

@Entry(objectClasses = {"locality"})
public final class Locality {
    @Id
    @JsonIgnore
    private Name id;

    @Attribute(name="l")
    private String name;

    private String description;

    @Attribute(name="seeAlso")
    private String localityId;


    public Name getId() {
        return id;
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

    public void setLocalityId(String localityId) {
        this.localityId = localityId;
    }

    public String getLocalityId() {
        return localityId;
    }

    public void setCompany(Name company) {
        if(this.id == null)
            this.id = LdapNameBuilder.newInstance(company)
                .add("ou","localities")
                .add("l",this.name)
                .build();
    }

    public Name getCompany() {
        return id.getPrefix(1);
    }

    public void setParent(Name parent) {
        this.id = LdapNameBuilder.newInstance(parent)
            .add("l",this.name)
            .build();
    }

    public Name getParent() {
        return id.getPrefix(id.size()-1);
    }

}
