package no.nav.saf.domain;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Arkivsak {
	private final String aktoerId;
	private final String orgnummer;
	private final String arkivsaksnummer;
	private final Arkivsakssystem arkivsaksystem;
	private final String fagsakId;
	private final String fagsaksystem;
	private final Tema tema;
	private final LocalDateTime datoOpprettet;

	public String getKey() {
		return arkivsaksnummer + arkivsaksystem;
	}

	public boolean isBrukerInfoMissing(){
		return aktoerId == null && orgnummer == null;
	}

	public boolean isBrukerPerson() {
		return isNotBlank(aktoerId);
	}

	public boolean isBrukerOrganisasjon() {
		return isNotBlank(orgnummer);
	}

	public static Tema mapTema(String temaString) {

		Optional<String> tema = Optional.ofNullable(temaString)
				.map(String::trim);

		return tema.filter(t -> FagomradeCode.OKO.name().equals(t))
				.map(t -> Tema.STO)
				.orElseGet(() -> {
					try {
						return tema.map(Tema::valueOf).orElse(Tema.UKJ);
					} catch (Exception e) {
						return null;
					}
				});
	}
}
