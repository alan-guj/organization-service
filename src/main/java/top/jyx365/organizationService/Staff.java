package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.jayway.jsonpath.JsonPath;

import java.util.ArrayList;
import java.util.List;
import javax.naming.Name;
import javax.naming.ldap.LdapName;

import javax.validation.constraints.NotNull;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;
import org.springframework.ldap.support.LdapNameBuilder;


@Entry(objectClasses = {"inetOrgPerson","organizationalPerson","Person","uidObject"})
@JsonIgnoreProperties(ignoreUnknown=true)
public final class Staff {
    @Id
    @JsonIgnore
    private Name id;

    @Attribute(name="uid")
    private String uid;

    private String employeeNumber;

    @Attribute(name="seeAlso")
    private Name relatedStaff;

    @Attribute(name="businessCategory")
    private List <BusinessCategory> businessCategories;

    @Attribute(name="cn")
    @DnAttribute(value="cn", index=2)
    @NotNull
    private String name;

    @NotNull
    @Attribute(name="sn")
    private String surname;

    private String description;


    @Attribute(name="ou")
    private List<Name> departments;

    @Attribute(name="mobile")
    private String mobile;

    @DnAttribute(value="dc", index=0)
    @Transient
    @JsonIgnore
    private String domain;

    @Attribute(name="o")
    @JsonIgnore
    private Name company;

    @Attribute(name="mail")
    private String email;

    @DnAttribute(value="ou", index=1)
    @Attribute(name="employeeType")
    @JsonIgnore
    private String type;

    public Staff() {
        this.uid = "null";
        this.type = "staffs";
    }


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

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

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

    public void removeBusinessCategory(String businessCategory) {
        if(this.businessCategories != null)
            this.businessCategories.remove(businessCategory);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setRelatedStaff(Name relatedStaff) {
        this.relatedStaff = relatedStaff;
    }

    public Name getRelatedStaff() {
        return relatedStaff;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
