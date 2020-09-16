package no.nav.saf.swagger;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.TagsSorter;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

import static springfox.documentation.builders.PathSelectors.regex;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ConditionalOnProperty(
        value = {"swagger.enabled"},
        havingValue = "true"
)
@Configuration
@EnableSwagger2
public class SwaggerConfig implements WebMvcConfigurer {

    @Value("${APP_VERSION:0.0.0}")
    private String version;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(regex("/rest.*"))
                .build()
                .useDefaultResponseMessages(false)
                .apiInfo(apiInfo())
                .securitySchemes(Lists.newArrayList(apiKey()));
    }

    @Bean
    UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .displayOperationId(false)
                .defaultModelsExpandDepth(1)
                .defaultModelExpandDepth(1)
                .defaultModelRendering(ModelRendering.EXAMPLE)
                .displayRequestDuration(false)
                .docExpansion(DocExpansion.NONE)
                .filter(false)
                .maxDisplayedTags(null)
                .operationsSorter(OperationsSorter.ALPHA)
                .showExtensions(false)
                .tagsSorter(TagsSorter.ALPHA)
                .validatorUrl(null)
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "SAF REST API",
                "Her dokumenteres REST tjenestegrensesnittet til sak og arkivfasade (SAF). <br/><br/>" +
                        "Til autentisering brukes OIDC-token (JWT via OAuth2.0). " + "Følgende format må brukes i Authorize sitt input-felt \"Value\": <strong> Bearer {token} </strong>. " +
                        "Eksempel på verdi i input-feltet: <strong> Bearer eYdmifml0ejugm </strong>. Et gyldig token kommer til å ha mange flere karakterer enn i eksempelet.<br/><br/>" +
                        "Tokens for manuell test kan hentes fra <a href=\"https://ida.adeo.no/\">IDA</a>. For maskinell test og produksjon kan tokens komme fra Azure V2, NAV REST-STS eller OpenAM.",
                version,
                "",
                new Contact("Team Dokumentløsninger", "https://nav-it.slack.com/archives/C6W9E5GPJ", "teamdokumenthandtering@nav.no"),
                "", "", new ArrayList<>());
    }

    private ApiKey apiKey() {

        return new ApiKey("apiKey", HttpHeaders.AUTHORIZATION, "header");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(false);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/swagger-ui/")
                .setViewName("forward:" + "/swagger-ui/index.html");
    }
}
