package no.nav.saf.anticorruptionlayer.aktoer.domain;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class HentIdentForAktoerIdListeResponseTo {

	private final String foedselsnr;
	private final String aktoerId;
	@Builder.Default
	private final List<String> historiskeIdenter = new ArrayList<>();
}
