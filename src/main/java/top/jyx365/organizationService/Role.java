package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.naming.Name;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.support.LdapNameBuilder;

@Entry(objectClasses = {"organizationalRole"})
public final class Role {
    @Id
    @JsonIgnore private Name id;

    @Attribute(name="cn")
    private String name;

    @Attribute(name="roleOccupant")
    private List<Name> occupants;

    private String description;

    @Attribute(name="ou")
    private Name department;



    public void setName(String name) {
        this.name = name;
        if(this.id == null && this.department!= null)
            this.id = LdapNameBuilder.newInstance(department)
                .add("cn",name)
                .build();
    }

    public String getName() {
        return name;
    }

    public void setOccupants(List<Name> occupants) {
        this.occupants = occupants;
    }

    public void addOccupant(Name occupant) {
        if(this.occupants == null) this.occupants = new ArrayList<Name>();
        this.occupants.add(occupant);
    }

    public void removeOccupant(Name occupant) {
        this.occupants.remove(occupant);
    }

    public List<Name> getOccupants() {
        return occupants;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDepartment(Name department) {
        this.department = department;
        if(this.id == null && this.name != null)
            this.id = LdapNameBuilder.newInstance(department)
                .add("cn",name)
                .build();
    }

    public Name getDepartment() {
        return department;
    }

    public void setId(Name id) {
        this.id = id;
    }

    public Name getId() {
        return id;
    }


    public String getCompany() {
        return id.getPrefix(1).toString();
    }


}
