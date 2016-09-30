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

@SpringBootApplication
public class OrganizationServiceApplication {


    private static Logger logger = LoggerFactory.getLogger(OrganizationServiceApplication.class);

    @Bean
    @ConfigurationProperties(prefix="ldap")
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate(ContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

    @Bean
    public OrganizationRepository repository() {
        return new OrganizationRepository();
    }



	public static void main(String[] args) {
		SpringApplication.run(OrganizationServiceApplication.class, args);
	}
}

@ControllerAdvice
class OrganizationServiceContollerAdvice {
    @ResponseBody
    @ExceptionHandler(NameAlreadyBoundException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    VndErrors nameAlreadyBoundExceptionHandler(NameAlreadyBoundException ex) {
        return new VndErrors(ex.getRemainingName().toString(),"object already exist");
    }
}

@RestController
@EnableResourceServer
@RequestMapping({
"/api/v1.0/companies/**/staffs"
})
class StaffController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OrganizationRepository repository;

    @RequestMapping(path="/api/v1.0/companies/{companyId}/staffs",method = RequestMethod.GET)
        public List<Staff> getStaffs(@PathVariable String companyId) {
            return repository.findAllStaffs(companyId);
        }

    @RequestMapping(path="/api/v1.0/companies/{companyId}/departments/{departmentId}/staffs",method = RequestMethod.GET)
        public List<Staff> getStaffs(@PathVariable String companyId, @PathVariable String departmentId) {
            return repository.findStaffs(companyId,departmentId);
        }

    @RequestMapping(path="/api/v1.0/companies/{companyId}/staffs",method = RequestMethod.POST)
        public Staff addCompanyStaff(@PathVariable String companyId, @RequestBody Staff staff) {
            Company company = repository.findCompany(companyId);
            staff.setCompany(company.getId());
            repository.addStaff(staff);
            return staff;
        }

    @RequestMapping(path="/api/v1.0/companies/{companyId}/departments/{departmentId}/staffs",method = RequestMethod.POST)
        public Staff addDepartmentStaff(@PathVariable String companyId, @PathVariable String departmentId, @RequestBody Staff staff) {
            Company company = repository.findCompany(companyId);
            staff.setCompany(company.getId());
            Department dept = repository.findDepartment(departmentId);
            staff.setDepartment(dept.getId());
            repository.addStaff(staff);
            return staff;
        }
    @RequestMapping(value="/{staffId}",method = RequestMethod.GET)
        public Staff getStaff(@PathVariable String staffId) {
            return repository.findStaff(staffId);
        }

}

@RestController
@EnableResourceServer
@RequestMapping("/api/v1.0/companies/**/roles")
class RoleController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OrganizationRepository repository;

    @RequestMapping(path="/api/v1.0/companies/{companyId}/departments/{departmentId}/roles", method = RequestMethod.POST)
        public Role addRole(@PathVariable String departmentId, @RequestBody Role role) {
            role.setDepartment(departmentId);
            repository.addRole(role);
            return role;
        }

    @RequestMapping(path="/api/v1.0/companies/{companyId}/departments/{departmentId}/roles", method = RequestMethod.GET)
        public List<Role> getDepartmentRoles(@PathVariable String departmentId) {
            return repository.findDepartmentRoles(departmentId);
        }

    @RequestMapping(path="/api/v1.0/companies/{companyId}/roles", method = RequestMethod.GET)
        public List<Role> getCompanyRoles(@PathVariable String companyId) {
            return repository.findAllRoles(companyId);
        }

    @RequestMapping(value="/roles/{roleId}", method = RequestMethod.GET)
        public Role getRoles(@PathVariable String roleId) {
            return repository.findRole(roleId);
        }
}

