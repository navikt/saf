package no.nav.saf.anticorruptionlayer.joark.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.UtsendingsInfoDto;
import no.nav.saf.domain.visningsmodell.Utsendingsinfo;
import no.nav.saf.domain.visningsmodell.VarselMelding;
import no.nav.saf.exceptions.SafTechnicalException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.isBlank;


public class UtsendingsInfoMapper {

	private final static ObjectMapper mapper = new ObjectMapper();
	private static final Pattern EPOST_VARSLINGSTEKST_PATTERN = Pattern.compile("Tittel\\s(?<epostTittel>.*),\\sTekst\\s(?<epostVarslingstekst>.*)", Pattern.DOTALL);

	public static Optional<Utsendingsinfo> mapUtsendingsInfo(UtsendingsInfoDto utsendingsInfoDto, UtsendingsKanalCode utsendingsKanalCode) {
		if (isNull(utsendingsInfoDto)) {
			return empty();
		}

		switch (utsendingsKanalCode) {
			case NAV_NO -> {
				return mapNavNoVarsel(utsendingsInfoDto);
			}
			case S -> {
				return mapFysiskPostadresse(utsendingsInfoDto.getFysiskPostadresse());
			}
			case SDP -> {
				return mapDigitalPostadresse(utsendingsInfoDto);
			}
			default -> {
				return empty();
			}
		}
	}

	private static Optional<Utsendingsinfo> mapFysiskPostadresse(UtsendingsInfoDto.FysiskPostadresseDto fysiskPostadresse) {
		if (isNull(fysiskPostadresse)) {
			return empty();
		}

		String adressetekstKonvolutt = buildAdressetekstKonvolutt(fysiskPostadresse);
		if (isBlank(adressetekstKonvolutt)) {
			return empty();
		}
		return Optional.of(Utsendingsinfo.builder()
				.fysiskpostSendt(Utsendingsinfo.FysiskpostSendt.builder()
						.adressetekstKonvolutt(adressetekstKonvolutt)
						.build())
				.build());
	}

	private static Optional<Utsendingsinfo> mapDigitalPostadresse(UtsendingsInfoDto utsendingsInfoDto) {
		UtsendingsInfoDto.DigitalPostadresseDto digitalPostadresseDto = utsendingsInfoDto.getDigitalPostadresse();

		if (isNull(digitalPostadresseDto) || isBlank(digitalPostadresseDto.getDigitalpostkasseAdresse())) {
			return empty();
		}
		return Optional.of(Utsendingsinfo.builder()
				.digitalpostSendt(Utsendingsinfo.DigitalpostSendt.builder()
						.adresse(digitalPostadresseDto.getDigitalpostkasseAdresse())
						.build())
				.varselSendt(mapVarselSendt(utsendingsInfoDto).toList())
				.build());
	}

