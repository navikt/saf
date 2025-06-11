package no.nav.saf.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.PdlAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.fpsak.FpsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivBruker;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivDokumentinfo;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivFildetaljer;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSak;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSaksrelasjon;
import no.nav.saf.anticorruptionlayer.k9.K9AntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pdl.PersonIkkeFunnetException;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static no.nav.saf.anticorruptionlayer.joark.domain.ArkivsakMapper.mapArkivsak;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode.valueOf;
import static no.nav.saf.domain.kode.Arkivsakssystem.GSAK;
import static no.nav.saf.domain.kode.Arkivsakssystem.PSAK;
import static no.nav.saf.domain.kode.Tema.PEN;
import static no.nav.saf.domain.kode.Tema.UFO;
import static no.nav.saf.domain.tilgangsmodell.BaseTilgangMapper.mapSkjerming;
import static no.nav.saf.domain.tilgangsmodell.BaseTilgangMapper.mapTilgangBrukerUtenTilknyttetSak;
import static no.nav.saf.domain.tilgangsmodell.BaseTilgangMapper.mapTilgangSakUtenSakstilknytning;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
@Component
class HentDokumentTilgangService {
	private final HentDokumentAntiCorruptionLayer hentDokumentAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;
	private final FpsakAntiCorruptionLayer fpsakAntiCorruptionLayer;
	private final K9AntiCorruptionLayer k9AntiCorruptionLayer;
	private final PdlAntiCorruptionLayer pdlAntiCorruptionLayer;

	public HentDokumentTilgangService(HentDokumentAntiCorruptionLayer hentDokumentAntiCorruptionLayer,
									  PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
									  BisysAntiCorruptionLayer bisysAntiCorruptionLayer,
									  FpsakAntiCorruptionLayer fpsakAntiCorruptionLayer,
									  K9AntiCorruptionLayer k9AntiCorruptionLayer, PdlAntiCorruptionLayer pdlAntiCorruptionLayer) {
		this.hentDokumentAntiCorruptionLayer = hentDokumentAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
		this.fpsakAntiCorruptionLayer = fpsakAntiCorruptionLayer;
		this.k9AntiCorruptionLayer = k9AntiCorruptionLayer;
		this.pdlAntiCorruptionLayer = pdlAntiCorruptionLayer;
	}

