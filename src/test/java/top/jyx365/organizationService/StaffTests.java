package top.jyx365.organizationService;


import lombok.extern.slf4j.Slf4j;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.ldap.NameNotFoundException;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev-int-test")
@ProfileValueSourceConfiguration(Configuration.class)
@Slf4j
public class StaffTests extends OrganizationServiceApplicationTests {

    /*3 Staff*/
    /*3.1 query */

    /*3.1.1 Get a non-department staff*/
    @Test
    @IfProfileValue(name="staff-test-group", values={"all","staff"})
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
            .andExpect(jsonPath("$.businessCategories",hasSize(1)))
            .andExpect(jsonPath("$.businessCategories[0].product",is(bc_1.getProduct())))
            .andExpect(jsonPath("$.businessCategories[0].locality",is(bc_1.getLocality())))
            .andExpect(jsonPath("$.departments").doesNotExist());
    }

    /*3.1.2 Get a department staff*/
    @Test
    @IfProfileValue(name="staff-test-group", values={"all","staff"})
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
    @IfProfileValue(name="staff-test-group", values={"all","staff"})
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
    @IfProfileValue(name="staff-test-group", values={"all","staff"})
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
    @IfProfileValue(name="staff-test-group", values={"all","staff"})
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
    @IfProfileValue(name="staff-test-group", values={"all","staff"})
    public void _3_1_6_getStaffsByName() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/**/staffs?name=staff2")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.staffs",hasSize(1)))
            .andExpect(jsonPath("$._embedded.staffs[0].id",is(s_2.getId().toString())));
    }

    /*3.1.7 query by uid*/
    @Test
    @IfProfileValue(name="staff-test-group", values={"all","staff"})
    public void _3_1_6_getStaffsByUid() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/**/staffs?uid=staff2_uid")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.staffs",hasSize(1)))
            .andExpect(jsonPath("$._embedded.staffs[0].id",is(s_2.getId().toString())))
            .andExpect(jsonPath("$._embedded.staffs[0].uid",is(s_2.getUid())));
    }

    /*3.2 Add */

    /*3.2.1 add non-department staff*/
    @Test
    @IfProfileValue(name="staff-test-group", values={"all","staff"})
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
    @IfProfileValue(name="staff-test-group", values={"all","staff"})
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
    @IfProfileValue(name="staff-test-group", values={"all","staff"})
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
    @IfProfileValue(name="staff-test-group", values={"all","staff"})
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
        thrown.expect(NameNotFoundException.class);
        repository.findStaff(s_1.getId());
    }

    /*3.4 update*/
    /*3.4.1 update an exist staff*/
    @Test
    @IfProfileValue(name="staff-test-group", values={"all","staff","update"})
    public void _3_4_1_updateExistStaff() throws Exception {
        Staff s = s_1;
        String _id = s.getId().toString();
        s.setSurname("修改测试员工1");
        s.setMobile("13851811111");
        s.setDescription("修改测试员工描述");
        s.addDepartment(d_1_2.getId());
        this.mockMvc.perform(put(PATH_PREFIX_v1+
                    s.getCompany()+
                    "/staffs/"+
                    _id
                    )
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(s))
                )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id",is(_id)))
            .andExpect(jsonPath("$.surname",is(s.getSurname())))
            .andExpect(jsonPath("$.mobile", is(s.getMobile())))
            .andExpect(jsonPath("$.description",is(s.getDescription())))
            .andExpect(jsonPath("$.departments",hasSize(1)))
            .andExpect(jsonPath("$.departments[0]",is(d_1_2.getId().toString())));
    }
    /*3.4.2 update the name of an exist staff,should ignore the name*/
    @Test
    @IfProfileValue(name="staff-test-group", values={"all","staff","update"})
    public void _3_4_2_updateNameOfExistStaff() throws Exception {
        Staff s = s_1;
        String _id = s.getId().toString();
        String _name = s.getName();
        s.setName("修改名称");
        s.setSurname("修改测试员工1");
        s.setMobile("13851811111");
        s.setDescription("修改测试员工描述");
        s.addDepartment(d_1_2.getId());
        this.mockMvc.perform(put(PATH_PREFIX_v1+
                    s.getCompany()+
                    "/staffs/"+
                    _id
                    )
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(s))
                )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id",is(_id)))
            .andExpect(jsonPath("$.name",is(_name)))
            .andExpect(jsonPath("$.surname",is(s.getSurname())))
            .andExpect(jsonPath("$.mobile", is(s.getMobile())))
            .andExpect(jsonPath("$.description",is(s.getDescription())))
            .andExpect(jsonPath("$.departments",hasSize(1)))
            .andExpect(jsonPath("$.departments[0]",is(d_1_2.getId().toString())));
        Staff _s = repository.findStaff(_id);
        assertNotNull(_s);
        assertTrue(_s.getName().equals(_name));
    }

    /*6. Applicant and Invitee*/
    /*6.1 Applicant*/
    /*6.1.1 add*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff"})
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

    /*6.1.2 Query*/
    /*6.1.2.1 get one*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff"})
    public void _6_1_2_1_getOneApplicant() throws Exception {
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
    /*6.1.2.2 query by mobile found*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff"})
    public void _6_1_2_2_getApplicantByMobile_found() throws Exception {
        Staff s = s_a_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/applicants?mobile=13813812345")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.applicants",hasSize(1)))
            .andExpect(jsonPath("$._embedded.applicants[0].id",is(s.getId().toString())));
    }
    /*6.1.2.2 query by mobile not found*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff"})
    public void _6_1_2_2_getApplicantByMobile_notFound() throws Exception {
        Staff s = s_a_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/applicants?mobile=1381345")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded").doesNotExist());
    }
    /*6.1.2.3 query by name found*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff"})
    public void _6_1_2_3_getApplicantByName_found() throws Exception {
        Staff s = s_a_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/applicants?name=applicant-1")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.applicants",hasSize(1)))
            .andExpect(jsonPath("$._embedded.applicants[0].id",is(s.getId().toString())));
    }
    /*6.1.2.4 query by name not found*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff"})
    public void _6_1_2_4_getApplicantByName_notFound() throws Exception {
        Staff s = s_a_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/applicants?name=nobody")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded").doesNotExist());
    }
    /*6.1.2.5 query by uid found*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff"})
    public void _6_1_2_5_getApplicantByUid_found() throws Exception {
        Staff s = s_a_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/applicants?uid=applicant-1-uid")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.applicants",hasSize(1)))
            .andExpect(jsonPath("$._embedded.applicants[0].id",is(s.getId().toString())));
    }

    /*6.1.2.6 query by uid not found*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff"})
    public void _6_1_2_6_getApplicantByUid_notFound() throws Exception {
        Staff s = s_a_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/applicants?uid=nobody")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded").doesNotExist());
    }


    /*6.1.3 approval*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff"})
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
    @IfProfileValue(name="staff-test-group", values = {"all","staff"})
    public void _6_1_4_delExistApplicant() throws Exception {
        this.mockMvc.perform(delete("/api/v1.0/companies/"+
                    s_a_1.getCompany()+
                    "/applicants/"+
                    s_a_1.getId())
                .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk());
        thrown.expect(NameNotFoundException.class);
        repository.findStaff(s_a_1.getId());
    }

    /*6.2 Invitee*/
    /*6.2.1 add*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff"})
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


    /*6.2.2.1 get one*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff"})
    public void _6_2_2_1_getOneInvitee() throws Exception {
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
    /*6.2.2.2 query by mobile found*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff","invitee"})
    public void _6_2_2_2_getInviteesByMobile_found() throws Exception {
        Staff s = s_i_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/invitees?mobile=invitee-1-mobile")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.invitees",hasSize(1)))
            .andExpect(jsonPath("$._embedded.invitees[0].id",is(s.getId().toString())));
    }
    /*6.2.2.2 query by mobile not found*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff","invitee"})
    public void _6_2_2_2_getInviteesByMobile_notFound() throws Exception {
        Staff s = s_i_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/invitees?mobile=notexistmobile")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded").doesNotExist());
    }
    /*6.2.2.3 query by name found*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff","invitee"})
    public void _6_2_2_3_getInviteesByName_found() throws Exception {
        Staff s = s_i_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/invitees?name=invitee-1")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.invitees",hasSize(1)))
            .andExpect(jsonPath("$._embedded.invitees[0].id",is(s.getId().toString())));
    }
    /*6.2.2.4 query by name not found*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff","invitee"})
    public void _6_2_2_4_getInviteesByName_notFound() throws Exception {
        Staff s = s_i_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/invitees?name=nobody")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded").doesNotExist());
    }
    /*6.2.2.5 query by uid found*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff","invitee"})
    public void _6_2_2_5_getInviteesByUid_found() throws Exception {
        Staff s = s_i_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/invitees?uid=invitee-1-uid")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.invitees",hasSize(1)))
            .andExpect(jsonPath("$._embedded.invitees[0].id",is(s.getId().toString())));
    }

    /*6.2.2.6 query by uid not found*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff","invitee"})
    public void _6_2_2_6_getInviteesByUid_notFound() throws Exception {
        Staff s = s_i_1;
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    s.getCompany().toString()+
                    "/invitees?uid=nobody")
                .header(AUTHORIZATION,ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded").doesNotExist());
    }


    /*6.2.3 confirm*/
    @Test
    @IfProfileValue(name="staff-test-group", values = {"all", "staff"})
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
    @IfProfileValue(name="staff-test-group", values = {"all","staff"})
    public void _6_2_4_delExistInvitee() throws Exception {
        this.mockMvc.perform(delete("/api/v1.0/companies/"+
                    s_i_1.getCompany()+
                    "/invitees/"+
                    s_i_1.getId())
                .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk());
        thrown.expect(NameNotFoundException.class);
        repository.findStaff(s_i_1.getId());
    }

}


