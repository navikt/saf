package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangBruker {
	private final String foedselsnr;
	private final String aktoerId;
	@Builder.Default
	private final List<TilgangIdent> historiskeIdenter = new ArrayList<>();

	public String getFoedselsnummer() {
		return (this.historiskeIdenter.isEmpty() || this.historiskeIdenter.get(0) == null) ? null : this.historiskeIdenter.get(0)
				.getIdentifikator();
	}
}
