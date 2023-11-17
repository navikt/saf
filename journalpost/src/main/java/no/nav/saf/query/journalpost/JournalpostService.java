package no.nav.saf.query.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.PdlAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.fpsak.FpsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivBruker;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivFildetaljer;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSak;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSaksrelasjon;
import no.nav.saf.anticorruptionlayer.k9.K9AntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.kode.Skjerming;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static no.nav.saf.domain.DomainConstants.ORGANISASJON;
import static no.nav.saf.domain.DomainConstants.PERSON;
import static no.nav.saf.domain.kode.Arkivsakssystem.GSAK;
import static no.nav.saf.domain.kode.Arkivsakssystem.PSAK;
import static no.nav.saf.domain.kode.Tema.PEN;
import static no.nav.saf.domain.kode.Tema.UFO;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
@Component
public class JournalpostService {
	private final JournalpostAntiCorruptionLayer journalpostAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;
	private final FpsakAntiCorruptionLayer fpsakAntiCorruptionLayer;
	private final K9AntiCorruptionLayer k9AntiCorruptionLayer;
	private final PdlAntiCorruptionLayer pdlAntiCorruptionLayer;

	public JournalpostService(PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
							  JournalpostAntiCorruptionLayer journalpostAntiCorruptionLayer,
							  BisysAntiCorruptionLayer bisysAntiCorruptionLayer,
							  FpsakAntiCorruptionLayer fpsakAntiCorruptionLayer,
							  K9AntiCorruptionLayer k9AntiCorruptionLayer,
							  PdlAntiCorruptionLayer pdlAntiCorruptionLayer) {
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.journalpostAntiCorruptionLayer = journalpostAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
		this.fpsakAntiCorruptionLayer = fpsakAntiCorruptionLayer;
		this.k9AntiCorruptionLayer = k9AntiCorruptionLayer;
		this.pdlAntiCorruptionLayer = pdlAntiCorruptionLayer;
	}

	JournalpostHolder hentJournalpost(String journalpostId, String eksternReferanseId, SafRequestContext safRequestContext) {
		ArkivJournalpost arkivJournalpost = journalpostAntiCorruptionLayer.hentJournalpost(journalpostId, eksternReferanseId);
		TilgangBruker tilgangBruker = mapTilgangBruker(arkivJournalpost);
		TilgangSak tilgangSak = mapTilgangSak(tilgangBruker, arkivJournalpost, safRequestContext);
		TilgangJournalpost tilgangJournalpost = mapTilgangJournalpost(arkivJournalpost);

		return new JournalpostHolder(arkivJournalpost, new JournalpostTilgang(tilgangBruker, tilgangSak, tilgangJournalpost));
	}

	private TilgangBruker mapTilgangBruker(ArkivJournalpost arkivJournalpost) {
		if (arkivJournalpost.isTilknyttetSak()) {
			return mapTilgangBrukerTilknyttetSak(arkivJournalpost);
		} else {
			return mapTilgangBrukerUtenTilknyttetSak(arkivJournalpost);
		}
	}

	private TilgangBruker mapTilgangBrukerTilknyttetSak(ArkivJournalpost arkivJournalpost) {
		ArkivSaksrelasjon arkivSaksrelasjon = arkivJournalpost.saksrelasjon();
		if (arkivSaksrelasjon.isPensjonsak()) {
			String fnr = pensjonSakAntiCorruptionLayer.findFoedselsnummerBySakId(valueOf(arkivSaksrelasjon.sakId()));
			if (fnr == null) {
				return mapTilgangBrukerUtenTilknyttetSak(arkivJournalpost);
			} else {
				return pdlAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(fnr);
			}
		} else {
			ArkivSak arkivSak = arkivSaksrelasjon.sak();
			return TilgangBruker.builder()
					.aktoerId(arkivSak.aktoerId())
					.orgnummer(arkivSak.aktoerId() == null ? trim(arkivSak.orgNr()) : null)
					.build();
		}
	}

	private static TilgangBruker mapTilgangBrukerUtenTilknyttetSak(ArkivJournalpost arkivJournalpost) {
		ArkivBruker bruker = arkivJournalpost.bruker();
		if (bruker == null || bruker.type() == null) {
			return null;
		}
		return switch (bruker.type()) {
			case PERSON -> TilgangBruker.builder()
					.foedselsnr(bruker.id())
					.build();
			case ORGANISASJON -> TilgangBruker.builder()
					.orgnummer(trim(bruker.id()))
					.build();
			default -> {
				log.error("Forventet bruker.type=(PERSON, ORGANISASJON) for journalpost uten sakstilknytning med journalpostId={}. Fikk bruker.type={}", arkivJournalpost.journalpostId(), bruker.type());
				yield null;
			}
		};
	}

	private TilgangSak mapTilgangSak(TilgangBruker tilgangBruker, ArkivJournalpost arkivJournalpost, SafRequestContext safRequestContext) {
		if (arkivJournalpost.isTilknyttetSak()) {
			return tilgangSakMedSakstilknytning(tilgangBruker, arkivJournalpost, safRequestContext);
		} else {
			return mapTilgangSakUtenSakstilknytning(arkivJournalpost);
		}
	}

	private TilgangSak tilgangSakMedSakstilknytning(TilgangBruker tilgangBruker, ArkivJournalpost arkivJournalpost, SafRequestContext safRequestContext) {
		ArkivSaksrelasjon arkivSaksrelasjon = arkivJournalpost.saksrelasjon();
		if (arkivSaksrelasjon.isPensjonsak()) {
			return mapTilgangPensjonSak(tilgangBruker, arkivJournalpost, safRequestContext);
		} else {
			Arkivsak arkivsak = mapArkivsak(arkivJournalpost);
			safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
			return mapTilgangGsak(arkivsak);
		}
	}

