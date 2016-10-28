package top.jyx365.organizationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.naming.Name;
import javax.naming.directory.SearchControls;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;

import static org.springframework.ldap.query.LdapQueryBuilder.query;
import top.jyx365.organizationService.Department;
public class OrganizationRepository {
    @Autowired
    private LdapTemplate ldapTemplate;

    private void addNode(Name parentNode, String type) {
        Name dn = LdapNameBuilder.newInstance(parentNode)
            .add("ou",type)
            .build();
        DirContextAdapter context= new DirContextAdapter(dn);
        context.setAttributeValues("objectclass", new String[] {"top", "organizationalUnit"});
        context.setAttributeValue("ou", type);
        context.setAttributeValue("description", type);
        ldapTemplate.bind(context);

    }

    private void removeNode(Name parentNode, String type) {
        Name dn = LdapNameBuilder.newInstance(parentNode)
            .add("ou",type)
            .build();
        ldapTemplate.unbind(dn);

    }

    /*Company*/
    public List<Company> getCompanies() {
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        return ldapTemplate.findAll(null,sc,Company.class);
    }

    public void addCompany(Company company) {
        ldapTemplate.create(company);
        addNode(company.getId(),"departments");
        addNode(company.getId(),"staffs");
        addNode(company.getId(),"groups");
        addNode(company.getId(),"applicants");
        addNode(company.getId(),"invitees");
        addNode(company.getId(),"products");
        addNode(company.getId(),"localities");
    }

    public Company findCompany(String companyId) {
        Name dn = LdapNameBuilder.newInstance(companyId).build();
        return ldapTemplate.findByDn(dn, Company.class);
    }

    public void removeCompany(String companyId) {
        Name dn = LdapNameBuilder.newInstance(companyId).build();
        Company company = ldapTemplate.findByDn(dn, Company.class);
        removeNode(company.getId(),"departments");
        removeNode(company.getId(),"staffs");
        removeNode(company.getId(),"groups");
        removeNode(company.getId(),"applicants");
        removeNode(company.getId(),"invitees");
        removeNode(company.getId(),"products");
        removeNode(company.getId(),"localities");
        ldapTemplate.delete(company);
    }

    /*departments*/
    public Department findDepartment(String departmentId) {
        Name dn = LdapNameBuilder.newInstance(departmentId).build();
        return findDepartment(dn);
    }

    public Department findDepartment(Name dn) {
        try {
            return ldapTemplate.findByDn(dn, Department.class);
        } catch(Exception e) {
            return null;
        }
    }

    public List<Department> findCompanyDepartments(String companyId,boolean recursive) {
        Name dn = LdapNameBuilder.newInstance(companyId)
            .add("ou","departments")
            .build();
        return findDepartment(dn,recursive);
    }

    public List<Department> findSubDepartments(String parent, boolean recursive) {
        Name dn = LdapNameBuilder.newInstance(parent).build();
        return findDepartment(dn, recursive);
    }


    public List<Department> findDepartment(Name root, boolean recursive) {
        SearchControls sc = new SearchControls();
        List<Department> firstLevel = ldapTemplate.findAll(root,sc,Department.class);
        if(recursive) {
            List<Department> allLevel = new ArrayList<Department>();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            firstLevel.forEach(d->{
                allLevel.addAll(ldapTemplate.findAll(d.getId(),sc,Department.class));
            });

            return allLevel;
        } else {
            return firstLevel;
        }
    }

    public void addDepartment(Department dept) {
        ldapTemplate.create(dept);
    }


    public void deleteDepartment(Department dept, boolean recursive) {
        ldapTemplate.unbind(dept.getId(),recursive);
    }

    /*department role*/
    public Role findRole(String roleId) {
        Name dn = LdapNameBuilder.newInstance(roleId).build();
        return findRole(dn);
    }

    public Role findRole(Name dn) {
        try {
            return ldapTemplate.findByDn(dn, Role.class);
        } catch(Exception e) {
            return null;
        }
    }

    public List<Role> findDepartmentRoles(String department) {
        try {
            SearchControls sc = new SearchControls();
            Name dn = LdapNameBuilder.newInstance(department).build();
            return ldapTemplate.findAll(dn, sc, Role.class);
        } catch(Exception e) {
            return null;
        }
    }

    public List<Role> findAllRoles(String companyId) {
        try {
            SearchControls sc = new SearchControls();
            Name dn = LdapNameBuilder.newInstance(companyId).build();
            return ldapTemplate.find(
                    query().base(dn)
                    .searchScope(SearchScope.SUBTREE)
                    .where("objectClass").is("organizationalRole"),
                    Role.class
                    );
        } catch (Exception e) {
            return null;
        }
    }


    public void addRole(Role role) {
        ldapTemplate.create(role);
    }

    public void deleteRole(Role role) {
        ldapTemplate.delete(role);
    }

    /*Staffs*/
    public List<Staff> findAllStaffs(String companyId, String type) {
        try {
            SearchControls sc = new SearchControls();
            Name dn = LdapNameBuilder.newInstance(companyId)
                .add("ou",type)
                .build();
            return ldapTemplate.findAll(dn,sc,Staff.class);
        } catch(Exception e) {
            return null;
        }
    }