	private static Optional<Utsendingsinfo> mapNavNoVarsel(UtsendingsInfoDto utsendingsInfoDto) {
		UtsendingsInfoDto.NavNoVarslingDto navNoVarslingDto = utsendingsInfoDto.getNavNoVarsling();
		Optional<Utsendingsinfo.EpostVarselSendt> epostVarselSendt;
		Optional<Utsendingsinfo.SmsVarselSendt> smsVarselSendt;

		if (!isVarselDtoNull(navNoVarslingDto)) {
			VarselMelding varselInfo = getVarselKontaktInfo(navNoVarslingDto);
			VarselMelding varseltekst = getVarseltekst(navNoVarslingDto);

			epostVarselSendt = mapEpostVarselSendtOld(varselInfo, varseltekst).filter(not(UtsendingsInfoMapper::isEpostVarselNull));
			smsVarselSendt = mapSmsVarselSendtOld(varselInfo, varseltekst).filter(not(UtsendingsInfoMapper::isSmsVarselNull));
		} else {
			epostVarselSendt = empty();
			smsVarselSendt = empty();
		}

		// map gamle og nye data til nye felter. Sorter så nyligste varsel er først i lista
		List<Utsendingsinfo.VarselSendt> varselSendtListe = Stream.concat(
						mapVarselSendtOldToNew(epostVarselSendt, smsVarselSendt),
						mapVarselSendt(utsendingsInfoDto)
				).sorted(UtsendingsInfoMapper::sorterVarselSendtNullsLast)
				.toList();

		Utsendingsinfo.UtsendingsinfoBuilder varselSendtUtsendingsinfoBuilder = Utsendingsinfo.builder();

		// map nye data til gamle felter om de ikke er matet med gamle data allerede
		epostVarselSendt
				.or(mapFromNewVarselSendtToOld(varselSendtListe, "EPOST",
						epostVarsel -> Utsendingsinfo.EpostVarselSendt.builder()
								.tittel(epostVarsel.getTittel())
								.varslingstekst(epostVarsel.getVarslingstekst())
								.adresse(epostVarsel.getAdresse())
								.build()))
				.ifPresent(varselSendtUtsendingsinfoBuilder::epostVarselSendt);

		smsVarselSendt
				.or(mapFromNewVarselSendtToOld(varselSendtListe, "SMS",
						smsVarsel -> Utsendingsinfo.SmsVarselSendt.builder()
								.varslingstekst(smsVarsel.getVarslingstekst())
								.adresse(smsVarsel.getAdresse())
								.build()))
				.ifPresent(varselSendtUtsendingsinfoBuilder::smsVarselSendt);

		Utsendingsinfo varselSendtUtsendingsinfo = varselSendtUtsendingsinfoBuilder.varselSendt(varselSendtListe).build();
		if (varselSendtUtsendingsinfo.getEpostVarselSendt() == null && varselSendtUtsendingsinfo.getSmsVarselSendt() == null) {
			return empty();
		}
		return Optional.of(varselSendtUtsendingsinfo);
	}

	private static int sorterVarselSendtNullsLast(Utsendingsinfo.VarselSendt v1, Utsendingsinfo.VarselSendt v2) {
		if (v1.getVarslingstidspunkt() == null) {
			return 1;
		} else if (v2.getVarslingstidspunkt() == null) {
			return -1;
		}
		return v2.getVarslingstidspunkt().compareTo(v1.getVarslingstidspunkt());
	}

	private static <T> Supplier<Optional<T>> mapFromNewVarselSendtToOld(List<Utsendingsinfo.VarselSendt> varselSendtCompound, String varselType, Function<Utsendingsinfo.VarselSendt, T> epostMapper) {
		return () -> varselSendtCompound.stream()
				.filter(v -> v.getType().equalsIgnoreCase(varselType))
				.findFirst()
				.map(epostMapper);
	}

	private static Stream<Utsendingsinfo.VarselSendt> mapVarselSendtOldToNew(Optional<Utsendingsinfo.EpostVarselSendt> epostVarselSendt, Optional<Utsendingsinfo.SmsVarselSendt> smsVarselSendt) {
		return Stream.concat(
				epostVarselSendt.stream()
						.map(epostVarsel -> Utsendingsinfo.VarselSendt.epost()
								.tittel(epostVarsel.getTittel())
								.adresse(epostVarsel.getAdresse())
								.varslingstekst(epostVarsel.getVarslingstekst())
								.build()),
				smsVarselSendt.stream()
						.map(smsVarsel -> Utsendingsinfo.VarselSendt.sms()
								.adresse(smsVarsel.getAdresse())
								.varslingstekst(smsVarsel.getVarslingstekst())
								.build()));
	}

	private static Stream<Utsendingsinfo.VarselSendt> mapVarselSendt(UtsendingsInfoDto utsendingsInfoDto) {
		return Stream.concat(
				utsendingsInfoDto.getEpostVarsel() == null ? Stream.empty() :
						utsendingsInfoDto.getEpostVarsel().stream()
								.map(epostVarsel -> Utsendingsinfo.VarselSendt.epost()
										.tittel(epostVarsel.getTittel())
										.adresse(epostVarsel.getEpostadresse())
										.varslingstekst(epostVarsel.getTekst())
										.varslingstidspunkt(epostVarsel.getVarslingstidspunkt())
										.build()),
				utsendingsInfoDto.getSmsVarsel() == null ? Stream.empty() :
						utsendingsInfoDto.getSmsVarsel().stream()
								.map(smsVarsel -> Utsendingsinfo.VarselSendt.sms()
										.adresse(smsVarsel.getMobilnummer())
										.varslingstekst(smsVarsel.getTekst())
										.varslingstidspunkt(smsVarsel.getVarslingstidspunkt())
										.build())
		);
	}

