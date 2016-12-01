package top.jyx365.organizationService;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.Name;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.Relation;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;


@Relation(collectionRelation="products", value="product")
class ProductResource extends ResourceSupport {
    private final Product prod;

    public ProductResource(Product prod){
        this.prod = prod;
    }

    @JsonProperty("id")
    public Name getDN() {
        return this.prod.getId();
    }

    public String getProductId() {
        return this.prod.getProductId();
    }

    public String getName() {
        return this.prod.getName();
    }

    public String getDescription() {
        return this.prod.getDescription();
    }

    public Name getCompany() {
        return this.prod.getCompany();
    }

    public String getProductName() {
        return this.prod.getProductName();
    }
}


class ProductResourceAssembler extends ResourceAssemblerSupport<Product, ProductResource>
{
    public ProductResourceAssembler() {
        super(ProductController.class, ProductResource.class);
    }

    @Override
    public ProductResource  toResource(Product prod) {
        ProductResource resource = createResourceWithId(
            prod.getId().toString(),prod,prod.getCompany());
        return resource;
    }

    @Override
    protected ProductResource instantiateResource(Product prod) {
        return new ProductResource(prod);
    }
}


@RestController
@EnableResourceServer
@RequestMapping("/api/v1.0/companies/{companyId}/products")
public class ProductController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ProductResourceAssembler assember = new ProductResourceAssembler();

    @Autowired
    private OrganizationRepository repository;

    @RequestMapping(method = RequestMethod.GET)
        public Resources<ProductResource> getProducts(
                @PathVariable String companyId,
                @RequestParam(required = false) String parent,
                @RequestParam(required = false, defaultValue="false") String recursive
                )
        {
            List<Product> result;
            Map<String, String> searchCondition = new HashMap<String, String>();
            if(!companyId.equals("**")) {
                searchCondition.put("o",companyId);
            }else {
                companyId=null;
            }
            if(parent == null) {
                result = repository.findCompanyProducts(companyId,searchCondition,true);
            } else {
                result = repository.findProducts(parent,searchCondition,recursive.equals("true"));
            }


            return new Resources<ProductResource>(
                    assember.toResources(result)
                    );
        }

    @RequestMapping(value="/{productId}",method = RequestMethod.GET)
        public ProductResource getProduct(
                @PathVariable String companyId,
                @PathVariable String productId
                )
        {
            return assember.toResource(
                    repository.findProduct(productId));
        }

    @RequestMapping(value="/{productId}",method = RequestMethod.DELETE)
        public void deleteProduct(
                @PathVariable String companyId,
                @PathVariable String productId
                )
        {
            Product prod = repository.findProduct(productId);
            repository.deleteProduct(prod);
        }

    @RequestMapping(method = RequestMethod.POST)
        public ResponseEntity<ProductResource> addProduct(
                @PathVariable String companyId,
                @RequestBody Product prod
                )
        {
            Company company = repository.findCompany(companyId);
            prod.setCompany(company.getId());
            repository.addProduct(prod);
            return new ResponseEntity<ProductResource>(
                    assember.toResource(prod),HttpStatus.CREATED
                    );
        }
    @RequestMapping(value="/{productId}", method = RequestMethod.PUT)
        public ProductResource updateProduct(
                @PathVariable String companyId,
                @PathVariable String productId,
                @RequestBody Product prod) {
            Product _prod = repository.findProduct(productId);
            _prod.setDescription(prod.getDescription());
            _prod.setProductId(prod.getProductId());
            _prod.setProductName(prod.getProductName());
            repository.updateProduct(_prod);
            return assember.toResource(_prod);
        }
}


