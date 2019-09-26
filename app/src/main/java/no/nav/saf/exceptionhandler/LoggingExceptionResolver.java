package no.nav.saf.exceptionhandler;

import no.nav.saf.exceptions.SafFunctionalException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class LoggingExceptionResolver extends AbstractHandlerExceptionResolver {
	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		if(ex instanceof SafFunctionalException) {
			if(logger.isWarnEnabled()) {
				logger.warn(ex.getMessage(), ex);
			}
		} else {
			if(logger.isErrorEnabled()) {
				logger.error(ex.getMessage(), ex);
			}
		}
		return null;
	}
}
