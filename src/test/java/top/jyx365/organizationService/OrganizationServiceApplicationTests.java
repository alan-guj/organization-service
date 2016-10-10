package top.jyx365.organizationService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
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
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
import org.springframework.web.context.WebApplicationContext;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import top.jyx365.organizationService.OrganizationServiceApplicationTests.TestProfileValueSource;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("development")
@ProfileValueSourceConfiguration(TestProfileValueSource.class)
public class OrganizationServiceApplicationTests {

    @ConfigurationProperties
    public static class TestProfileValueSource implements ProfileValueSource {
        public String get(String key) {
            return "group";
        }
    }


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MockMvc mockMvc;
    @SuppressWarnings("rawtypes")
    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private MediaType CONTENT_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    @Value("${access_token}")
    private String ACCESS_TOKEN;


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
    private Role r_2;
    private Group g_1, g_2;


    @Before
    public void setup() {
        /*Clear old data*/
        List<Company> companies = repository.getCompanies();
        companies.forEach(c->{
            ldapTemplate.unbind(c.getId(),true);
        });



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
        repository.addStaff(s_1);

        s_2 = new Staff();
        s_2.setName("staff2");
        s_2.setSurname("测试员工2");
        s_2.setMobile("123");
        s_2.setDescription("有部门员工");
        s_2.setCompany(c_1.getId());
        s_2.addDepartment(d_1.getId());
        repository.addStaff(s_2);

        r_2 = new Role();
        r_2.setName("director");
        r_2.setDescription("测试角色：部门2总监");
        r_2.setDepartment(d_2.getId().toString());
        r_2.addOccupant(s_1.getId().toString());
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
    }

