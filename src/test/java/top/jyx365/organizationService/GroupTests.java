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
public class GroupTests extends OrganizationServiceApplicationTests {

    public static class TestProfileValueSource implements ProfileValueSource {
        public String get(String key) {
            return "all";
        }
    }
    /*5. Group*/
    /*5.1 Query*/
    /*5.1.1 get all groups*/
    @Test
    @IfProfileValue(name="test-group", values = {"all","group"})
    public void _5_1_1_getAllGroups() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    c_1.getId().toString()+
                    "/groups")
                .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.groups",hasSize(2)))
            .andExpect(jsonPath("$._embedded.groups[0].id",is(g_1.getId().toString())))
            .andExpect(jsonPath("$._embedded.groups[0].name",is(g_1.getName())))
            .andExpect(jsonPath("$._embedded.groups[0].description",is(g_1.getDescription())))
            .andExpect(jsonPath("$._embedded.groups[0].company",is(g_1.getCompany().toString())))
            .andExpect(jsonPath("$._embedded.groups[0].members",hasSize(2)))
            .andExpect(jsonPath("$._embedded.groups[0].members",containsInAnyOrder(
                            s_1.getId().toString(),
                            s_2.getId().toString()
                            )))
            .andExpect(jsonPath("$._embedded.groups[1].id",is(g_2.getId().toString())))
            .andExpect(jsonPath("$._embedded.groups[1].name",is(g_2.getName())))
            .andExpect(jsonPath("$._embedded.groups[1].description",is(g_2.getDescription())))
            .andExpect(jsonPath("$._embedded.groups[1].company",is(g_2.getCompany().toString())))
            .andExpect(jsonPath("$._embedded.groups[1].members").doesNotExist());
    }
    /*5.1.2 get one group*/
    @Test
    @IfProfileValue(name="test-group", values = {"all","group"})
    public void _5_1_2_getOneGroup() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    c_1.getId().toString()+
                    "/groups/"+
                    g_1.getId().toString())
                .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id",is(g_1.getId().toString())))
            .andExpect(jsonPath("$.name",is(g_1.getName())))
            .andExpect(jsonPath("$.description",is(g_1.getDescription())))
            .andExpect(jsonPath("$.company",is(g_1.getCompany().toString())))
            .andExpect(jsonPath("$.members",hasSize(2)))
            .andExpect(jsonPath("$.members",containsInAnyOrder(
                            s_1.getId().toString(),
                            s_2.getId().toString()
                            )));
    }

    /*5.2 Add*/
    /*5.2.1 add new group*/
    @Test
    @IfProfileValue(name="test-group", values = {"all", "group"})
    public void _5_2_1_addNewGroup() throws Exception {
        Group g = new Group();
        g.setName("新增测试组");
        g.setDescription("新增测试组描述");
        String groupId = LdapNameBuilder.newInstance(c_1.getId())
            .add("ou","groups")
            .add("cn",g.getName())
            .build().toString();
        try {
            RequestBuilder request = post("/api/v1.0/companies/"+
                    c_1.getId().toString()+
                    "/groups")
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(g));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",is(groupId)))
                .andExpect(jsonPath("$.name", is(g.getName())))
                .andExpect(jsonPath("$.description", is(g.getDescription())))
                .andExpect(jsonPath("$.company", is(c_1.getId().toString())))
                .andExpect(jsonPath("$.members").doesNotExist());
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            cleanNode(groupId);
        }
    }

    /*5.2.2 add exist group*/
    @Test
    @IfProfileValue(name="test-group", values = {"all", "group"})
    public void _5_2_2_addExistGroup() throws Exception {
        Group g = new Group();
        g.setName("测试组-1");
        try {
            RequestBuilder request = post("/api/v1.0/companies/"+
                    c_1.getId().toString()+
                    "/groups")
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(g));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$[0].message",is("object already exist")));
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
        }
    }


    /*5.2.3 add new group with members*/
    @Test
    @IfProfileValue(name="test-group", values = {"all", "group"})
    public void _5_2_3_addNewGroupWithMembers() throws Exception {
        Group g = new Group();
        g.setName("新增测试组(有成员)");
        g.setDescription("新增测试组(有成员)描述");
        g.addMember(s_1.getId());
        String groupId = LdapNameBuilder.newInstance(c_1.getId())
            .add("ou","groups")
            .add("cn",g.getName())
            .build().toString();
        try {
            RequestBuilder request = post("/api/v1.0/companies/"+
                    c_1.getId().toString()+
                    "/groups")
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(g));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",is(groupId)))
                .andExpect(jsonPath("$.name", is(g.getName())))
                .andExpect(jsonPath("$.description", is(g.getDescription())))
                .andExpect(jsonPath("$.company", is(c_1.getId().toString())))
                .andExpect(jsonPath("$.members",hasSize(1)))
                .andExpect(jsonPath("$.members[0]",is(s_1.getId().toString())));
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            cleanNode(groupId);
        }
    }


}


