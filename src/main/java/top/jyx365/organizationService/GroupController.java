package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.Name;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.SpringApplication;
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

import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Relation(collectionRelation="groups", value="group")
class GroupResource extends ResourceSupport {
    private final Group group;

    public GroupResource(Group group) {
        this.group = group;
    }

    @JsonProperty("id")
    public Name getGroupId() {
        return this.group.getId();
    }

    public String getDescription() {
        return this.group.getDescription();
    }

    public String getName() {
        return this.group.getName();
    }

    public Name getCompany() {
        return this.group.getCompany();
    }

    public List<Name> getMembers() {
        return this.group.getMembers();
    }

    public List<BusinessCategory> getBusinessCategories() {
        return this.group.getBusinessCategories();
    }

}


class GroupResourceAssember extends ResourceAssemblerSupport<Group, GroupResource> {
    public GroupResourceAssember() {
        super(GroupController.class, GroupResource.class);
    }

    @Override
    public GroupResource toResource(Group group) {
        GroupResource resource = createResourceWithId(
                group.getId().toString(), group, group.getCompany().toString());
        return resource;
    }

    @Override
    protected GroupResource instantiateResource(Group group) {
        return new GroupResource(group);
    }
}


@RestController
@EnableResourceServer
@RequestMapping({"/api/v1.0/companies/{companyId}/groups"})
public class GroupController {
   private Logger logger = LoggerFactory.getLogger(this.getClass());
   private GroupResourceAssember assember = new GroupResourceAssember();

   @Autowired
   private OrganizationRepository repository;

   @RequestMapping(path="/{groupId}", method = RequestMethod.GET)
       public GroupResource getGroup(@PathVariable String groupId) {
           return assember.toResource(
                   repository.findGroup(groupId)
                   );
       }

    @RequestMapping(method = RequestMethod.GET)
        public Resources<GroupResource> getGroups(
                @PathVariable String companyId,
                @RequestParam(required = false) String name,
                @RequestParam(required = false) String member
                ) {
            if(companyId.equals("**")) companyId = null;
            Map<String, String> searchCondition = new HashMap<String, String>();
            searchCondition.put("cn",name);
            searchCondition.put("member",member);
            return new Resources<GroupResource>(
                    assember.toResources(
                        repository.findGroups(companyId,searchCondition)
                    ));
        }

    @RequestMapping(method = RequestMethod.POST)
        public ResponseEntity<GroupResource> addGroup(
                @PathVariable String companyId, @RequestBody Group group)
        {
            Company company = repository.findCompany(companyId);
            group.setCompany(company.getId());
            repository.addGroup(group);
            return new ResponseEntity<>(
                    assember.toResource(group),HttpStatus.CREATED
                    );
        }
    @RequestMapping(value = "/{groupId}",method = RequestMethod.DELETE)
        public void deleteDepartment(@PathVariable String groupId) {
            Group g = repository.findGroup(groupId);
            repository.deleteGroup(g);
        }
    @RequestMapping(value = "/{groupId}", method = RequestMethod.PUT)
        public GroupResource updateGroup(
                @PathVariable String companyId,
                @PathVariable String groupId,
                @RequestBody Group group) {
            Group _group = repository.findGroup(groupId);
            group.setId(_group.getId());
            Company _company = repository.findCompany(companyId);
            group.setCompany(_company.getId());
            repository.updateGroup(group);
            return assember.toResource(group);
        }
}
