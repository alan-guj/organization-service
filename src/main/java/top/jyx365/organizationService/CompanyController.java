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


@Relation(collectionRelation="companies",value="company")
class CompanyResource extends ResourceSupport {

    private final Company company;

    public CompanyResource(Company company) {
        this.company = company;
    }

    @JsonProperty("id")
    public Name getCompanyDn() {
        return this.company.getId();
    }

    public String getCompanyId() {
        return this.company.getCompanyId();
    }

    public String getName() {
        return this.company.getName();
    }

    public String getDescription() {
        return this.company.getDescription();
    }

    public String getDomain() {
        return this.company.getDomain();
    }


}


class CompanyResourceAssembler extends ResourceAssemblerSupport<Company, CompanyResource> {
    public CompanyResourceAssembler() {
        super(CompanyController.class, CompanyResource.class);
    }

    @Override
    public CompanyResource toResource(Company company) {
        CompanyResource resource = createResourceWithId(company.getId(),company);
        resource.add(
                linkTo(
                    methodOn(DepartmentController.class)
                    .getDepartments(company.getId().toString(),
                        null,null)
                    ).withRel("departments"));
        resource.add(
                linkTo(
                    methodOn(StaffController.class)
                    .getStaffs(company.getId().toString(),
                        null,null,null,null)
                    ).withRel("staffs"));
        resource.add(
                linkTo(
                    methodOn(StaffController.InviteeController.class)
                    .getInvitees(company.getId().toString(),
                        null,null,null)
                    ).withRel("invitees"));
        resource.add(
                linkTo(
                    methodOn(StaffController.ApplicantController.class)
                    .getApplicants(company.getId().toString()
                        ,null,null,null)
                    ).withRel("applicants"));
        resource.add(
                linkTo(
                    methodOn(GroupController.class)
                    .getAllGroups(company.getId().toString())
                    ).withRel("groups"));

        return resource;
    }

    @Override
    protected CompanyResource instantiateResource(Company company) {
        return new CompanyResource(company);
    }
}


@RestController
@EnableResourceServer
@RequestMapping("/api/v1.0/companies")
public class CompanyController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private CompanyResourceAssembler companyAssember = new CompanyResourceAssembler();


    @Autowired
    private OrganizationRepository repository;

    @RequestMapping(method = RequestMethod.POST)
        public ResponseEntity<CompanyResource> addCompany(@RequestBody Company company) {
            repository.addCompany(company);
            return new ResponseEntity<CompanyResource>(
                    companyAssember.toResource(company),HttpStatus.CREATED);
        }

    @RequestMapping(method = RequestMethod.GET)
        public Resources<CompanyResource> getCompanies() {
            List<CompanyResource> companies =
                companyAssember.toResources(repository.getCompanies());
            return new Resources<CompanyResource>(companies);
        }

    @RequestMapping(value = "/{companyId}", method = RequestMethod.GET)
        public CompanyResource getCompany(@PathVariable String companyId){
            return companyAssember.toResource(repository.findCompany(companyId));
        }

}


