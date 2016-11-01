package top.jyx365.organizationService;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev-int-test")
@ProfileValueSourceConfiguration(Configuration.class)
@Slf4j
public class CompanyTests extends OrganizationServiceApplicationTests {

    /*1. Test Company Service*/
    /*1.1 Add
    /*1.1.1 Test add a new company*/
    @Test
    @IfProfileValue(name="company-test-group", values={"all","company"})
    public void _1_1_1_testAddCompany() throws Exception {
        Company nc= new Company();
        nc.setName("测试新增企业");
        nc.setDomain("testcompany");
        nc.setDescription("测试增加企业描述");
        String id = "dc="+nc.getDomain();
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
                .andExpect(jsonPath("$.id",is("dc="+nc.getDomain())))
                .andExpect(jsonPath("$._links.departments.href",endsWith(id+"/departments")));
        } catch(Exception e) {
            throw(e);
        } finally {
        }
    }


    /*1.1.2 Test add an alreay exist company*/
    @Test
    @IfProfileValue(name="company-test-group", values={"all","company"})
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
    @IfProfileValue(name="company-test-group", values={"all","company"})
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
            .andExpect(jsonPath("$._embedded.companies[0]._links.departments.href",endsWith(testCompanies.get(0).getId().toString()+"/departments")))
            .andExpect(jsonPath("$._embedded.companies[1].id", is(testCompanies.get(1).getId().toString())))
            .andExpect(jsonPath("$._embedded.companies[1].name", is(testCompanies.get(1).getName())))
            .andExpect(jsonPath("$._embedded.companies[1].domain", is(testCompanies.get(1).getDomain())))
            .andExpect(jsonPath("$._embedded.companies[1]._links.departments.href",endsWith(testCompanies.get(1).getId().toString()+"/departments")))
            .andExpect(jsonPath("$._embedded.companies[1].departments").doesNotExist());

    }

    /*1.2.2 Test get an exist company*/
    @Test
    @IfProfileValue(name="company-test-group", values={"all","company"})
    public void _1_2_2_testGetOneCompany() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/"+c_1.getId())
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(testCompanies.get(0).getId().toString())))
            .andExpect(jsonPath("$.name", is(testCompanies.get(0).getName())))
            .andExpect(jsonPath("$.domain", is(testCompanies.get(0).getDomain())))
            .andExpect(jsonPath("$.description", is(testCompanies.get(0).getDescription())))
            .andExpect(jsonPath("$._links.departments.href",endsWith(testCompanies.get(0).getId().toString()+"/departments")));
    }
    /*1.2.3 Test get an not exist company*/
    @Test
    @IfProfileValue(name="company-test-group", values={"all","company"})
    public void _1_2_3_testGetNonExistCompany() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/company/dc=nonexist")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}


