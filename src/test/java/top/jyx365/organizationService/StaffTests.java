package top.jyx365.organizationService;


import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
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
import static top.jyx365.organizationService.OrganizationServiceApplicationTests.TestProfileValueSource;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev-int-test")
@ProfileValueSourceConfiguration(TestProfileValueSource.class)
@Slf4j
public class StaffTests extends OrganizationServiceApplicationTests {

    public static class TestProfileValueSource implements ProfileValueSource {
        public String get(String key) {
            return "none";
        }
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
                    "/staffs?department="+
                    s.getDepartments().get(0).toString())
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.staffs",hasSize(1)))
            .andExpect(jsonPath("$._embedded.staffs[0].id",is(s.getId().toString())));
    }

    /*3.1.5 query by mobile*/
    @Test
    @IfProfileValue(name="test-group", values={"all","staff"})
    public void _3_1_5_getStaffsByMobile() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/**/staffs?mobile=13851811909")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.staffs",hasSize(1)))
            .andExpect(jsonPath("$._embedded.staffs[0].id",is(s_1.getId().toString())));
    }

    /*3.1.6 query by name*/
    @Test
    @IfProfileValue(name="test-group", values={"all","staff"})
    public void _3_1_6_getStaffsByName() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/**/staffs?name=staff2")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.staffs",hasSize(1)))
            .andExpect(jsonPath("$._embedded.staffs[0].id",is(s_2.getId().toString())));
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
        s.addDepartment(d.getId());
        String s_id = LdapNameBuilder.newInstance(d.getCompany())
                .add("ou","staffs")
                .add("cn",s.getName())
                .build().toString();

        try {
            RequestBuilder request = post(
                    "/api/v1.0/companies/"+
                    d.getCompany()+
                    "/staffs"
                    )
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(s));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",is(s_id)))
                .andExpect(jsonPath("$.company", is(d.getCompany().toString())))
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
    @Ignore
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

    /*3.3 Delete*/
    /*3.3.1 delete an exist staff*/
    @Test
    @IfProfileValue(name="test-group", values={"all","staff"})
    public void _3_3_1_delExistStaff() throws Exception {
        this.mockMvc.perform(delete(
                    "/api/v1.0/companies/"+
                    s_1.getCompany()+
                    "/staffs/"+
                    s_1.getId()
                    )
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                )
            .andDo(print())
            .andExpect(status().isOk());
        assertNull(repository.findStaff(s_1.getId()));
    }

    /*6. Applicant and Invitee*/
    /*6.1 Applicant*/
    /*6.1.1 add*/
    @Test
    @IfProfileValue(name="test-group", values = {"all", "staff"})
    public void _6_1_1_addNewApplicant() throws Exception {
        Staff s = new Staff();
        s.setName("new_applicant");
        s.setSurname("新增申请员工");
        s.setType("applicants");
        String s_id = LdapNameBuilder.newInstance(c_1.getId())
                .add("ou","applicants")
                .add("cn",s.getName())
                .build().toString();

        try {
            RequestBuilder request = post(
                    "/api/v1.0/companies/"+
                    c_1.getId().toString()+
                    "/applicants"
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

    /*6.1.2 get one*/
    @Test
    @IfProfileValue(name="test-group", values = {"all", "staff"})
    public void _6_1_2_getOneApplicant() throws Exception {
        Staff s = s_a_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/applicants/"+
                    s.getId().toString())
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name",is(s.getName())))
            .andExpect(jsonPath("$.surname",is(s.getSurname())))
            .andExpect(jsonPath("$.mobile",is(s.getMobile())))
            .andExpect(jsonPath("$.id",is(s.getId().toString())))
            .andExpect(jsonPath("$.company",is(s.getCompany().toString())));
    }

    /*6.1.3 approval*/
    @Test
    @IfProfileValue(name="test-group", values = {"all", "staff"})
    public void _6_1_3_approveApplicant() throws Exception {
        Staff s = s_a_1;
        String id = LdapNameBuilder.newInstance(s.getCompany())
            .add("ou","staffs")
            .add("cn",s.getName())
            .build().toString();
        try {
            RequestBuilder request = post(
                    "/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/applicants/"+
                    s.getId().toString()+
                    "/approval"
                    )
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN);
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",is(id)))
                .andExpect(jsonPath("$.company", is(c_1.getId().toString())))
                .andExpect(jsonPath("$.departments").doesNotExist());
            Staff as = repository.findStaff(id);
            assertEquals(as.getId().toString(),id);
            assertTrue(as.getId().toString().equals(id));
        } catch (Exception e) {
            throw(e);
        } finally {
        }

    }

    /*6.1.4 delete*/
    @Test
    @IfProfileValue(name="test-group", values = {"all","staff"})
    public void _6_1_4_delExistApplicant() throws Exception {
        this.mockMvc.perform(delete("/api/v1.0/companies/"+
                    s_a_1.getCompany()+
                    "/applicants/"+
                    s_a_1.getId())
                .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk());
        assertNull(repository.findStaff(s_a_1.getId()));
    }

    /*6.2 Invitee*/
    /*6.2.1 add*/
    @Test
    @IfProfileValue(name="test-group", values = {"all", "staff"})
    public void _6_2_1_addNewInvitee() throws Exception {
        Staff s = new Staff();
        s.setName("new_invitee");
        s.setSurname("新增邀请员工");
        s.setType("invitees");
        String s_id = LdapNameBuilder.newInstance(c_1.getId())
                .add("ou","invitees")
                .add("cn",s.getName())
                .build().toString();

        try {
            RequestBuilder request = post(
                    "/api/v1.0/companies/"+
                    c_1.getId().toString()+
                    "/invitees"
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
            throw e;
        } finally {
            cleanNode(s_id);
        }

    }


    /*6.2.2 get one*/
    @Test
    @IfProfileValue(name="test-group", values = {"all", "staff"})
    public void _6_2_2_getOneInvitee() throws Exception {
        Staff s = s_i_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/invitees/"+
                    s.getId().toString())
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name",is(s.getName())))
            .andExpect(jsonPath("$.surname",is(s.getSurname())))
            .andExpect(jsonPath("$.mobile",is(s.getMobile())))
            .andExpect(jsonPath("$.id",is(s.getId().toString())))
            .andExpect(jsonPath("$.company",is(s.getCompany().toString())));
    }

    /*6.2.3 confirm*/
    @Test
    @IfProfileValue(name="test-group", values = {"all", "staff"})
    public void _6_2_3_confirmInivtee() throws Exception {
        Staff s = s_i_1;
        String id = LdapNameBuilder.newInstance(s.getCompany())
            .add("ou","staffs")
            .add("cn",s.getName())
            .build().toString();
        try {
            RequestBuilder request = post(
                    "/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/invitees/"+
                    s.getId().toString()+
                    "/confirmation"
                    )
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN);
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",is(id)))
                .andExpect(jsonPath("$.company", is(c_1.getId().toString())))
                .andExpect(jsonPath("$.departments").doesNotExist());
            Staff as = repository.findStaff(id);
            assertEquals(as.getId().toString(),id);
        } catch (Exception e) {
            throw e;
        } finally {
        }
    }
    /*6.2.4 delete*/
    @Test
    @IfProfileValue(name="test-group", values = {"all","staff"})
    public void _6_2_4_delExistInvitee() throws Exception {
        this.mockMvc.perform(delete("/api/v1.0/companies/"+
                    s_i_1.getCompany()+
                    "/invitees/"+
                    s_i_1.getId())
                .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk());
        assertNull(repository.findStaff(s_i_1.getId()));
    }

}


