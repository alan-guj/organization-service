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
public class ProductTests extends OrganizationServiceApplicationTests {


    /*7. Product*/
    /*7.1 Add*/
    /*7.1.1 Add new product*/
    @Test
    @IfProfileValue(name="product-test-group", values = {"all", "product"})
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
    @IfProfileValue(name="product-test-group", values = {"all", "product"})
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
    @IfProfileValue(name="product-test-group", values = {"all", "product"})
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

    /*7.3 delete*/
    /*7.3.1 delete an exist product*/
    @Test
    @IfProfileValue(name="product-test-group", values = {"all","product"})
    public void _7_3_1_delExistProduct() throws Exception {
        this.mockMvc.perform(delete("/api/v1.0/companies/"+
                    p_1.getCompany()+
                    "/products/"+
                    p_1.getId())
                .header(AUTHORIZATION, ACCESS_TOKEN))
            .andDo(print())
            .andExpect(status().isOk());
        assertNull(repository.findProduct(p_1.getId()));
    }

    /*7.4 update*/
    /*7.4.1 update an exist product*/
    @Test
    @IfProfileValue(name="product-test-group", values = {"all", "upadte"})
    public void _7_4_1_updateExistProduct() throws Exception {
        String _id = p_1.getId().toString();
        p_1.setDescription("mod_test_product_1");
        p_1.setProductId("test_product_1_mod_desc");

        this.mockMvc.perform(put(PATH_PREFIX_v1+
                    p_1.getCompany()+
                    "/products/"+
                    _id)
                .contentType(CONTENT_TYPE)
                .header(AUTHORIZATION, ACCESS_TOKEN)
                .content(json(p_1)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id",is(_id)))
            .andExpect(jsonPath("$.description",is(p_1.getDescription())))
            .andExpect(jsonPath("$.productId",is(p_1.getProductId())));
    }
}