	HentDokumentTilgang hentDokumentTilgang(String journalpostId, String dokumentInfoId, String variantFormat) {
		ArkivJournalpost arkivJournalpost = hentDokumentAntiCorruptionLayer.hentDokumentTilgang(journalpostId, dokumentInfoId);
		TilgangBruker tilgangBruker = mapTilgangBruker(arkivJournalpost);
		TilgangSak tilgangSak = mapTilgangSak(tilgangBruker, arkivJournalpost);
		TilgangJournalpost tilgangJournalpost = mapTilgangJournalpost(variantFormat, arkivJournalpost);
		return new HentDokumentTilgang(tilgangBruker, tilgangSak, tilgangJournalpost);
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
				return TilgangBruker.builder()
						.foedselsnr(fnr)
						.build();
			}
		} else {
			ArkivSak arkivSak = arkivSaksrelasjon.sak();
			ArkivBruker arkivBruker = arkivJournalpost.bruker();
			if (isNotBlank(arkivSak.aktoerId())) {
				try {
					TilgangBruker tilgangBruker = pdlAntiCorruptionLayer.hentTilgangBrukerByAktoerId(arkivSak.aktoerId());
					if (tilgangBruker != null && tilgangBruker.isPerson() && tilgangBruker.getFoedselsnr() != null) {
						return tilgangBruker;
					}
				} catch (PersonIkkeFunnetException e) {
					log.warn("Fant ikke bruker for aktørId={} i PDL, fortsetter med data fra t_bruker", arkivSak.aktoerId(), e);
				}
			}
			return TilgangBruker.builder()
					.aktoerId(arkivSak.aktoerId())
					.orgnummer(trim(arkivSak.orgNr()))
					.foedselsnr(arkivBruker != null && arkivBruker.isPerson() ? arkivBruker.id() : null)
					.build();
		}
	}

	private TilgangSak mapTilgangSak(TilgangBruker tilgangBruker, ArkivJournalpost arkivJournalpost) {
		if (arkivJournalpost.isTilknyttetSak()) {
			return tilgangSakMedSakstilknytning(tilgangBruker, arkivJournalpost);
		} else {
			return mapTilgangSakUtenSakstilknytning(arkivJournalpost);
		}
	}

	private TilgangSak tilgangSakMedSakstilknytning(TilgangBruker tilgangBruker, ArkivJournalpost arkivJournalpost) {
		ArkivSaksrelasjon arkivSaksrelasjon = arkivJournalpost.saksrelasjon();
		if (arkivSaksrelasjon.isPensjonsak()) {
			return mapTilgangPensjonSak(tilgangBruker, arkivJournalpost);
		} else {
			Arkivsak arkivsak = mapArkivsak(arkivJournalpost);
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
				.avsluttet(arkivsak.isAvsluttet())
				.orgnummer(trim(arkivsak.getOrgnummer()))
				.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
				.fagsaksystem(arkivsak.getFagsaksystem())
				.fpAktoerIdList(fpsak)
				.k9AktoerIdList(k9sak)
				.build();
	}

	private TilgangSak mapTilgangPensjonSak(TilgangBruker tilgangBruker, ArkivJournalpost arkivJournalpost) {
		ArkivSaksrelasjon arkivSaksrelasjon = arkivJournalpost.saksrelasjon();
		List<Arkivsak> arkivsaker = pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker, Arrays.asList(PEN, UFO));
		return arkivsaker.stream().filter(p -> p.getArkivsaksnummer().equals(valueOf(arkivSaksrelasjon.sakId())))
				.map(psakArkivsak -> TilgangSak.builder()
						.aktoerId(psakArkivsak.getAktoerId())
						.arkivsaksnummer(psakArkivsak.getArkivsaksnummer())
						.arkivsaksystem(PSAK)
						.avsluttet(psakArkivsak.isAvsluttet())
						.tema(psakArkivsak.getTema())
						.orgnummer(trim(psakArkivsak.getOrgnummer()))
						.relevanteTredjeparter(new ArrayList<>())
						.fagsaksystem(psakArkivsak.getFagsaksystem())
						.build()).findFirst()
				.orElseGet(() -> mapTilgangSakUtenSakstilknytning(arkivJournalpost));
	}

	private TilgangJournalpost mapTilgangJournalpost(String variantFormat, ArkivJournalpost arkivJournalpost) {
		ArkivDokumentinfo arkivDokumentinfoOpt = arkivJournalpost.dokumenter().get(0);
		Long journalpostId = arkivJournalpost.journalpostId();
		return TilgangJournalpost.builder()
				.journalpostId(journalpostId)
				.journalstatus(valueOf(arkivJournalpost.status()).toSafJournalstatus())
				.journalpostTittel(arkivJournalpost.innhold())
				.skjerming(mapSkjerming(arkivJournalpost.skjerming()))
				.dokumenter(mapTilgangDokumentInfo(journalpostId, variantFormat, arkivDokumentinfoOpt))
				.build();
	}

	private static List<TilgangDokumentInfo> mapTilgangDokumentInfo(Long journalpostId, String variantFormat, ArkivDokumentinfo arkivDokumentinfo) {
		Long dokumentInfoId = arkivDokumentinfo.dokumentInfoId();
		return List.of(TilgangDokumentInfo.builder()
				.skjerming(mapSkjerming(arkivDokumentinfo.skjerming()))
				.tilgangDokumentvarianter(mapTilgangDokumentvarianter(journalpostId, dokumentInfoId, variantFormat, arkivDokumentinfo.fildetaljer()))
				.journalpostId(valueOf(journalpostId))
				.dokumentInfoId(valueOf(dokumentInfoId))
				.build());
	}

	private static List<TilgangDokumentvariant> mapTilgangDokumentvarianter(Long journalpostId, Long dokumentInfoId, String variantFormat, List<ArkivFildetaljer> fildetaljer) {
		return fildetaljer.stream()
				.filter(f -> variantFormat.equals(f.format()))
				.map(arkivFildetaljer -> TilgangDokumentvariant.builder()
						.skjerming(mapSkjerming(arkivFildetaljer.skjerming()))
						.variantformat(VariantFormatCode.valueOf(arkivFildetaljer.format()).getSafVariantformat())
						.journalpostId(valueOf(journalpostId))
						.dokumentInfoId(valueOf(dokumentInfoId))
						.build()).collect(Collectors.toList());
	}
}
