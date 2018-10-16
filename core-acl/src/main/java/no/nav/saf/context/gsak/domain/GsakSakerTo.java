package no.nav.saf.context.gsak.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GsakSakerTo {
	private final Integer id;
	private final String tema;
	private final String applikasjon;
	private final String aktoerId;
	private final String orgnr;
	private final String fagsakNr;
	private final String opprettetAv;
	private final String opprettetTidspunkt;
}
