package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivDigitalPostadresse;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivFysiskPostadresse;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivNavNoVarsling;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivUtsendingsInfo;
import no.nav.saf.domain.visningsmodell.Utsendingsinfo;
import no.nav.saf.domain.visningsmodell.VarselMelding;
import no.nav.saf.exceptions.SafTechnicalException;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;
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
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoMapper.NORGE_LANDKODE;
import static org.apache.commons.lang3.StringUtils.isBlank;


public class ArkivUtsendingsInfoMapper {

	private final static JsonMapper mapper = new JsonMapper();
	private static final Pattern EPOST_VARSLINGSTEKST_PATTERN = Pattern.compile("Tittel\\s(?<epostTittel>.*),\\sTekst\\s(?<epostVarslingstekst>.*)", Pattern.DOTALL);

	public static Optional<Utsendingsinfo> mapArkivUtsendingsInfo(ArkivUtsendingsInfo arkivUtsendingsInfo, UtsendingsKanalCode utsendingsKanalCode) {
		if (isNull(arkivUtsendingsInfo)) {
			return empty();
		}

		switch (utsendingsKanalCode) {
			case NAV_NO -> {
				return mapNavNoVarsel(arkivUtsendingsInfo);
			}
			case S -> {
				return mapFysiskPostadresse(arkivUtsendingsInfo.fysiskPostadresse());
			}
			case SDP -> {
				return mapDigitalPostadresse(arkivUtsendingsInfo);
			}
			default -> {
				return empty();
			}
		}
	}

	private static Optional<Utsendingsinfo> mapFysiskPostadresse(ArkivFysiskPostadresse arkivFysiskPostadresse) {
		if (isNull(arkivFysiskPostadresse)) {
			return empty();
		}

		String adressetekstKonvolutt = buildAdressetekstKonvolutt(arkivFysiskPostadresse);
		if (isBlank(adressetekstKonvolutt)) {
			return empty();
		}
		return Optional.of(Utsendingsinfo.builder()
				.varselSendt(Collections.emptyList())
				.fysiskpostSendt(Utsendingsinfo.FysiskpostSendt.builder()
						.adressetekstKonvolutt(adressetekstKonvolutt)
						.build())
				.build());
	}

	private static Optional<Utsendingsinfo> mapDigitalPostadresse(ArkivUtsendingsInfo arkivUtsendingsInfo) {
		ArkivDigitalPostadresse arkivDigitalPostadresse = arkivUtsendingsInfo.digitalPostadresse();

		if (isNull(arkivDigitalPostadresse) || isBlank(arkivDigitalPostadresse.adresse())) {
			return empty();
		}
		return Optional.of(Utsendingsinfo.builder()
				.digitalpostSendt(Utsendingsinfo.DigitalpostSendt.builder()
						.adresse(arkivDigitalPostadresse.adresse())
						.build())
				.varselSendt(mapVarselSendt(arkivUtsendingsInfo).toList())
				.build());
	}

