package top.jyx365.organizationService;

import lombok.extern.slf4j.Slf4j;
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


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev-int-test")
@ProfileValueSourceConfiguration(Configuration.class)
@Slf4j
public class GroupTests extends OrganizationServiceApplicationTests {
    /*5. Group*/
    /*5.1 Query*/
    /*5.1.1 get all groups*/
    @Test
    @IfProfileValue(name="group-test-group", values = {"all","group"})
    public void _5_1_1_getAllGroups() throws Exception {
        this.mockMvc.perform(get("/api/v1.0/companies/"+
                    c_1.getId().toString()+
                    "/groups")
                .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.groups",hasSize(3)))
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
    @IfProfileValue(name="group-test-group", values = {"all","group"})
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

    /*5.1.3 Query group by name*/
    @Test
    @IfProfileValue(name="group-test-group", values = {"all","group"})
    public void _5_1_3_findGroupByName() throws Exception {
        this.mockMvc.perform(get(PATH_PREFIX_v1+
                    c_1.getId().toString()+
                    "/groups?name="+g_1.getName())
                .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.groups",hasSize(1)))
            .andExpect(jsonPath("$._embedded.groups[0].id",is(g_1.getId().toString())));
    }

    /*5.1.4 Query group by member*/
    @Test
    @IfProfileValue(name="group-test-group", values = {"all","group"})
    public void _5_1_4_findGroupByMember() throws Exception {
        this.mockMvc.perform(get(PATH_PREFIX_v1+
                    c_1.getId().toString()+
                    "/groups?member="+s_1.getId().toString())
                .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.groups",hasSize(2)))
            .andExpect(jsonPath("$._embedded.groups[*].id",containsInAnyOrder(
                            g_1.getId().toString(),
                            g_3.getId().toString()
                            )));
    }

    /*5.2 Add*/
    /*5.2.1 add new group*/
    @Test
    @IfProfileValue(name="group-test-group", values = {"all", "group"})
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
    @IfProfileValue(name="group-test-group", values = {"all", "group"})
    public void _5_2_2_addExistGroup() throws Exception {
        Group g = new Group();
        g.setName(g_1.getName());
        try {
            RequestBuilder request = post("/api/v1.0/companies/"+
                    c_1.getId().toString()+
                    "/groups")
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(g));
            this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isConflict());
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
        }
    }


    /*5.2.3 add new group with members*/
    @Test
    @IfProfileValue(name="group-test-group", values = {"all", "group"})
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

    /*5.3 delete*/
    /*5.3.1 delete an exist Group*/
    @Test
    @IfProfileValue(name = "group-test-group", values = {"all","delete"})
    public void _5_3_1_delExistGroup() throws Exception {
        this.mockMvc.perform(delete(PATH_PREFIX_v1+
                    g_1.getCompany()+
                    "/groups/"+
                    g_1.getId())
                .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk());
        assertNull(repository.findGroup(g_1.getId()));
    }

    /*5.4 update*/
    /*5.4.1 update an exist Group*/
    @Test
    @IfProfileValue(name = "group-test-group", values = {"all","update"})
    public void _5_4_1_updateExistGroup() throws Exception {
        String _id = g_1.getId().toString();

        Group g = g_1;
        g.setDescription("修改测试组-1");
        g.removeMember(s_1.getId());

        this.mockMvc.perform(put(PATH_PREFIX_v1+
                    g.getCompany()+
                    "/groups/"+
                    _id)
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(g)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id",is(_id)))
            .andExpect(jsonPath("$.description",is(g.getDescription())))
            .andExpect(jsonPath("$.members",hasSize(1)))
            .andExpect(jsonPath("$.members[0]",is(s_2.getId().toString())));
    }

}


