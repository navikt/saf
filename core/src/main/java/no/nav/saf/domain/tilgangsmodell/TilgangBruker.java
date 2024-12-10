package no.nav.saf.domain.tilgangsmodell;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import no.nav.safselvbetjening.tilgang.Ident;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
				.filter(ident -> IdentType.FOLKEREGISTERIDENT.equals(ident.getIdentType()))
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

	public List<String> getAlleAktoerIds() {
		if (historiskeIdenter.isEmpty() && aktoerId != null) {
			return Collections.singletonList(aktoerId);
		} else if (historiskeIdenter.isEmpty()) {
			return List.of();
		}
		List<String> idents = historiskeIdenter.stream()
				.filter(h -> IdentType.AKTOERID == h.getIdentType())
				.map(TilgangIdent::getIdentifikator)
				.collect(Collectors.toList());
		idents.add(aktoerId);
		return idents;
	}

	public Stream<Ident> getBrukersIdenterSomTilgangsIdenter() {
		return Stream.concat(this.getAlleIdenter().stream(), this.getAlleAktoerIds().stream()).map(Ident::of);
	}
}
