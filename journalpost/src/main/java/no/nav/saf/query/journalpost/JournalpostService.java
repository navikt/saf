package no.nav.saf.query.journalpost;

import graphql.schema.DataFetchingFieldSelectionSet;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.PdlAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.fpsak.FpsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSak;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSaksrelasjon;
import no.nav.saf.anticorruptionlayer.k9.K9AntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.exceptions.SakstilknytningTechnicalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.lang.String.valueOf;
import static no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer.SAFINTERN_FETCHPATHS_UTEN_DOKUMENTER;
import static no.nav.saf.anticorruptionlayer.joark.domain.ArkivsakMapper.mapArkivsak;
import static no.nav.saf.domain.kode.Arkivsakssystem.GSAK;
import static no.nav.saf.domain.kode.Arkivsakssystem.PSAK;
import static no.nav.saf.domain.kode.Tema.PEN;
import static no.nav.saf.domain.kode.Tema.UFO;
import static no.nav.saf.domain.tilgangsmodell.BaseTilgangMapper.mapTilgangBrukerUtenTilknyttetSak;
import static no.nav.saf.domain.tilgangsmodell.BaseTilgangMapper.mapTilgangJournalpost;
import static no.nav.saf.domain.tilgangsmodell.BaseTilgangMapper.mapTilgangSakUtenSakstilknytning;
import static no.nav.saf.query.journalpost.JournalpostQuery.SELECTION_JOURNALPOST_DOKUMENTER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
@Component
public class JournalpostService {
	private final JoarkAntiCorruptionLayer joarkAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;
	private final FpsakAntiCorruptionLayer fpsakAntiCorruptionLayer;
	private final K9AntiCorruptionLayer k9AntiCorruptionLayer;
	private final PdlAntiCorruptionLayer pdlAntiCorruptionLayer;

	public JournalpostService(JoarkAntiCorruptionLayer joarkAntiCorruptionLayer,
							  PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
							  BisysAntiCorruptionLayer bisysAntiCorruptionLayer,
							  FpsakAntiCorruptionLayer fpsakAntiCorruptionLayer,
							  K9AntiCorruptionLayer k9AntiCorruptionLayer,
							  PdlAntiCorruptionLayer pdlAntiCorruptionLayer) {
		this.joarkAntiCorruptionLayer = joarkAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
		this.fpsakAntiCorruptionLayer = fpsakAntiCorruptionLayer;
		this.k9AntiCorruptionLayer = k9AntiCorruptionLayer;
		this.pdlAntiCorruptionLayer = pdlAntiCorruptionLayer;
	}

	JournalpostHolder hentJournalpost(String journalpostId, String eksternReferanseId, SafRequestContext safRequestContext, DataFetchingFieldSelectionSet selectionSet) {
		ArkivJournalpost arkivJournalpost = journalpostFromAcl(journalpostId, eksternReferanseId, mapFields(selectionSet));
		TilgangBruker tilgangBruker = mapTilgangBruker(arkivJournalpost);
		TilgangSak tilgangSak = mapTilgangSak(tilgangBruker, arkivJournalpost, safRequestContext);
		TilgangJournalpost tilgangJournalpost = mapTilgangJournalpost(arkivJournalpost);

		return new JournalpostHolder(arkivJournalpost, new JournalpostTilgang(tilgangBruker, tilgangSak, tilgangJournalpost));
	}

	private Set<String> mapFields(DataFetchingFieldSelectionSet selectionSet) {
		if (selectionSet.contains(SELECTION_JOURNALPOST_DOKUMENTER)) {
			return Set.of();
		} else {
			return SAFINTERN_FETCHPATHS_UTEN_DOKUMENTER;
		}
	}

	private ArkivJournalpost journalpostFromAcl(String journalpostId, String eksternReferanseId, Set<String> fields) {
		if (isNotBlank(journalpostId)) {
			return joarkAntiCorruptionLayer.hentJournalpostById(journalpostId, fields);
		} else {
			return joarkAntiCorruptionLayer.hentJournalpostByEksternReferanseId(eksternReferanseId, fields);
		}
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
			if (arkivSak == null) {
				throw new SakstilknytningTechnicalException("Journalpostens sakstilknytning peker ikke på en arkivsak. " +
															"Sannsynligvis gjelder dette en pensjon-sakstilknytning som har feil metadata. " +
															"Dette er en metadata feil i arkivet og må korrigeres av #team_dokumentløsninger. " +
															"journalpostId=" + arkivJournalpost.journalpostId() + ", saksrelasjon.sakId=" + arkivSaksrelasjon.sakId() + ", saksrelasjon.fagsystem=" + arkivSaksrelasjon.fagsystem());
			}
			return TilgangBruker.builder()
					.aktoerId(arkivSak.aktoerId())
					.orgnummer(arkivSak.aktoerId() == null ? trim(arkivSak.orgNr()) : null)
					.build();
		}
	}

	private TilgangSak mapTilgangSak(TilgangBruker tilgangBruker, ArkivJournalpost arkivJournalpost, SafRequestContext safRequestContext) {
		if (arkivJournalpost.isTilknyttetSak()) {
			return mapTilgangSakMedSakstilknytning(tilgangBruker, arkivJournalpost, safRequestContext);
		} else {
			return mapTilgangSakUtenSakstilknytning(arkivJournalpost);
		}
	}

	private TilgangSak mapTilgangSakMedSakstilknytning(TilgangBruker tilgangBruker, ArkivJournalpost arkivJournalpost, SafRequestContext safRequestContext) {
		ArkivSaksrelasjon arkivSaksrelasjon = arkivJournalpost.saksrelasjon();
		if (arkivSaksrelasjon.isPensjonsak()) {
			return mapTilgangPensjonSak(tilgangBruker, arkivJournalpost, safRequestContext);
		} else {
			Arkivsak arkivsak = mapArkivsak(arkivJournalpost);
			safRequestContext.getRequestCache().putArkivsak(arkivsak);
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
				.peek(pensjonArkivsak -> safRequestContext.getRequestCache().putArkivsak(pensjonArkivsak))
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

}
