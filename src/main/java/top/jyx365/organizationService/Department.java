package top.jyx365.organizationService;

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
    private Name id;


    @Attribute(name="ou")
    private String name;

    @Attribute(name="o")
    private Name company;

    private String description;

    @Attribute(name="st")
    private List<Name> provinces;

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

    public String getId() {
        return id.toString();
    }

    public void setCompany(String company) {
        this.company = LdapNameBuilder.newInstance(company).build();
        this.id = LdapNameBuilder.newInstance(company)
            .add("ou","departments")
            .add("ou",this.name)
            .build();
    }

    public String getCompany() {
        if(company != null)
            return this.company.toString();
        else return null;
    }

    public void setParent(String parent) {
        this.id = LdapNameBuilder.newInstance(parent)
            .add("ou",this.name)
            .build();
    }

    public String getParent() {
        return id.getPrefix(id.size()-1).toString();
    }

    public void setProvinces(List<Name> provinces) {
        //this.provinces = provinces.stream()
                                //.map(p->LdapNameBuilder.newInstance(p).build())
                                //.collect(Collectors.toList());
        this.provinces = provinces;
    }

    public List<Name> getProvinces() {
        //if(provinces!=null)
            //return provinces.stream()
                            //.map(p->p.toString())
                            //.collect(Collectors.toList());
        //else return new ArrayList<String>();
        return provinces;
    }
}
