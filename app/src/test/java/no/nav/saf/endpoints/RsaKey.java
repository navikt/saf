package no.nav.saf.endpoints;

import lombok.Data;
import org.jose4j.jwk.RsaJsonWebKey;

@Data
public class RsaKey {
	private final String issuer;
	private final RsaJsonWebKey webKey;
}
