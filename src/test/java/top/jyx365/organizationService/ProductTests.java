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
public class ProductTests extends OrganizationServiceApplicationTests {

    public static class TestProfileValueSource implements ProfileValueSource {
        public String get(String key) {
            return "all";
        }
    }

    /*7. Product*/
    /*7.1 Add*/
    /*7.1.1 Add new product*/
    @Test
    @IfProfileValue(name="test-group", values = {"all", "product"})
    public void _7_1_1_addNewProduct() throws Exception {
        Product p = new Product();
        p.setName("test_add_product");
        p.setDescription("test_add_product_desc");
        p.setProductId("test_add_product_prodId");
        String id = LdapNameBuilder.newInstance(c_1.getId())
            .add("ou","products")
            .add("cn",p.getName())
            .build().toString();
        RequestBuilder request = post(
                "/api/v1.0/companies/"+
                c_1.getId().toString()+
                "/products"
                )
            .contentType(CONTENT_TYPE)
            .header(AUTHORIZATION, ACCESS_TOKEN)
            .content(json(p));
        this.mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id",is(id)))
            .andExpect(jsonPath("$.description",is(p.getDescription())))
            .andExpect(jsonPath("$.productId",is(p.getProductId())))
            .andExpect(jsonPath("$.name", is(p.getName())))
            .andExpect(jsonPath("$.company", is(c_1.getId().toString())));
    }


    /*7.2 Query*/
    /*7.2.1 Get all product*/
    @Test
    @IfProfileValue(name="test-group", values = {"all", "product"})
    public void _7_2_1_getAllProducts() throws Exception {
        RequestBuilder request = get(
                "/api/v1.0/companies/"+
                c_1.getId().toString()+
                "/products"
                )
            .contentType(CONTENT_TYPE)
            .header(AUTHORIZATION, ACCESS_TOKEN);
        this.mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.products",hasSize(2)))
            .andExpect(jsonPath("$._embedded.products[0].id",is(p_1.getId().toString())))
            .andExpect(jsonPath("$._embedded.products[0].name",is(p_1.getName())))
            .andExpect(jsonPath("$._embedded.products[0].description",is(p_1.getDescription())))
            .andExpect(jsonPath("$._embedded.products[0].productId",is(p_1.getProductId())))
            .andExpect(jsonPath("$._embedded.products[0].company",is(p_1.getCompany().toString())))
            .andExpect(jsonPath("$._embedded.products[1].id",is(p_2.getId().toString())))
            .andExpect(jsonPath("$._embedded.products[1].name",is(p_2.getName())))
            .andExpect(jsonPath("$._embedded.products[1].description",is(p_2.getDescription())))
            .andExpect(jsonPath("$._embedded.products[1].productId").doesNotExist())
            .andExpect(jsonPath("$._embedded.products[1].company",is(p_2.getCompany().toString())));
    }

    /*7.2.2 Get one product*/
    @Test
    @IfProfileValue(name="test-group", values = {"all", "product"})
    public void _7_2_2_getOneProduct() throws Exception {
        Product p = p_2;
        RequestBuilder request = get(
                "/api/v1.0/companies/"+
                c_1.getId().toString()+
                "/products/"+
                p.getId().toString()
                )
            .contentType(CONTENT_TYPE)
            .header(AUTHORIZATION, ACCESS_TOKEN);
        this.mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id",is(p.getId().toString())))
            .andExpect(jsonPath("$.name",is(p.getName())))
            .andExpect(jsonPath("$.description",is(p.getDescription())))
            .andExpect(jsonPath("$.company",is(p.getCompany().toString())))
            .andExpect(jsonPath("$.productId").doesNotExist());
    }


}


