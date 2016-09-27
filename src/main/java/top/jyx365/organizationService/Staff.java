package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.naming.Name;
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

    @Attribute(name="ou")
    private Name department;

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

    public void setDepartment(String department) {
        this.department = LdapNameBuilder.newInstance(department).build();
    }

    public String getDepartment() {
        return department!=null?department.toString():null;
    }


    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }

    public void setCompany(Name company) {
        this.company = company;
    }

    public Name getCompany() {
        return company;
    }


    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