    public List<Staff> findStaffs(String companyId,Map<String, String> searchCondition) {
        try {
            ContainerCriteria query;
            if(companyId != null) {
                Name dn = LdapNameBuilder.newInstance(companyId)
                    .add("ou","staffs")
                    .build();
                query = query().base(dn).where("objectclass").is("inetOrgPerson");
            }else {
                query = query().where("objectclass").is("inetOrgPerson")
                    .and("employeeType").is("staffs");
            }
            for(Map.Entry<String,String> entry:searchCondition.entrySet()){
                if(entry.getValue()!= null)
                    query=query.and(entry.getKey()).like(entry.getValue());
            }
            return ldapTemplate.find(query,Staff.class);
        } catch(Exception e) {
            return null;
        }
    }

    public List<Staff> findStaffs(String companyId, String departmentId,String type) {
        try {
            Name dn = LdapNameBuilder.newInstance(companyId)
                .add("ou",type)
                .build();
            return ldapTemplate.find(
                    query().base(dn)
                    .where("objectclass").is("inetOrgPerson")
                    .and("ou").is(departmentId),
                    Staff.class);
        } catch(Exception e) {
            return null;
        }
    }

    public Staff findStaff(String staffId) {
        Name dn = LdapNameBuilder.newInstance(staffId)
            .build();
        return ldapTemplate.findByDn(dn, Staff.class);
    }

    public Staff findStaff(Name dn) {
        try {
            return ldapTemplate.findByDn(dn, Staff.class);
        } catch(Exception e) {
            return null;
        }
    }


    public void addStaff(Staff staff) {
        ldapTemplate.create(staff);
    }

    public void updateStaff(Staff staff) {
        ldapTemplate.update(staff);
    }

    public void deleteStaff(Staff staff) {
        ldapTemplate.delete(staff);
    }

    /*Groups*/
    public List<Group> findAllGroups(String companyId) {
        SearchControls sc = new SearchControls();
        Name dn = LdapNameBuilder.newInstance(companyId)
            .add("ou","groups")
            .build();
        return ldapTemplate.findAll(dn,sc,Group.class);
    }

    public Group findGroup(String groupId) {
        Name dn = LdapNameBuilder.newInstance(groupId)
            .build();
        return ldapTemplate.findByDn(dn, Group.class);
    }

    public Group findGroup(Name dn) {
        return ldapTemplate.findByDn(dn, Group.class);
    }


    public void addGroup(Group group) {
        ldapTemplate.create(group);
    }

    public void updateGroup(Group group) {
        ldapTemplate.update(group);
    }

    public void deleteGroup(Group group) {
        ldapTemplate.delete(group);
    }


    /*Products*/
    public List<Product> findProducts(String companyId) {
        try {
            SearchControls sc = new SearchControls();
            Name dn = LdapNameBuilder.newInstance(companyId)
                .add("ou","products")
                .build();
            return ldapTemplate.findAll(dn,sc,Product.class);
        } catch(Exception e) {
            return null;
        }
    }

    public Product findProduct(String productId) {
        Name dn = LdapNameBuilder.newInstance(productId)
            .build();
        return ldapTemplate.findByDn(dn, Product.class);
    }

    public Product findProduct(Name dn) {
        try {
            return ldapTemplate.findByDn(dn, Product.class);
        } catch(Exception e) {
            return null;
        }
    }


    public void addProduct(Product product) {
        ldapTemplate.create(product);
    }

    public void updateProduct(Product product) {
        ldapTemplate.update(product);
    }

    public void deleteProduct(Product product) {
        ldapTemplate.delete(product);
    }

    /*Locality*/
    public List<Locality> findLocalities(Name root, boolean recursive) {
        try {
            SearchControls sc = new SearchControls();
            List<Locality> firstLevel = ldapTemplate.findAll(root,sc,Locality.class);
            if(recursive) {
                List<Locality> allLevel = new ArrayList<Locality>();
                sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
                firstLevel.forEach(l->{
                    allLevel.addAll(ldapTemplate.findAll(l.getId(),sc,Locality.class));
                });
                return allLevel;
            } else {
            return firstLevel;
            }
        } catch(Exception e) {
            return null;
        }
    }

    public List<Locality> findLocalities(String root, boolean recursive) {
        Name dn = LdapNameBuilder.newInstance(root).build();
        return findLocalities(dn,recursive);
    }

    public Locality findLocality(Name dn) {
        try {
            return ldapTemplate.findByDn(dn,Locality.class);
        } catch(Exception e) {
            return null;
        }
    }

    public Locality findLocality(String localityId) {
        Name dn = LdapNameBuilder.newInstance(localityId).build();
        return findLocality(dn);
    }

    public void addLocality(Locality l) {
        ldapTemplate.create(l);
    }

    public void updateLocality(Locality l) {
        ldapTemplate.update(l);
    }

    public void deleteLocality(Locality l, boolean recursive) {
        ldapTemplate.unbind(l.getId(),recursive);
    }

}

