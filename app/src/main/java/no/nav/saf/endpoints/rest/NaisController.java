package no.nav.saf.endpoints.rest;


import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementerer NAIS kontraktene for
 * - readiness
 * - aliveness
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Hidden
@Slf4j
@RestController
public final class NaisController {

	private static final String APPLICATION_ALIVE = "Application is alive!";
	private static final String APPLICATION_READY = "Application is ready for traffic!";
	private static final AtomicInteger IS_READY = new AtomicInteger(1);

	@Autowired
	public NaisController(MeterRegistry meterRegistry) {
		Gauge.builder("dok_app_is_ready", IS_READY, AtomicInteger::get).register(meterRegistry);
	}

	@GetMapping("/isAlive")
	@Unprotected
	public String isAlive() {
		return APPLICATION_ALIVE;
	}

	@ResponseBody
	@RequestMapping(value = "/isReady", produces = MediaType.TEXT_HTML_VALUE)
	@Unprotected
	public ResponseEntity<String> isReady() {
		return new ResponseEntity<>(APPLICATION_READY, HttpStatus.OK);
	}
}
