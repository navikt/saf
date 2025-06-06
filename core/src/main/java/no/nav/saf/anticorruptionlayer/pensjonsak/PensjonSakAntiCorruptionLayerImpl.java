package no.nav.saf.anticorruptionlayer.pensjonsak;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.pensjonsak.domain.SakSammendrag;
import no.nav.saf.anticorruptionlayer.pensjonsak.hentbrukerforsak.PensjonSakRestConsumer;
import no.nav.saf.anticorruptionlayer.pensjonsak.hentbrukerforsak.PersonHarIngenPensjonssakerException;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Slf4j
@Component
public class PensjonSakAntiCorruptionLayerImpl implements PensjonSakAntiCorruptionLayer {
	public static final String PSAK_FAGSYSTEM = "PP01";
	private static final Predicate<SakSammendrag> selectAllSakSammendrag = __ -> true;

	private final PensjonSakRestConsumer pensjonSakRestConsumer;

	@Autowired
	public PensjonSakAntiCorruptionLayerImpl(PensjonSakRestConsumer pensjonSakRestConsumer) {
		this.pensjonSakRestConsumer = pensjonSakRestConsumer;
	}

	@Override
	public List<Arkivsak> findArkivsaker(final TilgangBruker tilgangBruker, final List<Tema> tema) {
		if (tilgangBruker == null || tilgangBruker.getFoedselsnr() == null || tema.isEmpty()) {
			return emptyList();
		} else {
			Predicate<SakSammendrag> selectForTema = sakSammendrag -> tema.contains(mapToTema(sakSammendrag.arkivtema()));
			return findArkivsaker(tilgangBruker, selectForTema);
		}
	}

	@Override
	public List<Arkivsak> findArkivsaker(final TilgangBruker tilgangBruker) {
		return findArkivsaker(tilgangBruker, selectAllSakSammendrag);
	}

	private List<Arkivsak> findArkivsaker(TilgangBruker tilgangBruker, Predicate<SakSammendrag> sakSammendragSelector) {
		try {
			if (tilgangBruker.getFoedselsnr() == null) {
				return emptyList();
			} else {
				return pensjonSakRestConsumer.hentSakSammendragListe(tilgangBruker.getFoedselsnr())
						.stream()
						.filter(sakSammendragSelector)
						.map(sakSammendrag -> Arkivsak.builder()
								.aktoerId(tilgangBruker.getAktoerId())
								.arkivsaksnummer(sakSammendrag.sakId())
								.arkivsaksystem(Arkivsakssystem.PSAK)
								.fagsakId(sakSammendrag.sakId())
								.fagsaksystem(PSAK_FAGSYSTEM)
								.avsluttet(false)
								.tema(mapToTema(sakSammendrag.arkivtema()))
								.datoOpprettet(getDatoOpprettet(sakSammendrag))
								.build())
						.collect(Collectors.toList());
			}
		} catch (PersonHarIngenPensjonssakerException e) {
			log.info("Person har ingen pensjonssaker", e);
			return emptyList();
		} catch (Exception e) {
			log.warn("Klarte ikke hente pensjonssaker for fødselsnummer={}", "*****", e);
			return emptyList();
		}
	}

	@Override
	public String findFoedselsnummerBySakId(String sakId) {
		try {
			return pensjonSakRestConsumer.hentBrukerForSak(sakId).getFnr();
		} catch (Exception e) {
			log.warn("Klarte ikke å hente brukerId (fødselsnummer) for pensjonsak med sakId={}", sakId, e);
			return null;
		}
	}

	private Tema mapToTema(String tema) {
		if (tema == null) {
			return Tema.PEN;
		}
		try {
			return Tema.valueOf(tema);
		} catch (Exception e) {
			return Tema.PEN;
		}
	}

	private static LocalDateTime getDatoOpprettet(SakSammendrag sakSammendrag) {
		if (sakSammendrag.saksperiode().fom() == null) {
			return null;
		} else {
			return sakSammendrag.saksperiode().fom().atStartOfDay();
		}
	}
}