	private static Optional<Utsendingsinfo.SmsVarselSendt> mapSmsVarselSendtOld(VarselMelding varselInfo, VarselMelding varseltekst) {
		return isBlank(varselInfo.getSms()) || isBlank(varseltekst.getSms()) ? empty() : Optional.of(Utsendingsinfo.SmsVarselSendt.builder()
				.adresse(varselInfo.getSms())
				.varslingstekst(varseltekst.getSms())
				.build());
	}

	private static Optional<Utsendingsinfo.EpostVarselSendt> mapEpostVarselSendtOld(VarselMelding varselInfo, VarselMelding varseltekst) {
		if (isBlank(varselInfo.getEpost()) || isBlank(varseltekst.getEpost())) {
			return empty();
		}
		Matcher matcher = EPOST_VARSLINGSTEKST_PATTERN.matcher(varseltekst.getEpost());
		if (matcher.find()) {
			String epostTittel = matcher.group("epostTittel");
			String epostVarslingstekst = matcher.group("epostVarslingstekst");
			if (isBlank(epostTittel) || isBlank(epostVarslingstekst)) {
				return empty();
			}
			return Optional.of(Utsendingsinfo.EpostVarselSendt.builder()
					.tittel(epostTittel)
					.adresse(varselInfo.getEpost())
					.varslingstekst(epostVarslingstekst)
					.build());
		}
		return empty();
	}

	private static VarselMelding getVarselKontaktInfo(UtsendingsInfoDto.NavNoVarslingDto varslingDto) {
		return jsonStringToObject(varslingDto.getVarselSendtTil(), VarselMelding.class);
	}

	private static VarselMelding getVarseltekst(UtsendingsInfoDto.NavNoVarslingDto varslingDto) {
		return jsonStringToObject(varslingDto.getVarseltekst(), VarselMelding.class);
	}

	public static <T> T jsonStringToObject(String jsonString, Class<T> tClass) {
		try {
			return mapper.readValue(jsonString, tClass);
		} catch (IOException e) {
			throw new SafTechnicalException(e.getMessage(), e);
		}
	}

	private static String buildAdressetekstKonvolutt(UtsendingsInfoDto.FysiskPostadresseDto fysiskPostadresse) {
		String postnummer = isBlank(fysiskPostadresse.getPostnummer()) ? "" : fysiskPostadresse.getPostnummer();
		String poststed = isBlank(fysiskPostadresse.getPoststed()) ? "" : fysiskPostadresse.getPoststed();

		return Stream.of(fysiskPostadresse.getAdresselinje1(),
						fysiskPostadresse.getAdresselinje2(),
						fysiskPostadresse.getAdresselinje3(),
						postnummer + " " + poststed,
						fysiskPostadresse.getLandkode())
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.joining("\n")).strip();
	}

	private static boolean isEpostVarselNull(Utsendingsinfo.EpostVarselSendt epostVarselSendt) {
		return isNull(epostVarselSendt) || (isBlank(epostVarselSendt.getAdresse()) && isBlank(epostVarselSendt.getVarslingstekst()));
	}

	private static boolean isSmsVarselNull(Utsendingsinfo.SmsVarselSendt smsVarselSendt) {
		return isNull(smsVarselSendt) || (isBlank(smsVarselSendt.getAdresse()) && isBlank(smsVarselSendt.getVarslingstekst()));
	}

	private static boolean isVarselDtoNull(UtsendingsInfoDto.NavNoVarslingDto varslingDto) {
		return isNull(varslingDto) || (isBlank(varslingDto.getVarselSendtTil()) && isBlank(varslingDto.getVarseltekst()));
	}
}
