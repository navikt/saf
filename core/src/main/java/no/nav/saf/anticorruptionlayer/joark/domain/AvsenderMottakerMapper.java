package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.AvsenderMottakerIdTypeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.BrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.domain.visningsmodell.AvsenderMottaker;
import no.nav.saf.domain.visningsmodell.AvsenderMottakerIdType;

import java.util.regex.Pattern;

public class AvsenderMottakerMapper {
    private static final Pattern FNR_SIMPLE_REGEX = Pattern.compile("[0-7]\\d{10}");

    AvsenderMottaker map(JournalpostDto journalpostDto) {
        return AvsenderMottaker.builder()
                .id(journalpostDto.getAvsenderMottakerId())
                .type(mapAvsenderMottakerIdType(journalpostDto.getAvsenderMottakerId(), journalpostDto.getAvsenderMottakerIdType()))
                .navn(journalpostDto.getAvsenderMottakerNavn())
                .land(journalpostDto.getAvsenderMottakerLand())
                .erLikBruker(mapErLikBruker(journalpostDto.getAvsenderMottakerId(), journalpostDto.getBruker()))
                .build();
    }

    private AvsenderMottakerIdType mapAvsenderMottakerIdType(String avsenderMottakerId, AvsenderMottakerIdTypeCode avsenderMottakerIdTypeCode) {
        if (avsenderMottakerIdTypeCode != null) {
            switch (avsenderMottakerIdTypeCode) {
                case FNR:
                    return AvsenderMottakerIdType.FNR;
                case ORGNR:
                    return AvsenderMottakerIdType.ORGNR;
                case HPRNR:
                    return AvsenderMottakerIdType.HPRNR;
                case UTL_ORG:
                    return AvsenderMottakerIdType.UTL_ORG;
                default:
                    return AvsenderMottakerIdType.UKJENT;
            }

        } else {
            if (avsenderMottakerId == null) {
                return AvsenderMottakerIdType.NULL;
            } else {
                switch (avsenderMottakerId.length()) {
                    case 11:
                        if(FNR_SIMPLE_REGEX.matcher(avsenderMottakerId).matches()) {
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

    private boolean mapErLikBruker(String avsenderMottakerId, BrukerDto brukerDto) {
        if (avsenderMottakerId == null || brukerDto == null) {
            return false;
        }
        return avsenderMottakerId.equals(brukerDto.getBrukerId());
    }
}
