package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;

import java.util.List;

@Value
@Builder
public class TilgangSak {
	// Kjerneattributter brukt for tilgangskontroll
	String aktoerId;
	String foedselsnummer;
	String orgnummer;
	@NonNull
	Tema tema;
	String fagsaksystem;

	List<TilgangRelevantTredjepart> relevanteTredjeparter; // Bisys

	List<String> fpAktoerIdList;
	List<String> k9AktoerIdList;

	// Ekstra attributter for å forenkle kodeflyt
	String arkivsaksnummer;
	Arkivsakssystem arkivsaksystem;

	public String getKey() {
		return arkivsaksnummer + arkivsaksystem;
	}
}
