package top.jyx365.organizationService;

import javax.naming.Name;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;
import org.springframework.ldap.support.LdapNameBuilder;

@Entry(objectClasses = {"organization","dcObject","top"})
public final class Company {
    @Id
    private Name id;

    @Attribute(name="o")
    private String name;

    @Attribute(name="dc")
    @DnAttribute(value="dc",index=0)
    private String domain;


    private String description;

    //public void setDn(String dn) {
        //this.dn = LdapNameBuilder.newInstance(dn).build();
    //}

    public Name getId() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
