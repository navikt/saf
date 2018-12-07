package no.nav.saf.domain.tilgangsmodell;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import no.nav.saf.tilgangskontroll.abstraction.SecModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangBruker implements SecModel {
	private final String foedselsnr;
	private final String aktoerId;
	@Builder.Default
	private final List<TilgangIdent> historiskeIdenter = new ArrayList<>();
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private List<String> alleIdenter;

	public List<String> getAlleIdenter() {
		List<String> alleIdenter = historiskeIdenter.stream()
				.map(TilgangIdent::getIdentifikator)
				.collect(Collectors.toList());
		alleIdenter.add(foedselsnr);
		return alleIdenter;
	}
}
