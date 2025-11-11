package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TilgangIdent {
	String identifikator;
	IdentType identType;
}
