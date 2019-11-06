package no.nav.saf.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.springframework.stereotype.Component;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component("applikasjonslogg")
@Slf4j
public class ApplikasjonsLogger extends AbacLogger {
	public void logAbacDeny(final XacmlRequest xacmlRequest, final XacmlResponse xacmlResponse) {
		log.info("{}. {};", sanitizeFnr(mapRequest(xacmlRequest)), sanitizeFnr(mapResponse(xacmlResponse)));
	}

	public void logAbacPermit(final XacmlRequest xacmlRequest, final XacmlResponse xacmlResponse) {
		log.debug("{}. {};", sanitizeFnr(mapRequest(xacmlRequest)), sanitizeFnr(mapResponse(xacmlResponse)));
	}
}
