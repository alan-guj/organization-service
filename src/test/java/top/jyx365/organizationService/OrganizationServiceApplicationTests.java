package top.jyx365.organizationService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("development")
public class OrganizationServiceApplicationTests {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MockMvc mockMvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private MediaType CONTENT_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private String ACCESS_TOKEN = "bearer SUSNxAXJJW8pZLkUtPgXBW89cBtUD4";
    private String AUTHORIZATION = "Authorization";

    @Autowired
    private OrganizationRepository repository;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
                hmc -> hmc instanceof MappingJackson2HttpMessageConverter
                ).findAny().get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);

    }

    private List<Company> testCompanies = new ArrayList<Company>();

    private Company c_1,c_2;
    private Department d_1, d_1_1, d_1_2, d_2, d_3;
    private Staff s_1, s_2;


    @Before
    public void setup() {
        /*Clear old data*/
        List<Company> companies = repository.getCompanies();
        logger.debug("company number:{}",companies.size());
        companies.forEach(c->{
            ldapTemplate.unbind(c.getId(),true);
        });



        /*add test companies*/
        c_1 = new Company();
        c_1.setName("测试企业1");
        c_1.setDomain("company1");
        repository.addCompany(c_1);
        testCompanies.add(c_1);
        c_2 = new Company();
        c_2.setName("测试企业2");
        c_2.setDomain("company2");
        repository.addCompany(c_2);
        testCompanies.add(c_2);


        /*Add test department*/
        d_1 = new Department();
        d_1.setName("测试部门1");
        d_1.setDescription("测试部门1描述");
        d_1.setCompany(c_1.getId().toString());
        repository.addDepartment(d_1);

        d_1_1 = new Department();
        d_1_1.setName("测试部门1.1");
        d_1_1.setDescription("测试部门1.1描述");
        d_1_1.setParent(d_1.getId().toString());
        repository.addDepartment(d_1_1);

        d_1_2 = new Department();
        d_1_2.setName("测试部门1.2");
        d_1_2.setDescription("测试部门1.2描述");
        d_1_2.setParent(d_1.getId().toString());
        repository.addDepartment(d_1_2);

        d_2 = new Department();
        d_2.setName("测试部门2");
        d_2.setDescription("测试部门2描述");
        d_2.setCompany(c_1.getId().toString());
        repository.addDepartment(d_2);

        d_3 = new Department();
        d_3.setName("测试部门3");
        d_3.setDescription("测试部门3描述");
        d_3.setCompany(c_2.getId().toString());
        repository.addDepartment(d_3);

        /*Add test staffs*/
        s_1 = new Staff();
        s_1.setName("staff1");
        s_1.setSurname("测试员工1");
        s_1.setMobile("123");
        s_1.setDescription("无部门员工");
        s_1.setCompany(c_1.getId());

        s_2 = new Staff();
        s_2.setName("staff2");
        s_2.setSurname("测试员工2");
        s_2.setMobile("123");
        s_2.setDescription("有部门员工");
        s_2.setCompany(c_1.getId());
        s_2.setDepartment(d_1_2.getId());

    }

    /*1. Test Company Service*/
    /*1.1 Add
    /*1.1.1 Test add a new company*/
    @Test
    public void testAddCompany() throws Exception {
        Company nc= new Company();
        nc.setName("测试新增企业");
        nc.setDomain("testcompany");
        nc.setDescription("测试增加企业描述");
        try {
            RequestBuilder request = post("/api/v1.0/companies")
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(nc));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name",is(nc.getName())))
                .andExpect(jsonPath("$.description",is(nc.getDescription())))
                .andExpect(jsonPath("$.domain",is(nc.getDomain())))
                .andExpect(jsonPath("$.id",is("dc="+nc.getDomain())));
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            repository.removeCompany("dc="+nc.getDomain());
        }
    }


    /*1.1.2 Test add an alreay exist company*/
    @Test
    public void testAddExistCompany() throws Exception {
        Company nc= new Company();
        nc.setName("测试新增重名企业");
        nc.setDomain(c_1.getDomain());
        nc.setDescription("测试增加重名企业描述");
        try {
            RequestBuilder request = post("/api/v1.0/companies")
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(nc));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$[0].message",is("object already exist")));
        } catch(Exception e) {
            throw(e);
        } finally {
        }
    }

    /*1.2 Query
    /*1.2.1 Test get all companies*/
    @Test
    public void testGetCompanies() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.companies",hasSize(2)))
            .andExpect(jsonPath("$._embedded.companies[0].id", is(testCompanies.get(0).getId().toString())))
            .andExpect(jsonPath("$._embedded.companies[0].name", is(testCompanies.get(0).getName())))
            .andExpect(jsonPath("$._embedded.companies[0].domain", is(testCompanies.get(0).getDomain())))
            .andExpect(jsonPath("$._embedded.companies[0].description", is(testCompanies.get(0).getDescription())))
            .andExpect(jsonPath("$._embedded.companies[1].id", is(testCompanies.get(1).getId().toString())))
            .andExpect(jsonPath("$._embedded.companies[1].name", is(testCompanies.get(1).getName())))
            .andExpect(jsonPath("$._embedded.companies[1].domain", is(testCompanies.get(1).getDomain())))
            .andExpect(jsonPath("$._embedded.companies[1].description", is(testCompanies.get(1).getDescription())));

    }

    /*1.2.2 Test get an exist company*/
    @Test
    public void testGetOneCompany() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/"+c_1.getId())
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(testCompanies.get(0).getId().toString())))
            .andExpect(jsonPath("$.name", is(testCompanies.get(0).getName())))
            .andExpect(jsonPath("$.domain", is(testCompanies.get(0).getDomain())))
            .andExpect(jsonPath("$.description", is(testCompanies.get(0).getDescription())));
    }

    /*1.2.3 Test get an not exist company*/
    @Test
    public void testGetNonExistCompany() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/company/dc=nonexist")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isNotFound());
    }



    /*2. Department Service*/
    /*2.1 Add */
    /*2.1.1 Add new 1st level department*/
    @Test
    public void addFirstLevelDept() throws Exception {
        Department dept = new Department();
        dept.setName("testadddept");
        dept.setDescription("测试新增一级部门");
        dept.setCompany(c_1.getId().toString());
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
    public void addSecondLevelDept() throws Exception {
        Department dept = new Department();
        dept.setName("testadd2nddept");
        dept.setDescription("测试新增二级部门");
        dept.setParent(d_1.getId().toString());
        try {
            RequestBuilder request = post(
                    "/api/v1.0/companies/"+
                    d_1.getCompany()+
                    "/departments/"+
                    d_1.getId().toString()+
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
                .andExpect(jsonPath("$.company",is(d_1.getCompany())))
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
    public void addExistDept() throws Exception {
        Department dept = new Department();
        dept.setName(d_1_1.getName());
        dept.setDescription("测试新增重复部门");
        dept.setParent(d_1.getId().toString());
        try {
            RequestBuilder request = post(
                    "/api/v1.0/companies/"+
                    d_1.getCompany()+
                    "/departments/"+
                    d_1.getId().toString()+
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
    public void getAllDept() throws Exception {
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
    public void getDept() throws Exception {
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
            .andExpect(jsonPath("$.parent",is(d_1_1.getParent())))
            .andExpect(jsonPath("$.company",is(d_1_1.getCompany())));
    }

    /*2.2.3 Get sub departments*/
    @Test
    public void getSubDept() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    d_1.getCompany()+
                    "/departments/"+
                    d_1.getId().toString()+
                    "/departments")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.departments",hasSize(2)))
            .andExpect(jsonPath("$._embedded.departments[0].parent",is(d_1.getId().toString())));
    }

    private void cleanNode(String node) {
        try {
            ldapTemplate.unbind(node,true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void contextLoads() {
    }

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage
                );
        return mockHttpOutputMessage.getBodyAsString();
    }
}

