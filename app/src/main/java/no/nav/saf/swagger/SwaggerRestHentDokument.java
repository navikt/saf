package no.nav.saf.swagger;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ConditionalOnProperty(
		value = {"swagger.enabled"},
		havingValue = "true"
)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK.", response = void.class, responseHeaders =  @ResponseHeader(name = "Content-type", description = "Markør for dokumentets variantformat, f.eks PDF, PNG.", response = String.class)),
		@ApiResponse(code = 401, message = "Ingen tilgang til dokumentet."),
		@ApiResponse(code = 404, message = "Dokumentet ble ikke funnet.")}
)
public @interface SwaggerRestHentDokument {
	@AliasFor(
			annotation = RequestMapping.class
	)
	String value() default "";
}