    /*1. Test Company Service*/
    /*1.1 Add
    /*1.1.1 Test add a new company*/
    @Test
    @IfProfileValue(name="test-group", values={"all","add"})
    public void _1_1_1_testAddCompany() throws Exception {
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
            throw(e);
        } finally {
            repository.removeCompany("dc="+nc.getDomain());
        }
    }


    /*1.1.2 Test add an alreay exist company*/
    @Test
    @IfProfileValue(name="test-group", values={"all","company"})
    public void _1_1_2_testAddExistCompany() throws Exception {
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
    @IfProfileValue(name="test-group", values={"all","company"})
    public void _1_2_1_testGetCompanies() throws Exception {
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
            .andExpect(jsonPath("$._embedded.companies[1].departments").doesNotExist());

    }

    /*1.2.2 Test get an exist company*/
    @Test
    @IfProfileValue(name="test-group", values={"all","company"})
    public void _1_2_2_testGetOneCompany() throws Exception {
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
    @IfProfileValue(name="test-group", values={"all","company"})
    public void _1_2_3_testGetNonExistCompany() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/company/dc=nonexist")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isNotFound());
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
    @IfProfileValue(name="test-group", values={"all","department"})
    public void _2_1_2_addSecondLevelDept() throws Exception {
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
    @IfProfileValue(name="test-group", values={"all","department"})
    public void _2_1_3_addExistDept() throws Exception {
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
            .andExpect(jsonPath("$.parent",is(d_1_1.getParent())))
            .andExpect(jsonPath("$.company",is(d_1_1.getCompany())));
    }

    /*2.2.3 Get sub departments*/
    @Test
    @IfProfileValue(name="test-group", values={"all","department"})
    public void _2_2_3_getSubDept() throws Exception {
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


    /*3 Staff*/
    /*3.1 query */

    /*3.1.1 Get a non-department staff*/
    @Test
    @IfProfileValue(name="test-group", values={"all","staff"})
    public void _3_1_1_getOneNonDeptStaff() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s_1.getCompany().toString()+
                    "/staffs/"+
                    s_1.getId().toString())
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name",is(s_1.getName())))
            .andExpect(jsonPath("$.surname",is(s_1.getSurname())))
            .andExpect(jsonPath("$.mobile",is(s_1.getMobile())))
            .andExpect(jsonPath("$.id",is(
                            LdapNameBuilder.newInstance(s_1.getCompany())
                                .add("ou","staffs")
                                .add("cn",s_1.getName())
                                .build().toString()
                            )))
            .andExpect(jsonPath("$.company",is(s_1.getCompany().toString())))
            .andExpect(jsonPath("$.departments").doesNotExist());
    }

    /*3.1.2 Get a department staff*/
    @Test
    @IfProfileValue(name="test-group", values={"all","staff"})
    public void _3_1_2_getOneDeptStaff() throws Exception {
        Staff s = s_2;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/staffs/"+
                    s.getId().toString())
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name",is(s.getName())))
            .andExpect(jsonPath("$.surname",is(s.getSurname())))
            .andExpect(jsonPath("$.mobile",is(s.getMobile())))
            .andExpect(jsonPath("$.id",is(
                            LdapNameBuilder.newInstance(s.getCompany())
                                .add("ou","staffs")
                                .add("cn",s.getName())
                                .build().toString()
                            )))
            .andExpect(jsonPath("$.company",is(s.getCompany().toString())))
            .andExpect(jsonPath("$.departments",hasSize(1)))
            .andExpect(jsonPath("$.departments[0]",is(s.getDepartments().get(0).toString())));
    }

    /*3.1.3 Get all staffs*/
    @Test
    @IfProfileValue(name="test-group", values={"all","staff"})
    public void _3_1_3_getAllStaffs() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    c_1.getId().toString()+
                    "/staffs")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.staffs",hasSize(2)));
    }

    /*3.1.4 Get dept staffs*/
    @Test
    @IfProfileValue(name="test-group", values={"all","staff"})
    public void _3_1_4_getDeptStaffs() throws Exception {
        Staff s = s_2;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/departments/"+
                    s.getDepartments().get(0).toString()+
                    "/staffs")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.staffs",hasSize(1)))
            .andExpect(jsonPath("$._embedded.staffs[0].id",is(s.getId().toString())));
    }

    /*3.2 Add */

    /*3.2.1 add non-department staff*/
    @Test
    @IfProfileValue(name="test-group", values={"all","staff"})
    public void _3_2_1_addNonDeptStaff() throws Exception {
        Staff s = new Staff();
        s.setName("new_staff");
        s.setSurname("新增测试员工");
        String s_id = LdapNameBuilder.newInstance(c_1.getId())
                .add("ou","staffs")
                .add("cn",s.getName())
                .build().toString();

        try {
            RequestBuilder request = post(
                    "/api/v1.0/companies/"+
                    c_1.getId().toString()+
                    "/staffs"
                    )
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(s));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",is(s_id)))
                .andExpect(jsonPath("$.company", is(c_1.getId().toString())))
                .andExpect(jsonPath("$.departments").doesNotExist());
        } catch (Exception e) {
            throw(e);
        } finally {
            cleanNode(s_id);
        }

    }


    /*3.2.2 add department staff*/
    @Test
    @IfProfileValue(name="test-group", values={"all","staff"})
    public void _3_2_2_addDeptStaff() throws Exception {
        Staff s = new Staff();
        Department d = d_1;
        s.setName("new_dept_staff");
        s.setSurname("新增部门测试员工");
        String s_id = LdapNameBuilder.newInstance(d.getCompany())
                .add("ou","staffs")
                .add("cn",s.getName())
                .build().toString();

        try {
            RequestBuilder request = post(
                    "/api/v1.0/companies/"+
                    d.getCompany()+
                    "/departments/"+
                    d_1.getId().toString()+
                    "/staffs"
                    )
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(s));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",is(s_id)))
                .andExpect(jsonPath("$.company", is(d.getCompany())))
                .andExpect(jsonPath("$.departments", hasSize(1)))
                .andExpect(jsonPath("$.departments[0]", is(d.getId().toString())));
        } catch (Exception e) {
            throw(e);
        } finally {
            cleanNode(s_id);
        }
    }

    /*3.2.3 add multi-department staff */
    @Test
    @IfProfileValue(name="test-group", values={"all","staff"})
    public void _3_2_3_addMultiDeptStaff() throws Exception {
        Staff s = s_2;
        try {
            RequestBuilder request = post(
                    "/api/v1.0/companies/"+
                    d_1_2.getCompany()+
                    "/departments/"+
                    d_1_2.getId().toString()+
                    "/staffs"
                    )
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(s));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",is(s.getId().toString())))
                .andExpect(jsonPath("$.company", is(s.getCompany().toString())))
                .andExpect(jsonPath("$.departments", hasSize(2)))
                .andExpect(jsonPath("$.departments", containsInAnyOrder(
                                d_1.getId().toString(),
                                d_1_2.getId().toString()
                                )));
        } catch (Exception e) {
            throw(e);
        } finally {
        }
    }


    /*4 Role Service*/
    /*4.1 Add */

    /*4.1.1 Add a dept role*/
    @Test
    @IfProfileValue(name="test-group", values={"all","role"})
    public void _4_1_1_addDeptRole() throws Exception {
        Role role = new Role();
        Department dept = d_1_1;
        role.setDepartment(dept.getId().toString());
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
                .andExpect(jsonPath("$.department",is(role.getDepartment())))
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
            .andExpect(jsonPath("$._embedded.roles[0].department",is(r_2.getDepartment())))
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
            .andExpect(jsonPath("$.department",is(r_2.getDepartment())))
            .andExpect(jsonPath("$.occupants",hasSize(1)))
            .andExpect(jsonPath("$.occupants",containsInAnyOrder(
                            s_1.getId().toString()
                            )));
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

    @SuppressWarnings("unchecked")
    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage
                );
        return mockHttpOutputMessage.getBodyAsString();
    }
}


