package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.security.oauth2.provider.OAuth2Authentication;
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

@Relation(collectionRelation="invitees", value="invitee")
class InviteeResource extends StaffResource {
    public InviteeResource(Staff staff) {
        super(staff);
    }
}


@Relation(collectionRelation="applicants", value="applicant")
class ApplicantResource extends StaffResource {
    public ApplicantResource(Staff staff) {
        super(staff);
    }
}


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

    public String getUid() {
        return this.staff.getUid();
    }

    public List<BusinessCategory> getBusinessCategories() {
        return this.staff.getBusinessCategories();
    }

    public Name getRelatedStaff() {
        return this.staff.getRelatedStaff();
    }
}


class InviteeResourcAssember extends ResourceAssemblerSupport<Staff, InviteeResource> {
    public InviteeResourcAssember() {
        super(StaffController.InviteeController.class, InviteeResource.class);
    }

    @Override
    public InviteeResource toResource(Staff staff) {
        InviteeResource resource = createResourceWithId(
                staff.getId().toString(),
                staff,
                staff.getCompany().toString(),
                "invitees");
        return resource;
    }

    @Override
    protected InviteeResource instantiateResource(Staff staff) {
        return new InviteeResource(staff);
    }
}


class ApplicantResourceAssembler extends ResourceAssemblerSupport<Staff, ApplicantResource> {
    public ApplicantResourceAssembler() {
        super(StaffController.ApplicantController.class, ApplicantResource.class);
    }

    @Override
    public ApplicantResource toResource(Staff staff) {
        ApplicantResource resource = createResourceWithId(
                staff.getId().toString(),
                staff,
                staff.getCompany().toString(),
                "applicants");
        return resource;
    }

    @Override
    protected ApplicantResource instantiateResource(Staff staff) {
        return new ApplicantResource(staff);
    }
}


class StaffResourceAssembler extends ResourceAssemblerSupport<Staff, StaffResource> {
    public StaffResourceAssembler() {
        super(StaffController.class, StaffResource.class);
    }

    @Override
    public StaffResource toResource(Staff staff) {
        StaffResource resource = createResourceWithId(
                staff.getId().toString(),
                staff,
                staff.getCompany().toString(),
                "staffs");
        return resource;
    }

    @Override
    protected StaffResource instantiateResource(Staff staff) {
        return new StaffResource(staff);
    }
}

