package no.nav.saf.domain.tilgangsmodell;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder(toBuilder = true)
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
				.filter(ident -> ident.getIdentType().equals(IdentType.FOLKEREGISTERIDENT))
				.map(TilgangIdent::getIdentifikator)
				.collect(Collectors.toList());
		if (foedselsnr != null) {
			tmpAlleIdenter.add(foedselsnr);
		}
		if (orgnummer != null) {
			tmpAlleIdenter.add(orgnummer);
		}
		return tmpAlleIdenter;
	}

	public boolean isPerson() {
		return isNotBlank(foedselsnr) || isNotBlank(aktoerId);
	}

	public boolean isOrganisasjon() {
		return !isPerson() && isNotBlank(orgnummer);
	}

	public boolean isUkjent() {
		return !isPerson() && !isOrganisasjon();
	}

	public List<String> hentAlleAktoerId() {
		if (historiskeIdenter.isEmpty() && aktoerId != null) {
			return Collections.singletonList(aktoerId);
		} else if (historiskeIdenter.isEmpty()) {
			return new ArrayList<>();
		}
		List<String> idents = historiskeIdenter.stream().filter(h -> h.getIdentType().equals(IdentType.AKTOERID)).map(TilgangIdent::getIdentifikator).collect(Collectors.toList());
		idents.add(aktoerId);
		return idents;
	}
}
