package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TilgangIdent {
	private final String identifikator;
	private final IdentType identType;
}
