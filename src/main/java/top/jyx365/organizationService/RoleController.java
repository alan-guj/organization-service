package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.naming.Name;
import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.VndErrors;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.core.Relation;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


@Relation(collectionRelation="roles", value="role")
class RoleResource extends ResourceSupport {
    private final Role role;

    public RoleResource(Role role) {
        this.role = role;
    }

    @JsonProperty("id")
    public Name getRoleId() {
        return this.role.getId();
    }

    public String getName() {
        return this.role.getName();
    }

    public String getDescription() {
        return this.role.getDescription();
    }

    public Name getDepartment() {
        return this.role.getDepartment();
    }

    public List<Name> getOccupants() {
        return this.role.getOccupants();
    }


    public String getCompany() {
        return this.role.getCompany();
    }

}


class RoleResourceAssembler extends ResourceAssemblerSupport<Role, RoleResource> {
    public RoleResourceAssembler() {
        super(RoleController.class, RoleResource.class);
    }

    @Override
    public RoleResource toResource(Role role) {
        RoleResource resource = createResourceWithId(
                role.getId().toString(),role,role.getCompany(),role.getDepartment());
        return resource;
    }

    @Override
    protected RoleResource instantiateResource(Role role) {
        return new RoleResource(role);
    }
}


@RestController
@EnableResourceServer
@RequestMapping("/api/v1.0/companies/{companyId}/departments/{departmentId}/roles")
class RoleController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private RoleResourceAssembler assember = new RoleResourceAssembler();

    @Autowired
    private OrganizationRepository repository;

    @RequestMapping(method = RequestMethod.POST)
        public ResponseEntity<RoleResource> addRole(
                @PathVariable String departmentId,
                @RequestBody Role role)
        {
            Department dept = repository.findDepartment(departmentId);
            role.setDepartment(dept.getId());
            repository.addRole(role);
            return new ResponseEntity<RoleResource>(
                    assember.toResource(role),HttpStatus.CREATED);
        }

    @RequestMapping(method = RequestMethod.GET)
        public Resources<RoleResource> getRoles(
                @PathVariable String companyId,
                @PathVariable String departmentId)
        {
            return new Resources<RoleResource>(
                    assember.toResources(
                        repository.findDepartmentRoles(departmentId)
                        )
                    );
        }


    @RequestMapping(value="/{roleId}", method = RequestMethod.GET)
        public RoleResource getRole(@PathVariable String roleId) {
            return assember.toResource(repository.findRole(roleId));
        }

    @RequestMapping(value="/{roleId}", method = RequestMethod.DELETE)
        public void deleteRole(@PathVariable String roleId) {
            Role role = repository.findRole(roleId);
            repository.deleteRole(role);
        }

    @RequestMapping(value="/{roleId}", method = RequestMethod.PUT)
        public RoleResource updateRole(@PathVariable String roleId,@RequestBody Role role) {
            Role _role = repository.findRole(roleId);
            role.setId(_role.getId());
            repository.updateRole(role);
            return assember.toResource(role);
        }

}

