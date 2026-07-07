package no.nav.saf.endpoints;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class TrailingSlashLoggingInterceptor implements HandlerInterceptor {

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
		String path = request.getRequestURI();
		if (path != null && path.length() > 1 && path.endsWith("/")) {
			log.info("Mottok request med trailing slash. method={}, path={}", request.getMethod(), path);
		}
	}
}