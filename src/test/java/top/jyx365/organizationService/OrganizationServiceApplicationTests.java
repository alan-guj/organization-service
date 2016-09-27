package top.jyx365.organizationService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import top.jyx365.organizationService.CompanyController;
import top.jyx365.organizationService.StaffController;

@RunWith(SpringRunner.class)
@WebMvcTest(CompanyController.class)
//@ContextConfiguration(locations = "/spring-config.xml")
//@ActiveProfiles("dev")
public class OrganizationServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    //@MockBean
    ////@Autowired
    //private OrganizationRepository repository;


    @Test
    public void testGetCompany() throws Exception {

        this.mockMvc.perform(get("/api/v1.0/companies")
                .header("Authorization","bearer FcwQwMtQCFeZCTsnIAYhzSRiDpM3nJ"))
            .andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void contextLoads() {
    }

}

