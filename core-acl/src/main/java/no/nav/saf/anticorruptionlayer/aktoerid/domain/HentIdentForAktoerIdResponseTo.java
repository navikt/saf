package no.nav.saf.anticorruptionlayer.aktoerid.domain;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
public class HentIdentForAktoerIdResponseTo {

	private final String foedselsnr;
	@Builder.Default
	private final List<String> historiskeIdenter = new ArrayList<>();

}
