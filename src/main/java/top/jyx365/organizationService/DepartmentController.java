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

@Relation(collectionRelation="departments", value="department")
class DepartmentResource extends ResourceSupport {
    private final Department dept;

    public DepartmentResource(Department dept) {
        this.dept = dept;
    }

    @JsonProperty("id")
    public Name getDepartmentId() {
        return this.dept.getId();
    }

    public String getName() {
        return this.dept.getName();
    }

    public String getDescription() {
        return this.dept.getDescription();
    }

    public String getCompany() {
        return this.dept.getCompany();
    }

    public String getParent() {
        return this.dept.getParent();
    }

    public List<String> getBusinessCategories() {
        return this.dept.getBusinessCategories();
    }

}

class DepartmentResourceAssembler extends ResourceAssemblerSupport<Department, DepartmentResource> {
    public DepartmentResourceAssembler() {
        super(DepartmentController.class, DepartmentResource.class);
    }

    @Override
    public DepartmentResource toResource(Department dept) {
        DepartmentResource resource = createResourceWithId(
                dept.getId().toString(),dept,dept.getCompany());
        return resource;
    }

    @Override
    protected DepartmentResource instantiateResource(Department dept) {
        return new DepartmentResource(dept);
    }
}


@RestController
@EnableResourceServer
@RequestMapping("/api/v1.0/companies/{companyId}/departments")
public class DepartmentController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private DepartmentResourceAssembler deptAssember = new DepartmentResourceAssembler();

    @Autowired
    private OrganizationRepository repository;

    @RequestMapping(method = RequestMethod.GET)
        public Resources<DepartmentResource> getDepartments(
                @PathVariable String companyId,
                @RequestParam(required=false) String parent
                )
        {
            List<Department> result;
            if(parent == null)
                result = repository.findCompanyDepartments(companyId,true);
            else
                result = repository.findSubDepartments(parent,false);

            return new Resources<DepartmentResource>(deptAssember.toResources(result));
        }

    @RequestMapping(value="/{parentDepartmentId}/departments",method = RequestMethod.GET)
        public Resources<DepartmentResource> getSubDepartments(
                @PathVariable String companyId,
                @PathVariable String parentDepartmentId)
        {
            return new Resources<DepartmentResource>(
                    deptAssember.toResources(
                        repository.findSubDepartments(parentDepartmentId,false)
                        )
                    );
        }

    @RequestMapping(method = RequestMethod.POST)
        public ResponseEntity<DepartmentResource> addDepartment(
                @PathVariable String companyId,
                @RequestBody Department dept)
        {
            dept.setCompany(companyId);
            repository.addDepartment(dept);
            return new ResponseEntity<DepartmentResource>(
                    deptAssember.toResource(dept),HttpStatus.CREATED);
        }

    @RequestMapping(value="/{parentDepartmentId}/departments",method = RequestMethod.POST)
        public ResponseEntity<DepartmentResource> addSubDepartments(
                @PathVariable String companyId,
                @PathVariable String parentDepartmentId,
                @RequestBody Department dept)
        {
            dept.setParent(parentDepartmentId);
            repository.addDepartment(dept);
            return new ResponseEntity<DepartmentResource>(
                    deptAssember.toResource(dept),HttpStatus.CREATED);
        }

    @RequestMapping(value = "/{departmentId}", method = RequestMethod.GET)
        public DepartmentResource getDepartment(@PathVariable String companyId, @PathVariable String departmentId) {
            return deptAssember.toResource(repository.findDepartment(departmentId));
        }

}


