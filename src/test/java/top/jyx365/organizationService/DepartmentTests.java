package top.jyx365.organizationService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.env.Environment;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapNameBuilder;

import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.ProfileValueSource;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static top.jyx365.organizationService.CompanyTests.TestProfileValueSource;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev-int-test")
@ProfileValueSourceConfiguration(TestProfileValueSource.class)
@Slf4j
public class DepartmentTests extends OrganizationServiceApplicationTests {

    public static class TestProfileValueSource implements ProfileValueSource {
        public String get(String key) {
            return "department";
        }
    }
    /*2. Department Service*/
    /*2.1 Add */
    /*2.1.1 Add new 1st level department*/
    @Test
    @IfProfileValue(name="test-group", values={"all","department"})
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
    @IfProfileValue(name="test-group", values={"all","department"})
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
    @IfProfileValue(name="test-group", values={"all","department"})
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
    @IfProfileValue(name="test-group", values={"all","department"})
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
    @IfProfileValue(name="test-group", values={"all","department"})
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
    @IfProfileValue(name="test-group", values={"all","department"})
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

    /*4 Role Service*/
    /*4.1 Add */

    /*4.1.1 Add a dept role*/
    @Test
    @IfProfileValue(name="test-group", values={"all","role"})
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


    /*4.1.2 Add a company role/
    @Test
    public void addCompanyRole() throws Exception {
        Role role = new Role();
        Company c = c_1;
        role.setDepartment(c.getId().toString());
        role.setName("ceo");
        role.setDescription("测试角色-ceo");
        String roleId = LdapNameBuilder.newInstance(c.getId())
                                                .add("ou","roles")
                                                .add("cn",role.getName())
                                                .build().toString();
        try {
            RequestBuilder request = post("/api/v1.0/companies/"+
                    c.getId().toString()+
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
                .andExpect(jsonPath("$.department",is(role.getDepartment())))
                .andExpect(jsonPath("$.occupants").doesNotExist());
        } catch(Exception e) {
            throw(e);
        } finally {
            cleanNode(roleId);
        }
    }
    */

    /*4.1.3 add a duplicated dept role*/
    @Test
    @IfProfileValue(name="test-group", values={"all","role"})
    public void _4_1_3_addDupDeptRole() throws Exception {
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

    /*4.1.4 get all roles of a dept*/
    @Test
    @IfProfileValue(name="test-group", values={"all","role"})
    public void _4_1_4_getAllDeptRoles() throws Exception {
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

    /*4.1.5 get one role of a dept*/
    @Test
    @IfProfileValue(name="test-group", values={"all","role"})
    public void _4_1_5_getOneDeptRole() throws Exception {
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



}


