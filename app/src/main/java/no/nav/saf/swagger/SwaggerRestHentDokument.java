package no.nav.saf.swagger;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
		@ApiResponse(code = 200, message = "OK."),
		@ApiResponse(code = 401, message = "Ingen tilgang til dokumentet."),
		@ApiResponse(code = 404, message = "Dokumentet ble ikke funnet i JOARK.")}
)
public @interface SwaggerRestHentDokument {
	@AliasFor(
			annotation = RequestMapping.class
	)
	String value() default "";
}
