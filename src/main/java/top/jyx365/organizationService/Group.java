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

@Entry(objectClasses = {"groupOfNames"})
public final class Group {

    @Transient
    private final Name DEFAULT_MEMBER_ID = LdapNameBuilder.newInstance("cn=nobody").build();

    @Id
    @JsonIgnore private Name id;

    @Attribute(name="description")
    private String description;

    @Attribute(name="cn")
    @DnAttribute(value="cn", index=2)
    private String name;

    @Attribute(name="member")
    private List<Name> members = new ArrayList<Name>();

    @DnAttribute(value="dc", index=0)
    @Transient
    private @JsonIgnore String domain;

    @Attribute(name="o")
    @JsonIgnore
    private Name company;

    @DnAttribute(value="ou", index=1)
    @Transient
    private final String type = "groups";

    @Attribute(name="businessCategory")
    private List <String> businessCategories;


    public void setBusinessCategories(List<String> businessCategories) {
        this.businessCategories = businessCategories;
    }

    public List<String> getBusinessCategories() {
        return businessCategories;
    }

    public void addBusinessCategory(String businessCategory) {
        if(this.businessCategories == null)
            this.businessCategories = new ArrayList<String>();
        this.businessCategories.add(businessCategory);
    }

    public void removeBusinessCategory(String businessCategory) {
        if(this.businessCategories != null)
            this.businessCategories.remove(businessCategory);
    }


    public Group() {
        this.members.add(DEFAULT_MEMBER_ID);
    }

    public void setId(Name id) {
        this.id = id;
    }

    public Name getId() {
        return id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setMembers(List<Name> members) {
        this.members = members;
    }

    public List<Name> getMembers() {
        List<Name> ret = new ArrayList<Name>(this.members);
        ret.remove(DEFAULT_MEMBER_ID);
        return ret;
    }

    public void addMember(Name member) {
        if(this.members == null) this.members = new ArrayList<Name>();
        this.members.add(member);
    }

    public void removeMember(Name member) {
        if(this.members != null) this.members.remove(member);
    }
}
