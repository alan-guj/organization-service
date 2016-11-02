package top.jyx365.organizationService;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.ldap.support.LdapNameBuilder;

import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.ProfileValueSource;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static top.jyx365.organizationService.OrganizationServiceApplicationTests.TestProfileValueSource;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev-int-test")
@ProfileValueSourceConfiguration(Configuration.class)
@Slf4j
public class DepartmentTests extends OrganizationServiceApplicationTests {

    /*2. Department Service*/
    /*2.1 Add */
    /*2.1.1 Add new 1st level department*/
    @Test
    @IfProfileValue(name="dept-test-group", values={"all","department"})
    public void _2_1_1_addFirstLevelDept() throws Exception {
        Department dept = new Department();
        dept.setName("testadddept");
        dept.setDescription("测试新增一级部门");
        dept.setCompany(c_1.getId());
        try {
            RequestBuilder request = post("/api/v1.0/companies/"+c_1.getId().toString()+"/departments")
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(dept));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",is(
                                            LdapNameBuilder.newInstance(c_1.getId())
                                                .add("ou","departments")
                                                .add("ou",dept.getName())
                                                .build().toString()
                                )))
                .andExpect(jsonPath("$.name",is(dept.getName())))
                .andExpect(jsonPath("$.description",is(dept.getDescription())))
                .andExpect(jsonPath("$.company",is(c_1.getId().toString())))
                .andExpect(jsonPath("$.parent",is(
                                 LdapNameBuilder.newInstance(c_1.getId())
                                                .add("ou","departments")
                                                .build().toString()
                                )));
        } catch(Exception e) {
            throw(e);
        } finally {
            cleanNode(LdapNameBuilder.newInstance(c_1.getId())
                        .add("ou","departments")
                        .add("ou",dept.getName())
                        .build().toString());
        }

    }

    /*2.1.2 Add new 2nd level department*/
    @Test
    @IfProfileValue(name="dept-test-group", values={"all","department"})
    public void _2_1_2_addSecondLevelDept() throws Exception {
        Department dept = new Department();
        dept.setName("testadd2nddept");
        dept.setDescription("测试新增二级部门");
        dept.setParent(d_1.getId());
        try {
            RequestBuilder request = post(
                    "/api/v1.0/companies/"+
                    d_1.getCompany()+
                    "/departments")
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(dept));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",is(
                                            LdapNameBuilder.newInstance(d_1.getId())
                                                .add("ou",dept.getName())
                                                .build().toString()
                                )))
                .andExpect(jsonPath("$.name",is(dept.getName())))
                .andExpect(jsonPath("$.description",is(dept.getDescription())))
                .andExpect(jsonPath("$.company",is(d_1.getCompany().toString())))
                .andExpect(jsonPath("$.parent",is(d_1.getId().toString())));
        } catch(Exception e) {
            throw(e);
        } finally {
            cleanNode(LdapNameBuilder.newInstance(d_1.getId())
                        .add("ou",dept.getName())
                        .build().toString());
        }
    }

    /*2.1.3 Add exist department*/
    @Test
    @IfProfileValue(name="dept-test-group", values={"all","department"})
    public void _2_1_3_addExistDept() throws Exception {
        Department dept = new Department();
        dept.setName(d_1_1.getName());
        dept.setDescription("测试新增重复部门");
        dept.setParent(d_1.getId());
        try {
            RequestBuilder request = post(
                    "/api/v1.0/companies/"+
                    d_1.getCompany()+
                    "/departments")
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(dept));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$[0].message",is("object already exist")));
        } catch(Exception e) {
            throw(e);
        } finally {
            cleanNode(LdapNameBuilder.newInstance(d_1.getId())
                        .add("ou",dept.getName())
                        .build().toString());
        }
    }

    /*2.2 Query*/
    /*2.2.1 Get all departments*/
    @Test
    @IfProfileValue(name="dept-test-group", values={"all","department"})
    public void _2_2_1_getAllDept() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    c_1.getId().toString()+
                    "/departments")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.departments",hasSize(4)));
    }

    /*2.2.2 Get a departments*/
    @Test
    @IfProfileValue(name="dept-test-group", values={"all","department"})
    public void _2_2_2_getDept() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    d_1_1.getCompany()+
                    "/departments/"+
                    d_1_1.getId().toString())
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id",is(d_1_1.getId().toString())))
            .andExpect(jsonPath("$.name",is(d_1_1.getName())))
            .andExpect(jsonPath("$.description",is(d_1_1.getDescription())))
            .andExpect(jsonPath("$.parent",is(d_1_1.getParent().toString())))
            .andExpect(jsonPath("$.company",is(d_1_1.getCompany().toString())))
            .andExpect(jsonPath("$._links.roles.href",endsWith(
                            d_1_1.getCompany()+"/departments/"
                            +d_1_1.getId().toString()+"/roles")));
    }

    /*2.2.3 Get sub departments*/
    @Test
    @IfProfileValue(name="dept-test-group", values={"all","department"})
    public void _2_2_3_getSubDept() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    d_1.getCompany()+
                    "/departments?parent="+
                    d_1.getId().toString())
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.departments",hasSize(2)))
            .andExpect(jsonPath("$._embedded.departments[0].parent",is(d_1.getId().toString())));
    }


    /*2.3 Delete*/
    /*2.3.1 delete a exist dept*/
    @Test
    @IfProfileValue(name="dept-test-group", values={"all","dept-del"})
    public void _2_3_1_delExistDept() throws Exception {
        this.mockMvc.perform(delete("/api/v1.0/companies/"+
                    d_1.getCompany()+
                    "/departments/"+
                    d_1.getId())
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk());
        assertNull(repository.findDepartment(d_1.getId()));
    }

    /*2.4 Update*/
    /*2.4.1 update an exist dept*/
    @Test
    @IfProfileValue(name="dept-test-group", values = {"all","dept-update"})
    public void _2_4_1_updateExistDept() throws Exception {
        Department d = d_1;
        d.setDescription("修改部门1描述");
        this.mockMvc.perform(put("/api/v1.0/companies/"+
                    d.getCompany()+
                    "/departments/"+
                    d.getId())
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(d)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id",is(d.getId().toString())))
            .andExpect(jsonPath("$.description",is(d.getDescription())));
    }

    /*4 Role Service*/
    /*4.1 Add */

    /*4.1.1 Add a dept role*/
    @Test
    @IfProfileValue(name="dept-test-group", values={"all","role"})
    public void _4_1_1_addDeptRole() throws Exception {
        Role role = new Role();
        Department dept = d_1_1;
        role.setDepartment(dept.getId());
        role.setName("manager");
        role.setDescription("测试角色-经理");
        String roleId = LdapNameBuilder.newInstance(dept.getId())
                                                .add("cn",role.getName())
                                                .build().toString();
        try {
            RequestBuilder request = post("/api/v1.0/companies/"+
                    dept.getCompany()+
                    "/departments/"+
                    dept.getId().toString()+
                    "/roles")
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(role));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",is(roleId)))
                .andExpect(jsonPath("$.name",is(role.getName())))
                .andExpect(jsonPath("$.description",is(role.getDescription())))
                .andExpect(jsonPath("$.department",is(role.getDepartment().toString())))
                .andExpect(jsonPath("$.occupants").doesNotExist());
        } catch(Exception e) {
            throw(e);
        } finally {
            cleanNode(roleId);
        }
    }


    /*4.1.2 add a duplicated dept role*/
    @Test
    @IfProfileValue(name="dept-test-group", values={"all","role"})
    public void _4_1_2_addDupDeptRole() throws Exception {
        Role role = r_2;
        try {
            RequestBuilder request = post("/api/v1.0/companies/"+
                    d_2.getCompany()+
                    "/departments/"+
                    d_2.getId().toString()+
                    "/roles")
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(role));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isConflict());
        } catch(Exception e) {
            throw(e);
        } finally {
            cleanNode(role.getId().toString());
        }
    }

    /*4.2 Query*/
    /*4.2.1 get all roles of a dept*/
    @Test
    @IfProfileValue(name="dept-test-group", values={"all","role"})
    public void _4_2_1_getAllDeptRoles() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                        d_2.getCompany()+
                        "/departments/"+
                        d_2.getId().toString()+
                        "/roles")
                    .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.roles",hasSize(1)))
            .andExpect(jsonPath("$._embedded.roles[0].id",is(r_2.getId().toString())))
            .andExpect(jsonPath("$._embedded.roles[0].name",is(r_2.getName())))
            .andExpect(jsonPath("$._embedded.roles[0].description",is(r_2.getDescription())))
            .andExpect(jsonPath("$._embedded.roles[0].department",is(r_2.getDepartment().toString())))
            .andExpect(jsonPath("$._embedded.roles[0].occupants",hasSize(1)))
            .andExpect(jsonPath("$._embedded.roles[0].occupants",containsInAnyOrder(
                            s_1.getId().toString()
                            )));
    }

    /*4.2.1 get one role of a dept*/
    @Test
    @IfProfileValue(name="dept-test-group", values={"all","role"})
    public void _4_2_1_getOneDeptRole() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                        d_2.getCompany()+
                        "/departments/"+
                        d_2.getId().toString()+
                        "/roles/"+
                        r_2.getId())
                    .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id",is(r_2.getId().toString())))
            .andExpect(jsonPath("$.name",is(r_2.getName())))
            .andExpect(jsonPath("$.description",is(r_2.getDescription())))
            .andExpect(jsonPath("$.department",is(r_2.getDepartment().toString())))
            .andExpect(jsonPath("$.occupants",hasSize(1)))
            .andExpect(jsonPath("$.occupants",containsInAnyOrder(
                            s_1.getId().toString()
                            )));
    }

    /*4.3 delete*/
    /*4.3.1 delete an exist role*/
    @Test
    @IfProfileValue(name="dept-test-group", values={"all","role"})
    public void _4_3_1_delExistRole() throws Exception {
        this.mockMvc.perform(delete("/api/v1.0/companies/"+
                    r_2.getCompany()+
                    "/departments/"+
                    r_2.getDepartment()+
                    "/roles/"+
                    r_2.getId())
                .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk());
        assertNull(repository.findRole(r_2.getId()));
    }

    /*4.4 update*/
    /*4.4.1 update an exist role*/
    @Test
    @IfProfileValue(name = "dept-test-group", values = {"all","role","update-role"})
    public void _4_4_1_updateExistRole() throws Exception {

        String _id = r_2.getId().toString();
        r_2.setDescription("修改角色测试");
        log.debug("_4_4_1_updateExistRole:{}",json(r_2));
        this.mockMvc.perform(put(PATH_PREFIX_v1+
                    r_2.getCompany()+
                    "/departments/"+
                    r_2.getCompany()+
                    "/roles/"+
                    _id)
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(r_2)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id",is(_id)))
            .andExpect(jsonPath("$.description",is(r_2.getDescription())));
    }
}


