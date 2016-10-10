package top.jyx365.organizationService;

import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.builders.PathSelectors.ant;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.async.DeferredResult;

import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationCodeGrant;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.service.TokenEndpoint;
import springfox.documentation.service.TokenRequestEndpoint;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * SwaggerConfig
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Value("${security.oauth2.client.clientId}")
	private String authClientId;

	@Value("${security.oauth2.client.clientSecret}")
	private String authClientSecret;

	//@Value("${security.userOauth.type}")
	private final String type = "oauth2";

	@Value("${security.oauth2.client.userAuthorizationUri}")
	private String authorizationUrl;

	@Value("${security.oauth2.client.accessTokenUri}")
	private String tokenUrl;

	@Value("${security.oauth2.client.tokenName}")
	private String tokenName;

	@Value("${security.oauth2.client.scope}")
	private String scopeCode;

	@Value("${security.oauth2.client.scope}")
	private String scopeDesc;

	@Value("${app.key}")
	private String appKey;

	@Value("${app.name}")
	private String appName;

	@Value("${app.desc}")
	private String appDesc;

	@Value("${app.version}")
	private String appVersion;

	@Value("${app.termsOfServiceUrl}")
	private String termsOfServiceUrl;

	@Value("${app.contact.name}")
	private String contactName;

	@Value("${app.contact.url}")
	private String contactUrl;

	@Value("${app.contact.email}")
	private String contactEmail;

	@Value("${app.license}")
	private String license;

	@Value("${app.licenseUrl}")
	private String licenseUrl;

	/**
	 * 可以定义多个组，比如本类中定义把test和demo区分开了 （访问页面就可以看到效果了）
	 *
	 */
	@Bean
	public Docket jyxApi() {
		return new Docket(DocumentationType.SWAGGER_2).groupName(appName)
				.genericModelSubstitutes(DeferredResult.class)
				.useDefaultResponseMessages(false)
				.forCodeGeneration(true)
				.pathMapping("/")
				.select()
				.apis(RequestHandlerSelectors.basePackage("top.jyx365.organizationService"))
				.build()
				.securitySchemes(newArrayList(oauth()))
				.securityContexts(newArrayList(securityContext()))
				.apiInfo(jyxApiInfo());
	}

	private ApiInfo jyxApiInfo() {
		ApiInfo apiInfo = new ApiInfo(appName, appDesc, appVersion, termsOfServiceUrl,
				new Contact(contactName, contactUrl, contactEmail),
				license, licenseUrl);
		return apiInfo;
	}

	 @Bean
	SecurityScheme apiKey() {
	        return new ApiKey(appName, appKey, "header");
	}

	@Bean
	SecurityContext securityContext() {
		AuthorizationScope[] scopes = new AuthorizationScope[]{new AuthorizationScope(scopeCode, scopeDesc)};

		SecurityReference securityReference = SecurityReference
				.builder()
				.reference(type)
				.scopes(scopes)
				.build();

		return SecurityContext
				.builder()
				.securityReferences(newArrayList(securityReference))
				.forPaths(ant("/api/**"))
				.build();
	}

	@Bean
	SecurityScheme oauth() {
		return new OAuthBuilder()
				.name(type)
				.grantTypes(grantTypes())
				.scopes(scopes())
				.build();
	}

	List<AuthorizationScope> scopes() {
		return newArrayList(new AuthorizationScope(scopeCode, scopeDesc));
	}

	List<GrantType> grantTypes() {
		List<GrantType> grants = newArrayList(new AuthorizationCodeGrant(
				new TokenRequestEndpoint(authorizationUrl, authClientId, authClientSecret),
				new TokenEndpoint(tokenUrl, tokenName)));
		return grants;
	}

	@Bean
	public SecurityConfiguration securityInfo() {
		return new SecurityConfiguration(authClientId, authClientSecret, scopeCode,
				appKey, appKey, ApiKeyVehicle.HEADER, "", ",");
	}

}
