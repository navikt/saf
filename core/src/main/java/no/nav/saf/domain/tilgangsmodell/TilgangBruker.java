package no.nav.saf.domain.tilgangsmodell;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangBruker {
	private final String foedselsnr;
	private final String aktoerId;
	private final String orgnummer;
	@Builder.Default
	private final List<TilgangIdent> historiskeIdenter = new ArrayList<>();
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private List<String> alleIdenter;

	public List<String> getAlleIdenter() {
		List<String> tmpAlleIdenter = historiskeIdenter.stream()
				.map(TilgangIdent::getIdentifikator)
				.collect(Collectors.toList());
		tmpAlleIdenter.add(foedselsnr);
		return tmpAlleIdenter;
	}

	public boolean isBrukerPerson() {
		return orgnummer == null;
	}
}
