package no.nav.saf.domain.visningsmodell;

import lombok.Value;

@Value
public class Bruker {
	private final String id;
	private final BrukerIdType type;
}
