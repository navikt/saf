package no.nav.saf.domain;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;

import java.time.LocalDateTime;

import static no.nav.saf.domain.DomainConstants.SAK_STATUS_AAPEN;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Value
@Builder
public class Arkivsak {
	String aktoerId;
	String orgnummer;
	String arkivsaksnummer;
	Arkivsakssystem arkivsaksystem;
	String fagsakId;
	String fagsaksystem;
	Tema tema;
	String sakStatus;
	LocalDateTime datoOpprettet;

	public String getKey() {
		return arkivsaksnummer + arkivsaksystem;
	}

	public boolean isBrukerInfoMissing() {
		return aktoerId == null && orgnummer == null;
	}

	public boolean isBrukerPerson() {
		return isNotBlank(aktoerId);
	}

	public boolean isBrukerOrganisasjon() {
		return isNotBlank(orgnummer);
	}

	public boolean isAvsluttet() {
		return sakStatus != null && !SAK_STATUS_AAPEN.equalsIgnoreCase(sakStatus);
	}

	public static Tema mapTema(String temaString) {
		if (temaString == null) {
			return null;
		}
		try {
			return Tema.valueOf(temaString.trim());
		} catch (Exception e) {
			return null;
		}
	}
}
