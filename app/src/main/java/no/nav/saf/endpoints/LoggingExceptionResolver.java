package no.nav.saf.endpoints;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

@Component
public class LoggingExceptionResolver extends AbstractHandlerExceptionResolver {
	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		if (ex instanceof SafFunctionalException) {
			if (logger.isWarnEnabled()) {
				logger.warn(ex.getMessage(), ex);
			}
		} else if(ex instanceof SafTechnicalException) {
			logger.error(ex.getMessage(), ex);
		} else {
			if (logger.isErrorEnabled()) {
				logger.error("requestURI=" + request.getRequestURI() + "\n" + ex.getMessage(), ex);
			}
		}
		return null;
	}
}
