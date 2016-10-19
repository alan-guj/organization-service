package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.naming.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.Relation;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;


@Relation(collectionRelation="localities", value="locality")
class LocalityResource extends ResourceSupport {
    private final Locality l;

    public LocalityResource(Locality l) {
        this.l = l;
    }

    @JsonProperty("id")
    public Name getLocalityDn() {
        return this.l.getId();
    }

    public String getName() {
        return this.l.getName();
    }

    public String getDescrption() {
        return this.l.getDescription();
    }

    public Name getCompany() {
        return this.l.getCompany();
    }

    public Name getParent() {
        return this.l.getParent();
    }

    public String getLocalityId() {
        return this.l.getLocalityId();
    }
}


class LocalityResourceAssembler extends ResourceAssemblerSupport<Locality, LocalityResource>
{
    public LocalityResourceAssembler() {
        super(LocalityController.class, LocalityResource.class);
    }

    @Override
    public LocalityResource toResource(Locality locality) {
        LocalityResource resource = createResourceWithId(
            locality.getId().toString(),locality,locality.getCompany());
        return resource;
    }

    public LocalityResource instantiateResource(Locality l) {
        return new LocalityResource(l);
    }
}

@RestController
@EnableResourceServer
@RequestMapping("/api/v1.0/companies/{companyId}/localities")
public class LocalityController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private LocalityResourceAssembler assember = new LocalityResourceAssembler();

    @Autowired
    private OrganizationRepository repository;

    @RequestMapping(method = RequestMethod.GET)
        public Resources<LocalityResource> getLocalities(
                @PathVariable String companyId
                )
        {
            return new Resources<LocalityResource>(
                    assember.toResources(repository.findLocalities(companyId,true)));
        }

    @RequestMapping(value="/{LocalityId}",method = RequestMethod.GET)
        public LocalityResource getLocality(
                @PathVariable String companyId,
                @PathVariable String localityId
                )
        {
            return assember.toResource(repository.findLocality(localityId));
        }

    @RequestMapping(method = RequestMethod.POST)
        public ResponseEntity<LocalityResource> addLocality(
                @PathVariable String companyId,
                @RequestBody Locality l
                )
        {
            Company company = repository.findCompany(companyId);
            l.setCompany(company.getId());
            return new ResponseEntity<LocalityResource>(
                    assember.toResource(l), HttpStatus.CREATED);
        }
}
