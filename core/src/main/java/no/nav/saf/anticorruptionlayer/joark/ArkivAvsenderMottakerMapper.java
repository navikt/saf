package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivAvsenderMottaker;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivBruker;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.domain.visningsmodell.AvsenderMottaker;
import no.nav.saf.domain.visningsmodell.AvsenderMottakerIdType;

import java.util.regex.Pattern;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.AvsenderMottakerIdTypeCode.valueOf;

public class ArkivAvsenderMottakerMapper {
	private static final Pattern FNR_SIMPLE_REGEX = Pattern.compile("[0-7]\\d{10}");

	static AvsenderMottaker mapArkivAvsenderMottaker(ArkivJournalpost arkivJournalpost) {
		ArkivAvsenderMottaker arkivAvsenderMottaker = arkivJournalpost.avsenderMottaker();
		if (arkivAvsenderMottaker == null) {
			// Bug som feature. Klienter gjør ikke null sjekk på denne og får NPE pga i AvsenderMottakerMapper så er den aldri null
			return AvsenderMottaker.builder().build();
		}
		return AvsenderMottaker.builder()
				.id(arkivAvsenderMottaker.id())
				.type(mapAvsenderMottakerIdType(arkivAvsenderMottaker.id(), arkivAvsenderMottaker.type()))
				.navn(arkivAvsenderMottaker.navn())
				.land(arkivAvsenderMottaker.land())
				.erLikBruker(mapErLikBruker(arkivAvsenderMottaker.id(), arkivJournalpost.bruker()))
				.build();
	}

	private static AvsenderMottakerIdType mapAvsenderMottakerIdType(String id, String type) {
		if (type != null) {
			try {
				return switch (valueOf(type)) {
					case FNR:
						yield AvsenderMottakerIdType.FNR;
					case ORGNR:
						yield AvsenderMottakerIdType.ORGNR;
					case HPRNR:
						yield AvsenderMottakerIdType.HPRNR;
					case UTL_ORG:
						yield AvsenderMottakerIdType.UTL_ORG;
				};
			} catch (IllegalArgumentException e) {
				return AvsenderMottakerIdType.UKJENT;
			}
		} else {
			if (id == null) {
				return AvsenderMottakerIdType.NULL;
			} else {
				switch (id.length()) {
					case 11:
						if (FNR_SIMPLE_REGEX.matcher(id).matches()) {
							return AvsenderMottakerIdType.FNR;
						} else {
							return AvsenderMottakerIdType.UKJENT;
						}
					case 9:
						return AvsenderMottakerIdType.ORGNR;
					default:
						return AvsenderMottakerIdType.UKJENT;
				}
			}
		}
	}

	private static boolean mapErLikBruker(String avsenderMottakerId, ArkivBruker arkivBruker) {
		if (avsenderMottakerId == null || arkivBruker == null) {
			return false;
		}
		return avsenderMottakerId.equals(arkivBruker.id());
	}
}
