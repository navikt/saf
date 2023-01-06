package no.nav.saf.anticorruptionlayer.joark.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.UtsendingsInfoDto;
import no.nav.saf.domain.visningsmodell.Utsendingsinfo;
import no.nav.saf.domain.visningsmodell.VarselMelding;
import no.nav.saf.exceptions.SafTechnicalException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;


public class UtsendingsInfoMapper {

	public Optional<Utsendingsinfo> mapUtsendingsInfo(UtsendingsInfoDto utsendingsInfoDto, UtsendingsKanalCode utsendingsKanalCode) {
		if (isNull(utsendingsInfoDto)) {
			return Optional.empty();
		}

		switch (utsendingsKanalCode) {
			case NAV_NO -> {
				return isVarselDtoNull(utsendingsInfoDto.getNavNoVarsling()) ? Optional.empty() : mapVarselSendt(utsendingsInfoDto.getNavNoVarsling());
			}
			case S -> {
				return mapFysiskPostadresse(utsendingsInfoDto.getFysiskPostadresse());
			}
			case SDP -> {
				return isNull(utsendingsInfoDto.getDigitalPostadresse()) ? Optional.empty() : Optional.of(Utsendingsinfo.builder()
						.digitalpostSendt(mapDigitalPostadresse(utsendingsInfoDto.getDigitalPostadresse()))
						.build());
			}
			default -> {
				return Optional.empty();
			}
		}
	}

	private Optional<Utsendingsinfo> mapFysiskPostadresse(UtsendingsInfoDto.FysiskPostadresseDto fysiskPostadresse) {
		if (isNull(fysiskPostadresse)) {
			return Optional.empty();
		}

		String adressetekstKonvolutt = isBlank(buildAdressetekstKonvolutt(fysiskPostadresse)) ? null : buildAdressetekstKonvolutt(fysiskPostadresse);

		return isBlank(adressetekstKonvolutt) ? Optional.empty() :
				Optional.of(Utsendingsinfo.builder()
						.fysiskpostSendt(Utsendingsinfo.FysiskpostSendt.builder()
								.adressetekstKonvolutt(adressetekstKonvolutt)
								.build()).build());
	}

	private Utsendingsinfo.DigitalpostSendt mapDigitalPostadresse(UtsendingsInfoDto.DigitalPostadresseDto digitalPostadresseDto) {
		return isNull(digitalPostadresseDto) || isBlank(digitalPostadresseDto.getDigitalpostkasseAdresse()) ? null : Utsendingsinfo.DigitalpostSendt.builder()
				.adresse(digitalPostadresseDto.getDigitalpostkasseAdresse())
				.build();
	}

	private Optional<Utsendingsinfo> mapVarselSendt(UtsendingsInfoDto.NavNoVarslingDto navNoVarslingDto) {
		VarselMelding varselInfo = getVarselKontaktInfo(navNoVarslingDto);
		VarselMelding varseltekst = getVarseltekst(navNoVarslingDto);
		if (isNull(varselInfo) || isNull(varseltekst)) {
			return Optional.empty();
		}

		Utsendingsinfo.EpostVarselSendt epostVarselSendt = mapEpostVarselSendt(varselInfo, varseltekst);
		Utsendingsinfo.SmsVarselSendt smsVarselSendt = mapSmsVarselSendt(varselInfo, varseltekst);

		if (isSmsVarselNull(smsVarselSendt) && isEpostVarselNull(epostVarselSendt)) {
			return Optional.empty();
		}

		Utsendingsinfo varselSendtUtsendingsinfo = Utsendingsinfo.builder()
				.epostVarselSendt(epostVarselSendt)
				.smsVarselSendt(smsVarselSendt)
				.build();
		return isVarselSendtNull(varselSendtUtsendingsinfo) ? Optional.empty() : Optional.of(varselSendtUtsendingsinfo);
	}

	private Utsendingsinfo.SmsVarselSendt mapSmsVarselSendt(VarselMelding varselInfo, VarselMelding varseltekst) {
		return isBlank(varselInfo.getSms()) || isBlank(varseltekst.getSms()) ? null : Utsendingsinfo.SmsVarselSendt.builder()
				.adresse(varselInfo.getSms())
				.varslingstekst(varseltekst.getSms())
				.build();
	}

	private Utsendingsinfo.EpostVarselSendt mapEpostVarselSendt(VarselMelding varselInfo, VarselMelding varseltekst) {
		List<String> varselList = parseString(varseltekst.getEpost());
		return varselList == null ? null :
				Utsendingsinfo.EpostVarselSendt.builder()
						.tittel(varselList.size() > 1 ? varselList.get(0) : null)
						.adresse(varselInfo.getEpost())
						.varslingstekst(varselList.size() > 1 ? varselList.get(1).strip() : varselList.get(0).strip())
						.build();
	}

	private VarselMelding getVarselKontaktInfo(UtsendingsInfoDto.NavNoVarslingDto varslingDto) {
		return jsonStringToObject(varslingDto.getVarselSendtTil(), VarselMelding.class);
	}

	private VarselMelding getVarseltekst(UtsendingsInfoDto.NavNoVarslingDto varslingDto) {
		return jsonStringToObject(varslingDto.getVarseltekst(), VarselMelding.class);
	}

	public static <T> T jsonStringToObject(String jsonString, Class<T> tClass) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(jsonString, tClass);
		} catch (IOException e) {
			throw new SafTechnicalException(e.getMessage(), e);
		}
	}

	private String buildAdressetekstKonvolutt(UtsendingsInfoDto.FysiskPostadresseDto fysiskPostadresse) {
		String postnummer = isBlank(fysiskPostadresse.getPostnummer()) ? "" : fysiskPostadresse.getPostnummer();
		String poststed = isBlank(fysiskPostadresse.getPoststed()) ? "" : fysiskPostadresse.getPoststed();

		String postadresse = Stream.of(fysiskPostadresse.getAdresselinje1(),
						fysiskPostadresse.getAdresselinje2(),
						fysiskPostadresse.getAdresselinje3(),
						postnummer + " " + poststed,
						fysiskPostadresse.getLandkode())
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.joining("\n")).strip();
		return isBlank(postadresse) ? null : postadresse;
	}

	private List<String> parseString(String varseltekst) {
		return isBlank(varseltekst) ? null : Arrays.stream(varseltekst.split(",", 2)).toList();
	}

	private boolean isEpostVarselNull(Utsendingsinfo.EpostVarselSendt epostVarselSendt) {
		return isNull(epostVarselSendt) || (isBlank(epostVarselSendt.getAdresse()) && isBlank(epostVarselSendt.getVarslingstekst()));
	}

	private boolean isSmsVarselNull(Utsendingsinfo.SmsVarselSendt smsVarselSendt) {
		return isNull(smsVarselSendt) || (isBlank(smsVarselSendt.getAdresse()) && isBlank(smsVarselSendt.getVarslingstekst()));
	}

	private boolean isVarselSendtNull(Utsendingsinfo utsendingsinfo) {
		return isNull(utsendingsinfo.getEpostVarselSendt()) && isNull(utsendingsinfo.getSmsVarselSendt());
	}

	private boolean isVarselDtoNull(UtsendingsInfoDto.NavNoVarslingDto varslingDto) {
		return isNull(varslingDto) || (isBlank(varslingDto.getVarselSendtTil()) && isBlank(varslingDto.getVarseltekst()));
	}
}
