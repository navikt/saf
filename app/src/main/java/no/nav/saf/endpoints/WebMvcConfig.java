package no.nav.saf.endpoints;

import no.nav.saf.exceptionhandler.LoggingExceptionResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	@Override
	public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
		LoggingExceptionResolver loggingExceptionResolver = new LoggingExceptionResolver();
		loggingExceptionResolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
		resolvers.add(loggingExceptionResolver);
		AnnotationAwareOrderComparator.sort(resolvers);
	}
}
