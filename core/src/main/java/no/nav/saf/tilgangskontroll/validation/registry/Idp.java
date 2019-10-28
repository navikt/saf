package no.nav.saf.tilgangskontroll.validation.registry;

import lombok.Data;

/**
 * Class containing identity provider information.
 */
@Data
public class Idp {
	private String issuerUrl;
	private String jwksUrl;
	private String proxyUrl;
}