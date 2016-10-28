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
public class LocalityTests extends OrganizationServiceApplicationTests {

    public static class TestProfileValueSource implements ProfileValueSource {
        public String get(String key) {
            return "all";
        }
    }


    /*8. Locality*/
    /*8.1 Add*/
    /*8.1.1 Add new locality*/
    @Test
    @IfProfileValue(name="test-group", values = {"all","locality"})
    public void _8_1_1_addNewLocality() throws Exception {
        Locality l = new Locality();
        l.setName("test_add_locality");
        l.setDescription("test_add_locality_desc");
        l.setLocalityId("test_add_locality_localityId");
        String id = LdapNameBuilder.newInstance(c_1.getId())
            .add("ou","localities")
            .add("l",l.getName())
            .build().toString();
        String parent = LdapNameBuilder.newInstance(c_1.getId())
            .add("ou","localities")
            .build().toString();
        RequestBuilder request = post(
                "/api/v1.0/companies/"+
                c_1.getId().toString()+
                "/localities"
                )
            .contentType(CONTENT_TYPE)
            .header(AUTHORIZATION, ACCESS_TOKEN)
            .content(json(l));
        this.mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id",is(id)))
            .andExpect(jsonPath("$.description",is(l.getDescription())))
            .andExpect(jsonPath("$.localityId",is(l.getLocalityId())))
            .andExpect(jsonPath("$.name", is(l.getName())))
            .andExpect(jsonPath("$.parent", is(parent)))
            .andExpect(jsonPath("$.company", is(c_1.getId().toString())));
    }
    /*8.1.2 Add new sub locality*/
    @Test
    @IfProfileValue(name="test-group", values = {"all","locality"})
    public void _8_1_1_addNewSubLocality() throws Exception {
        Locality l = new Locality();
        l.setName("test_add_locality");
        l.setDescription("test_add_locality_desc");
        l.setLocalityId("test_add_locality_localityId");
        l.setParent(l_1.getId());
        String id = LdapNameBuilder.newInstance(c_1.getId())
            .add("ou","localities")
            .add("l",l_1.getName())
            .add("l",l.getName())
            .build().toString();
        String parent = l_1.getId().toString();
        RequestBuilder request = post(
                "/api/v1.0/companies/"+
                c_1.getId().toString()+
                "/localities"
                )
            .contentType(CONTENT_TYPE)
            .header(AUTHORIZATION, ACCESS_TOKEN)
            .content(json(l));
        this.mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id",is(id)))
            .andExpect(jsonPath("$.description",is(l.getDescription())))
            .andExpect(jsonPath("$.localityId",is(l.getLocalityId())))
            .andExpect(jsonPath("$.name", is(l.getName())))
            .andExpect(jsonPath("$.parent", is(parent)))
            .andExpect(jsonPath("$.company", is(l_1.getCompany().toString())));
    }

    /*8.2 Query*/
    /*8.2.1 get all Localities*/
    @Test
    @IfProfileValue(name="test-group", values = {"all","locality"})
    public void _8_2_1_getAllLocalities() throws Exception {
        RequestBuilder request = get(
                "/api/v1.0/companies/"+
                c_1.getId().toString()+
                "/localities"
                )
            .contentType(CONTENT_TYPE)
            .header(AUTHORIZATION, ACCESS_TOKEN);
        this.mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.localities",hasSize(3)));
    }

    /*8.2.2 get one Locality*/
    @Test
    @IfProfileValue(name="test-group", values = {"all","locality"})
    public void _8_2_2_getOneLocality() throws Exception {
        Locality l = l_2;
        RequestBuilder request = get(
                "/api/v1.0/companies/"+
                c_1.getId().toString()+
                "/localities/"+
                l.getId().toString()
                )
            .contentType(CONTENT_TYPE)
            .header(AUTHORIZATION, ACCESS_TOKEN);
        this.mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id",is(l.getId().toString())))
            .andExpect(jsonPath("$.name",is(l.getName())))
            .andExpect(jsonPath("$.description",is(l.getDescription())))
            .andExpect(jsonPath("$.parent",is(l.getParent().toString())))
            .andExpect(jsonPath("$.company",is(l.getCompany().toString())))
            .andExpect(jsonPath("$.localityId").doesNotExist());
    }

    /*8.2.3 get one Sub Locality*/
    @Test
    @IfProfileValue(name="test-group", values = {"all","locality"})
    public void _8_2_3_getOneSubLocality() throws Exception {
        Locality l = l_1_1;
        RequestBuilder request = get(
                "/api/v1.0/companies/"+
                c_1.getId().toString()+
                "/localities/"+
                l.getId().toString()
                )
            .contentType(CONTENT_TYPE)
            .header(AUTHORIZATION, ACCESS_TOKEN);
        this.mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id",is(l.getId().toString())))
            .andExpect(jsonPath("$.name",is(l.getName())))
            .andExpect(jsonPath("$.description",is(l.getDescription())))
            .andExpect(jsonPath("$.parent",is(l.getParent().toString())))
            .andExpect(jsonPath("$.company",is(l.getCompany().toString())))
            .andExpect(jsonPath("$.localityId",is(l.getLocalityId())));
    }



}


