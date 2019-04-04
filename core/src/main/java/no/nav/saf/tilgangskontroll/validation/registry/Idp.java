package no.nav.saf.tilgangskontroll.validation.registry;

import lombok.Value;

/**
 * Class containing identity provider information.
 */
@Value
public class Idp {
	private String issuerUrl;
	private String jwksUrl;
}