	private TilgangSak mapTilgangGsak(Arkivsak arkivsak) {
		BidragSak bidragSak = bisysAntiCorruptionLayer.hentBidragSakByArkivsak(arkivsak);
		List<String> fpsak = fpsakAntiCorruptionLayer.hentRelevanteParter(arkivsak);
		List<String> k9sak = k9AntiCorruptionLayer.hentRelevanteParter(arkivsak);
		return TilgangSak.builder()
				.aktoerId(arkivsak.getAktoerId())
				.arkivsaksnummer(arkivsak.getArkivsaksnummer())
				.arkivsaksystem(GSAK)
				.tema(arkivsak.getTema())
				.orgnummer(trim(arkivsak.getOrgnummer()))
				.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
				.fagsaksystem(arkivsak.getFagsaksystem())
				.fpAktoerIdList(fpsak)
				.k9AktoerIdList(k9sak)
				.build();
	}

	private TilgangSak mapTilgangPensjonSak(TilgangBruker tilgangBruker, ArkivJournalpost arkivJournalpost, SafRequestContext safRequestContext) {
		ArkivSaksrelasjon arkivSaksrelasjon = arkivJournalpost.saksrelasjon();
		List<Arkivsak> arkivsaker = pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker, Arrays.asList(PEN, UFO));
		return arkivsaker.stream().filter(p -> p.getArkivsaksnummer().equals(valueOf(arkivSaksrelasjon.sakId())))
				.peek(pensjonArkivsak -> safRequestContext.getRequestCache().putObject(pensjonArkivsak.getKey(), pensjonArkivsak))
				.map(psakArkivsak -> TilgangSak.builder()
						.aktoerId(psakArkivsak.getAktoerId())
						.arkivsaksnummer(psakArkivsak.getArkivsaksnummer())
						.arkivsaksystem(PSAK)
						.tema(psakArkivsak.getTema())
						.orgnummer(trim(psakArkivsak.getOrgnummer()))
						.relevanteTredjeparter(new ArrayList<>())
						.fagsaksystem(psakArkivsak.getFagsaksystem())
						.build()).findFirst()
				.orElseGet(() -> mapTilgangSakUtenSakstilknytning(arkivJournalpost));
	}

	private static TilgangSak mapTilgangSakUtenSakstilknytning(ArkivJournalpost arkivJournalpost) {
		return TilgangSak.builder()
				.tema(Tema.valueOf(arkivJournalpost.fagomraade()))
				.build();
	}

	private Arkivsak mapArkivsak(ArkivJournalpost arkivJournalpost) {
		ArkivSaksrelasjon arkivSaksrelasjon = arkivJournalpost.saksrelasjon();
		ArkivSak arkivSak = arkivSaksrelasjon.sak();
		return Arkivsak.builder()
				.arkivsaksnummer(valueOf(arkivSaksrelasjon.sakId()))
				.arkivsaksystem(arkivSaksrelasjon.isPensjonsak() ? PSAK : GSAK)
				.fagsakId(arkivSak.fagsakNr())
				.fagsaksystem(arkivSak.applikasjon())
				.orgnummer(trim(arkivSak.orgNr()))
				.aktoerId(arkivSak.aktoerId())
				.tema(Arkivsak.mapTema(arkivSak.tema()))
				.build();
	}

	private TilgangJournalpost mapTilgangJournalpost(ArkivJournalpost arkivJournalpost) {
		Long journalpostId = arkivJournalpost.journalpostId();
		return TilgangJournalpost.builder()
				.journalpostId(valueOf(journalpostId))
				.journalstatus(JournalStatusCode.valueOf(arkivJournalpost.status()).toSafJournalstatus())
				.skjerming(mapSkjerming(arkivJournalpost.skjerming()))
				.dokumenter(mapTilgangDokumentInfo(arkivJournalpost))
				.build();
	}

	private static List<TilgangDokumentInfo> mapTilgangDokumentInfo(ArkivJournalpost arkivJournalpost) {
		Long journalpostId = arkivJournalpost.journalpostId();
		return arkivJournalpost.dokumenter().stream()
				.map(arkivDokumentinfo -> {
					Long dokumentInfoId = arkivDokumentinfo.dokumentInfoId();
					return TilgangDokumentInfo.builder()
							.skjerming(mapSkjerming(arkivDokumentinfo.skjerming()))
							.tilgangDokumentvarianter(mapTilgangDokumentvarianter(journalpostId, dokumentInfoId, arkivDokumentinfo.fildetaljer()))
							.journalpostId(valueOf(journalpostId))
							.dokumentInfoId(valueOf(dokumentInfoId))
							.build();
				}).collect(Collectors.toList());
	}

	private static List<TilgangDokumentvariant> mapTilgangDokumentvarianter(Long journalpostId, Long dokumentInfoId, List<ArkivFildetaljer> fildetaljer) {
		return fildetaljer.stream()
				.map(arkivFildetaljer -> TilgangDokumentvariant.builder()
						.skjerming(mapSkjerming(arkivFildetaljer.skjerming()))
						.variantformat(VariantFormatCode.valueOf(arkivFildetaljer.format()).getSafVariantformat())
						.journalpostId(valueOf(journalpostId))
						.dokumentInfoId(valueOf(dokumentInfoId))
						.build()).collect(Collectors.toList());
	}

	private static Skjerming mapSkjerming(String skjerming) {
		try {
			return skjerming == null ? null : SkjermingTypeCode.valueOf(skjerming).getSafSkjerming();
		} catch (IllegalArgumentException e) {
			// I tilfelle det introduseres en ny kodeverdi her uten at denne appen er i synk
			return Skjerming.FEIL;
		}
	}
}
