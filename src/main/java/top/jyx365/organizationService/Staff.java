package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;
import org.springframework.ldap.support.LdapNameBuilder;

@Entry(objectClasses = {"inetOrgPerson","organizationalPerson","Person"})
public final class Staff {
    @Id
    private Name id;

    @Attribute(name="cn")
    @DnAttribute(value="cn", index=2)
    private String name;

    @Attribute(name="sn")
    private String surname;

    private String description;


    @Attribute(name="ou")
    private List<Name> departments;

    @Attribute(name="mobile")
    private String mobile;

    @DnAttribute(value="dc", index=0)
    @Transient
    private @JsonIgnore String domain;

    @Attribute(name="o")
    private Name company;

    @DnAttribute(value="ou", index=1)
    @Transient
    private final String type = "staffs";

    public void setId(Name id) {
        this.id = id;
    }

    public Name getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSurname() {
        return surname;
    }

    public void setDepartments(List<Name> departments) {
        this.departments = departments;
    }

    public void addDepartment(Name department) {
        if(this.departments == null) this.departments = new ArrayList<Name>();
        this.departments.add(department);
    }

    public void removeDepartment(Name department) {
        if(this.departments != null) this.departments.remove(department);
    }

    public List<Name> getDepartments() {
        return departments;
    }


    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
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
