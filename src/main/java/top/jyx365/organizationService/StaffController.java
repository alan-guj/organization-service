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


@Relation(collectionRelation="staffs", value="staff")
class StaffResource extends ResourceSupport {
    private final Staff staff;

    public StaffResource(Staff staff) {
        this.staff = staff;
    }

    @JsonProperty("id")
    public Name getStaffId() {
        return this.staff.getId();
    }

    public String getName() {
        return this.staff.getName();
    }

    public String getSurname() {
        return this.staff.getSurname();
    }

    public String getDescription() {
        return this.staff.getDescription();
    }

    public String getMobile() {
        return this.staff.getMobile();
    }

    public Name getCompany() {
        return this.staff.getCompany();
    }

    public List<Name> getDepartments() {
        return this.staff.getDepartments();
    }

}


class StaffResourceAssembler extends ResourceAssemblerSupport<Staff, StaffResource> {
    public StaffResourceAssembler() {
        super(StaffController.class, StaffResource.class);
    }

    @Override
    public StaffResource toResource(Staff staff) {
        StaffResource resource = createResourceWithId(
                staff.getId().toString(),staff,staff.getCompany().toString());
        return resource;
    }

    @Override
    protected StaffResource instantiateResource(Staff staff) {
        return new StaffResource(staff);
    }
}




@RestController
@EnableResourceServer
@RequestMapping({
"/api/v1.0/companies/**/staffs"
})
public class StaffController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private StaffResourceAssembler resourceAssember= new StaffResourceAssembler();

    @Autowired
    private OrganizationRepository repository;

    @RequestMapping(path="/api/v1.0/companies/{companyId}/staffs",method = RequestMethod.GET)
        public Resources<StaffResource> getStaffs(@PathVariable String companyId) {
            return new Resources<StaffResource>(
                    resourceAssember.toResources(repository.findAllStaffs(companyId)));
        }

    @RequestMapping(path="/api/v1.0/companies/{companyId}/departments/{departmentId}/staffs",method = RequestMethod.GET)
        public Resources<StaffResource> getStaffs(@PathVariable String companyId, @PathVariable String departmentId) {
            return new Resources<StaffResource>(
                    resourceAssember.toResources(repository.findStaffs(companyId,departmentId)));
        }

    @RequestMapping(path="/api/v1.0/companies/{companyId}/staffs",method = RequestMethod.POST)
        public ResponseEntity<StaffResource> addCompanyStaff(
                @PathVariable String companyId, @RequestBody Staff staff)
        {
            Company company = repository.findCompany(companyId);
            staff.setCompany(company.getId());
            repository.addStaff(staff);
            return new ResponseEntity<StaffResource>(
                    resourceAssember.toResource(staff),HttpStatus.CREATED);
        }

    @RequestMapping(path="/api/v1.0/companies/{companyId}/departments/{departmentId}/staffs",method = RequestMethod.POST)
        public ResponseEntity<StaffResource> addDepartmentStaff(
                @PathVariable String companyId, @PathVariable String departmentId, @RequestBody Staff staff)
        {
            if(staff.getId() != null) {

                /*id isn't none, try to find the staff*/
                Staff _staff = repository.findStaff(staff.getId());
                if(_staff != null) {
                    /*get a staff, add department*/
                    Department dept = repository.findDepartment(departmentId);
                    _staff.addDepartment(dept.getId());
                    repository.updateStaff(_staff);
                    return new ResponseEntity<StaffResource>(
                            resourceAssember.toResource(_staff),HttpStatus.CREATED);
                }
            }

            /*id is null ,or staff is not exist, add a new staff*/
            Company company = repository.findCompany(companyId);
            staff.setCompany(company.getId());
            Department dept = repository.findDepartment(departmentId);
            staff.addDepartment(dept.getId());
            repository.addStaff(staff);
            return new ResponseEntity<StaffResource>(
                    resourceAssember.toResource(staff),HttpStatus.CREATED);

        }

    @RequestMapping(value="/{staffId}",method = RequestMethod.GET)
        public StaffResource getStaff(@PathVariable String staffId) {
            return resourceAssember.toResource(repository.findStaff(staffId));
        }

}