	private static Optional<Utsendingsinfo> mapNavNoVarsel(ArkivUtsendingsInfo arkivUtsendingsInfo) {
		ArkivNavNoVarsling arkivNavNoVarsling = arkivUtsendingsInfo.navNoVarsling();
		Optional<Utsendingsinfo.EpostVarselSendt> epostVarselSendt;
		Optional<Utsendingsinfo.SmsVarselSendt> smsVarselSendt;

		if (!isArkivNavNoVarslingEmpty(arkivNavNoVarsling)) {
			VarselMelding varselInfo = getVarselKontaktInfo(arkivNavNoVarsling);
			VarselMelding varseltekst = getVarseltekst(arkivNavNoVarsling);

			epostVarselSendt = mapEpostVarselSendtOld(varselInfo, varseltekst).filter(not(ArkivUtsendingsInfoMapper::isEpostVarselNull));
			smsVarselSendt = mapSmsVarselSendtOld(varselInfo, varseltekst).filter(not(ArkivUtsendingsInfoMapper::isSmsVarselNull));
		} else {
			epostVarselSendt = empty();
			smsVarselSendt = empty();
		}

		// map gamle og nye data til nye felter. Sorter så nyligste varsel er først i lista
		List<Utsendingsinfo.VarselSendt> varselSendtListe = Stream.concat(
						mapVarselSendtOldToNew(epostVarselSendt, smsVarselSendt),
						mapVarselSendt(arkivUtsendingsInfo)
				).sorted(ArkivUtsendingsInfoMapper::sorterVarselSendtNullsLast)
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

	private static Stream<Utsendingsinfo.VarselSendt> mapVarselSendt(ArkivUtsendingsInfo arkivUtsendingsInfo) {
		return Stream.concat(
				arkivUtsendingsInfo.epostVarsel() == null ? Stream.empty() :
						arkivUtsendingsInfo.epostVarsel().stream()
								.map(epostVarsel -> Utsendingsinfo.VarselSendt.epost()
										.tittel(epostVarsel.tittel())
										.adresse(epostVarsel.epostadresse())
										.varslingstekst(epostVarsel.tekst())
										.varslingstidspunkt(epostVarsel.varslingstidspunkt())
										.build()),
				arkivUtsendingsInfo.smsVarsel() == null ? Stream.empty() :
						arkivUtsendingsInfo.smsVarsel().stream()
								.map(smsVarsel -> Utsendingsinfo.VarselSendt.sms()
										.adresse(smsVarsel.mobilnummer())
										.varslingstekst(smsVarsel.tekst())
										.varslingstidspunkt(smsVarsel.varslingstidspunkt())
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

	private static VarselMelding getVarselKontaktInfo(ArkivNavNoVarsling arkivNavNoVarsling) {
		return jsonStringToObject(arkivNavNoVarsling.varselSendtTil(), VarselMelding.class);
	}

	private static VarselMelding getVarseltekst(ArkivNavNoVarsling arkivNavNoVarsling) {
		return jsonStringToObject(arkivNavNoVarsling.varseltekst(), VarselMelding.class);
	}

	public static <T> T jsonStringToObject(String jsonString, Class<T> tClass) {
		try {
			return mapper.readValue(jsonString, tClass);
		} catch (Exception e) {
			throw new SafTechnicalException(e.getMessage(), e);
		}
	}

	private static String buildAdressetekstKonvolutt(ArkivFysiskPostadresse arkivFysiskPostadresse) {
		String postnummer = isBlank(arkivFysiskPostadresse.postnummer()) ? "" : arkivFysiskPostadresse.postnummer();
		String poststed = isBlank(arkivFysiskPostadresse.poststed()) ? "" : arkivFysiskPostadresse.poststed();

		return Stream.of(arkivFysiskPostadresse.adresselinje1(),
						arkivFysiskPostadresse.adresselinje2(),
						arkivFysiskPostadresse.adresselinje3(),
						postnummer + " " + poststed,
						NORGE_LANDKODE.equals(arkivFysiskPostadresse.landkode()) ? "" : arkivFysiskPostadresse.landkode())
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.joining("\n")).strip();
	}

	private static boolean isEpostVarselNull(Utsendingsinfo.EpostVarselSendt epostVarselSendt) {
		return isNull(epostVarselSendt) || (isBlank(epostVarselSendt.getAdresse()) && isBlank(epostVarselSendt.getVarslingstekst()));
	}

	private static boolean isSmsVarselNull(Utsendingsinfo.SmsVarselSendt smsVarselSendt) {
		return isNull(smsVarselSendt) || (isBlank(smsVarselSendt.getAdresse()) && isBlank(smsVarselSendt.getVarslingstekst()));
	}

	private static boolean isArkivNavNoVarslingEmpty(ArkivNavNoVarsling arkivNavNoVarsling) {
		return isNull(arkivNavNoVarsling) || (isBlank(arkivNavNoVarsling.varselSendtTil()) && isBlank(arkivNavNoVarsling.varseltekst()));
	}
}
