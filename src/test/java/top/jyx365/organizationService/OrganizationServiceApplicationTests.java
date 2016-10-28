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


@EnableOAuth2Client
@Configuration
class OAuth2ClientConfigure {

    @Bean
    @ConfigurationProperties("security.oauth2.client")
    public ClientCredentialsResourceDetails resource() {
        return new ClientCredentialsResourceDetails();
    }

    @Bean
    public OAuth2RestOperations systemRestTemplate() {
        return new OAuth2RestTemplate(resource());
    }
}


public abstract class OrganizationServiceApplicationTests {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    protected MockMvc mockMvc;
    @SuppressWarnings("rawtypes")
    protected HttpMessageConverter mappingJackson2HttpMessageConverter;

    protected MediaType CONTENT_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    protected String ACCESS_TOKEN;


    protected String AUTHORIZATION = "Authorization";

    @Autowired
    protected OAuth2RestOperations systemRestTemplate;


    @Autowired
    protected OrganizationRepository repository;

    @Autowired
    protected LdapTemplate ldapTemplate;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
                hmc -> hmc instanceof MappingJackson2HttpMessageConverter
                ).findAny().get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);

    }

    protected List<Company> testCompanies = new ArrayList<Company>();

    protected Company c_1,c_2;
    protected Department d_1, d_1_1, d_1_2, d_2, d_3;
    protected Staff s_1, s_2;
    protected Role r_2;
    protected Group g_1, g_2;
    protected Staff s_a_1,s_i_1;
    protected Product p_1,p_2;
    protected Locality l_1,l_1_1,l_2;

    @Before
    public void setup() {
        /*Clear old data*/
        List<Company> companies = repository.getCompanies();
        companies.forEach(c->{
            ldapTemplate.unbind(c.getId(),true);
        });
        this.ACCESS_TOKEN ="bearer "+systemRestTemplate.getAccessToken();
        logger.info("ACCESS_TOKEN:{}",ACCESS_TOKEN);

        /*add test companies*/
        c_1 = new Company();
        c_1.setName("测试企业1");
        c_1.setDomain("company1");
        c_1.setDescription("测试企业1描述");
        repository.addCompany(c_1);
        testCompanies.add(c_1);
        c_2 = new Company();
        c_2.setName("测试企业2");
        c_2.setDomain("company2");
        repository.addCompany(c_2);
        testCompanies.add(c_2);


        /*Add test department*/
        d_1 = new Department();
        d_1.setName("test_deparment_1");
        d_1.setDescription("测试部门1描述");
        d_1.setCompany(c_1.getId());
        repository.addDepartment(d_1);

        d_1_1 = new Department();
        d_1_1.setName("test_deparment_1.1");
        d_1_1.setDescription("测试部门1.1描述");
        d_1_1.setParent(d_1.getId());
        repository.addDepartment(d_1_1);

        d_1_2 = new Department();
        d_1_2.setName("测试部门1.2");
        d_1_2.setDescription("测试部门1.2描述");
        d_1_2.setParent(d_1.getId());
        repository.addDepartment(d_1_2);

        d_2 = new Department();
        d_2.setName("测试部门2");
        d_2.setDescription("测试部门2描述");
        d_2.setCompany(c_1.getId());
        repository.addDepartment(d_2);

        d_3 = new Department();
        d_3.setName("测试部门3");
        d_3.setDescription("测试部门3描述");
        d_3.setCompany(c_2.getId());
        d_3.addBusinessCategory("locality:jiangsu;product:nasaichang");
        repository.addDepartment(d_3);

        /*Add test staffs*/
        s_1 = new Staff();
        s_1.setName("staff1");
        s_1.setSurname("测试员工1");
        s_1.setMobile("13851811909");
        s_1.setDescription("无部门员工");
        //s_1.addBusinessCategory("locality:jiangsu;product:nasaichang");
        //s_1.addBusinessCategory("locality:jiangsu1;product:nasaichang");
        s_1.setCompany(c_1.getId());
        repository.addStaff(s_1);

        s_2 = new Staff();
        s_2.setName("staff2");
        s_2.setSurname("测试员工2");
        s_2.setMobile("13813811111");
        s_2.setDescription("有部门员工");
        s_2.setCompany(c_1.getId());
        s_2.addDepartment(d_1.getId());
        repository.addStaff(s_2);

        s_a_1 = new Staff();
        s_a_1.setType("applicants");
        s_a_1.setName("applicant-1");
        s_a_1.setSurname("申请加入员工1");
        s_a_1.setMobile("123");
        s_a_1.setDescription("申请加入员工1");
        s_a_1.setCompany(c_1.getId());
        repository.addStaff(s_a_1);

        s_i_1 = new Staff();
        s_i_1.setType("invitees");
        s_i_1.setName("invitee-1");
        s_i_1.setSurname("邀请加入员工1");
        s_i_1.setMobile("123");
        s_i_1.setDescription("邀请加入员工1");
        s_i_1.setCompany(c_1.getId());
        repository.addStaff(s_i_1);


        r_2 = new Role();
        r_2.setName("director");
        r_2.setDescription("测试角色：部门2总监");
        r_2.setDepartment(d_2.getId());
        r_2.addOccupant(s_1.getId());
        repository.addRole(r_2);

        g_1 = new Group();
        g_1.setName("测试组-1");
        g_1.setDescription("测试组1描述");
        g_1.setCompany(c_1.getId());
        g_1.addMember(s_1.getId());
        g_1.addMember(s_2.getId());
        repository.addGroup(g_1);


        g_2 = new Group();
        g_2.setName("空测试组-2");
        g_2.setDescription("空测试组2描述");
        g_2.setCompany(c_1.getId());
        repository.addGroup(g_2);


        p_1 = new Product();
        p_1.setName("test_product_1");
        p_1.setDescription("test_product_1");
        p_1.setProductId("test_product_ID1");
        p_1.setCompany(s_1.getId());
        repository.addProduct(p_1);

        p_2 = new Product();
        p_2.setName("test_product_2");
        p_2.setDescription("test_product_2");
        p_2.setCompany(s_1.getId());
        repository.addProduct(p_2);

        l_1 = new Locality();
        l_1.setName("test_locality_1");
        l_1.setDescription("test_locality_1_desc");
        l_1.setLocalityId("test_locality_1_localId");
        l_1.setCompany(c_1.getId());
        repository.addLocality(l_1);

        l_1_1 = new Locality();
        l_1_1.setName("test_locality_1_1");
        l_1_1.setDescription("test_locality_1_1_desc");
        l_1_1.setLocalityId("test_locality_1_1_localId");
        l_1_1.setParent(l_1.getId());
        repository.addLocality(l_1_1);


        l_2 = new Locality();
        l_2.setName("test_locality_2");
        l_2.setDescription("test_locality_2_desc");
        l_2.setCompany(c_1.getId());
        repository.addLocality(l_2);

    }


    protected void cleanNode(String node) {
        try {
            ldapTemplate.unbind(node,true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void contextLoads() {
    }

    @SuppressWarnings("unchecked")
    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage
                );
        return mockHttpOutputMessage.getBodyAsString();
    }
}


