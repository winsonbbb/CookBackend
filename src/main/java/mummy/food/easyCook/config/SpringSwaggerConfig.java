package mummy.food.easyCook.config;

import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableSwagger2
public class SpringSwaggerConfig {
  private static final String SECURITY_SCHEMA_JWT = "JWT";
  private static final List<ResponseMessage> responses = Arrays.asList(
    new ResponseMessageBuilder().code(400).message("Error Response").build(),
    new ResponseMessageBuilder().code(401).message("Unauthorized").build(),
    new ResponseMessageBuilder().code(403).message("Forbidden").build()
  );
  private static final AuthorizationScope[] scopes = new AuthorizationScope[] {
    new AuthorizationScopeBuilder().scope("staff").description("staff").build(),
    new AuthorizationScopeBuilder().scope("agent").description("agent user").build()
  };
  @Autowired(required = false)
  BuildProperties buildProperties;

  @Bean
  Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
      .select()
      .apis(RequestHandlerSelectors.any())
      .paths(
        Predicates.not(PathSelectors.regex("/actuator.*|/error.*"))
      )
      .build()
      .useDefaultResponseMessages(false)
      .globalResponseMessage(RequestMethod.GET, responses)
      .globalResponseMessage(RequestMethod.POST, responses)
      .apiInfo(apiInfo())
//      .tags(
//        new Tag(Constant.AUTH_NAME, Constant.AUTH_DESCRIPTION, 10),
//        new Tag(Constant.AGENT_NAME, Constant.AGENT_DESCRIPTION, 20),
//        new Tag(Constant.AUDIT_TRAIL_NAME, Constant.AUDIT_TRAIL_DESCRIPTION, 30),
//        new Tag(Constant.HOME_NAME, Constant.HOME_DESCRIPTION, 40),
//        new Tag(Constant.POST_NAME, Constant.POST_DESCRIPTION, 50),
//        new Tag(Constant.NOTIFICATION_NAME, Constant.NOTIFICATION_DESCRIPTION, 60)
//      )
      .securitySchemes(jwtSchema())
      .securityContexts(jwtSecurityContext());
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
//      .title(Constant.SWAGGER_API_TITLE)
      .version(((buildProperties == null) || (buildProperties.getVersion() == null) ? "N/A" : buildProperties.getVersion()))
      .build();
  }

  // for JWT
  private List<SecurityScheme> jwtSchema() {
    List<SecurityScheme> schema = new ArrayList<>();
    schema.add(new ApiKey(SECURITY_SCHEMA_JWT, HttpHeaders.AUTHORIZATION, "header"));
    return schema;
  }

  private List<SecurityContext> jwtSecurityContext() {
    List<SecurityContext> context = new ArrayList<>();
    List<SecurityReference> securityReferences = new ArrayList<>();
    securityReferences.add(new SecurityReference(SECURITY_SCHEMA_JWT, scopes));
    context.add(SecurityContext.builder()
      .forPaths(Predicates.not(PathSelectors.regex("/version|/token|/auth.*|/public/.*")))
      .securityReferences(securityReferences).build());
    return context;
  }

  @Bean
  SecurityConfiguration security() {
    return SecurityConfigurationBuilder.builder().scopeSeparator(",")
//      .appName(Constant.SWAGGER_APP_NAME)
      .appName("Cooking mama")
      .build();
  }
}
