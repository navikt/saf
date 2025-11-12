package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
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
	// Kjerneattributter brukt for tilgangskontroll
	String foedselsnummer;
	String aktoerId;
	String orgnummer;

	@Builder.Default
	List<TilgangIdent> historiskeIdenter = new ArrayList<>();

	public List<String> getAlleIdenter() {
		List<String> tmpAlleIdenter = historiskeIdenter.stream()
				.filter(ident -> IdentType.FOLKEREGISTERIDENT.equals(ident.getIdentType()))
				.map(TilgangIdent::getIdentifikator)
				.collect(Collectors.toList());
		if (foedselsnummer != null) {
			tmpAlleIdenter.add(foedselsnummer);
		}
		if (orgnummer != null) {
			tmpAlleIdenter.add(orgnummer);
		}
		return tmpAlleIdenter;
	}

	public boolean isPerson() {
		return isNotBlank(foedselsnummer) || isNotBlank(aktoerId);
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
