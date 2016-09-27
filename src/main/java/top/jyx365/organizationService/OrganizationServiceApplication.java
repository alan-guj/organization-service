package top.jyx365.organizationService;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@SpringBootApplication
public class OrganizationServiceApplication {


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


class CompanyResource extends ResourceSupport {


    private final Company company;

    public CompanyResource(Company company) {
        String companyId= company.getId().toString();
        this.company = company;
        this.add(linkTo(
                    methodOn(DepartmentController.class,companyId).getDepartments(companyId,null)).withRel("departments"));
        this.add(linkTo(
                    methodOn(CompanyController.class,companyId).getCompany(companyId)).withSelfRel());
    }
    public Company getCompany() {
        return this.company;
    }
}

class CompanyResourceAssembler extends ResourceAssemblerSupport<Company, CompanyResource> {
    public CompanyResourceAssembler() {
        super(CompanyController.class, CompanyResource.class);
    }

    @Override
    public CompanyResource toResource(Company company) {
        CompanyResource resource = new CompanyResource(company);
        return resource;
    }
}


@RestController
@EnableResourceServer
@RequestMapping("/api/v1.0/companies")
class CompanyController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private CompanyResourceAssembler companyAssember = new CompanyResourceAssembler();

    @Autowired
    private OrganizationRepository repository;

    @RequestMapping(method = RequestMethod.POST)
    public CompanyResource addCompany(@RequestBody Company company) {
        repository.addCompany(company);
        return companyAssember.toResource(company);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<CompanyResource> getCompanies() {
        return companyAssember.toResources(repository.getCompanies());
    }

    @RequestMapping(value = "/{companyId}", method = RequestMethod.GET)
    public CompanyResource getCompany(@PathVariable String companyId){
        return companyAssember.toResource(repository.findCompany(companyId));
    }

}



@RestController
@EnableResourceServer
@RequestMapping("/api/v1.0/companies/{companyId}/departments")
class DepartmentController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OrganizationRepository repository;

    @RequestMapping(method = RequestMethod.GET)
    public List<Department> getDepartments(
            @PathVariable String companyId,
            @RequestParam(required=false) String parent
            ) {
        if(parent == null)
            return repository.findDepartments(companyId);
        else
            return repository.findSubDepartments(parent);
    }

    @RequestMapping(value="/{parentDepartmentId}/departments",method = RequestMethod.GET)
    public List<Department> getSubDepartments(
            @PathVariable String companyId,
            @PathVariable String parentDepartmentId
            ) {
        return repository.findSubDepartments(parentDepartmentId);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Department addDepartment(@PathVariable String companyId,  @RequestBody Department dept) {
        dept.setCompany(companyId);
        repository.addDepartment(dept);
        return dept;
    }

    @RequestMapping(value="/{parentDepartmentId}/departments",method = RequestMethod.POST)
    public Department addSubDepartments(
            @PathVariable String companyId,
            @PathVariable String parentDepartmentId,
            @RequestBody Department dept
            ) {
        dept.setParent(parentDepartmentId);
        repository.addDepartment(dept);
        return dept;
    }

    @RequestMapping(value = "/{departmentId}", method = RequestMethod.GET)
    public Department getDepartment(@PathVariable String companyId, @PathVariable String departmentId) {
        return repository.findDepartment(departmentId);
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
        staff.setDomain(company.getDomain());
        staff.setCompany(company.getId());
        repository.addStaff(staff);
        return staff;
    }

    @RequestMapping(path="/api/v1.0/companies/{companyId}/departments/{departmentId}/staffs",method = RequestMethod.POST)
    public Staff addDepartmentStaff(@PathVariable String companyId, @PathVariable String departmentId, @RequestBody Staff staff) {
        Company company = repository.findCompany(companyId);
        staff.setDomain(company.getDomain());
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

