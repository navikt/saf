package no.nav.saf.endpoints;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new TrailingSlashLoggingInterceptor())
				.addPathPatterns("/**");
		registry.addInterceptor(new MDCClearHandlerInterceptor());
	}

	@Override
	public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
		LoggingExceptionResolver loggingExceptionResolver = new LoggingExceptionResolver();
		loggingExceptionResolver.setOrder(HIGHEST_PRECEDENCE + 1);
		resolvers.add(loggingExceptionResolver);
		AnnotationAwareOrderComparator.sort(resolvers);
	}
}