@RestController
@EnableResourceServer
@RequestMapping("/api/v1.0/companies/{companyId}/staffs")
public class StaffController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private StaffResourceAssembler resourceAssember= new StaffResourceAssembler();

    @Autowired
    private OrganizationRepository repository;

    @RequestMapping(method = RequestMethod.POST)
        public ResponseEntity<StaffResource> addCompanyStaff(
                @PathVariable String companyId, @RequestBody Staff staff)
        {
            Company company = repository.findCompany(companyId);
            staff.setCompany(company.getId());
            staff.setType("staffs");
            repository.addStaff(staff);
            return new ResponseEntity<StaffResource>(
                    resourceAssember.toResource(staff),HttpStatus.CREATED);
        }


    @RequestMapping(method = RequestMethod.GET)
        public Resources<StaffResource> getStaffs(
                @PathVariable String companyId,
                @RequestParam(required = false) String mobile,
                @RequestParam(required = false) String department,
                @RequestParam(required = false) String name
                )
        {
            if(companyId.equals("**")) companyId = null;
            Map<String, String> searchCondition = new HashMap<String, String>();
            searchCondition.put("mobile",mobile);
            searchCondition.put("ou",department);
            searchCondition.put("name",name);
            return new Resources<StaffResource>(resourceAssember.toResources(
                    repository.findStaffs(companyId,searchCondition)));
        }

    @RequestMapping(value="/{staffId}",method = RequestMethod.GET)
        public StaffResource getStaff(
                @PathVariable String staffId
                )
        {
            return resourceAssember.toResource(repository.findStaff(staffId));
        }


    @RestController
    @EnableResourceServer
    @RequestMapping("/api/v1.0/companies/{companyId}/applicants")
    class ApplicantController {

        private ApplicantResourceAssembler assember = new ApplicantResourceAssembler();
        private StaffResourceAssembler staffAssember = new StaffResourceAssembler();

        @Autowired
        private OrganizationRepository repository;

        @RequestMapping(method = RequestMethod.GET)
            public Resources<ApplicantResource> getApplicants(@PathVariable String companyId) {
                return new Resources<ApplicantResource>(
                        assember.toResources(
                            repository.findAllStaffs(companyId,"applicants"))
                        );
            }

        @RequestMapping(value = "/{applicantId}", method = RequestMethod.GET)
            public ApplicantResource getApplicant(@PathVariable String applicantId) {
                return assember.toResource(
                        repository.findStaff(applicantId));
            }


        @RequestMapping(method = RequestMethod.POST)
            public ResponseEntity<ApplicantResource> addApplicants(
                    OAuth2Authentication user,
                    @PathVariable String companyId,
                    @RequestBody Staff staff
                    )
            {
                Company company = repository.findCompany(companyId);
                staff.setCompany(company.getId());
                staff.setType("applicants");

                @SuppressWarnings("unchecked")
                Map<String, Object> principal = (Map<String, Object>)user.getPrincipal();

                staff.setUid(principal.get("id").toString());

                /*TODO: Set Related Staff*/

                repository.addStaff(staff);
                return new ResponseEntity<ApplicantResource>(
                        assember.toResource(staff), HttpStatus.CREATED
                        );
            }

        @RequestMapping(value = "/{applicantId}/approval", method = RequestMethod.POST)
            public ResponseEntity<StaffResource> approveApplicant(
                    @PathVariable String applicantId)
            {
                Staff staff = repository.findStaff(applicantId);

                staff.setType("staffs");
                repository.updateStaff(staff);
                return new ResponseEntity<StaffResource>(
                        staffAssember.toResource(staff), HttpStatus.CREATED
                        );
            }
    }

    @RestController
    @EnableResourceServer
    @RequestMapping("/api/v1.0/companies/{companyId}/invitees")
    class InviteeController {

        private InviteeResourcAssember assember = new InviteeResourcAssember();
        private StaffResourceAssembler staffAssember = new StaffResourceAssembler();

        @Autowired
        private OrganizationRepository repository;


        @RequestMapping(method = RequestMethod.GET)
            public Resources<InviteeResource> getInvitees(@PathVariable String companyId) {
                return new Resources<InviteeResource>(
                        assember.toResources(
                            repository.findAllStaffs(companyId,"invitees"))
                        );
            }

        @RequestMapping(value = "/{inviteeId}", method = RequestMethod.GET)
            public InviteeResource getInvitee(@PathVariable String inviteeId) {
                return assember.toResource(
                        repository.findStaff(inviteeId));
            }


        @RequestMapping(method = RequestMethod.POST)
            public ResponseEntity<InviteeResource> addInvitee(
                    @PathVariable String companyId,
                    @RequestBody Staff staff
                    )
            {
                Company company = repository.findCompany(companyId);
                staff.setCompany(company.getId());
                staff.setType("invitees");

                /*TODO: Set Related Staff*/

                repository.addStaff(staff);
                return new ResponseEntity<InviteeResource>(
                        assember.toResource(staff), HttpStatus.CREATED
                        );
            }

        @RequestMapping(value = "/{inviteeId}/confirmation", method = RequestMethod.POST)
            public ResponseEntity<StaffResource> approveApplicant(
                    OAuth2Authentication user,
                    @PathVariable String inviteeId) {
                Staff staff = repository.findStaff(inviteeId);
                staff.setType("staffs");

                @SuppressWarnings("unchecked")
                Map<String, Object> principal = (Map<String, Object>)user.getPrincipal();

                staff.setUid(principal.get("id").toString());

                repository.updateStaff(staff);
                return new ResponseEntity<StaffResource>(
                        staffAssember.toResource(staff), HttpStatus.CREATED
                        );
            }
    }
}


