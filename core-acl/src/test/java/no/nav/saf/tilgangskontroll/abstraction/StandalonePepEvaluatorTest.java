package no.nav.saf.tilgangskontroll.abstraction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Hans Petter Simonsen - Miles
 */
@ExtendWith(MockitoExtension.class)

class StandalonePepEvaluatorTest {

	private class BrukerPepEvaluator extends StandalonePepEvaluator<TilgangBruker> {

		public BrukerPepEvaluator(StandalonePepEvaluator parent, SecModelDataFetcher dataFetcher, Pep pep, SecModelParameterAdapter parameterAdapter) {
			super(parent, dataFetcher, pep, parameterAdapter);
		}
	}

	private class BrukerDataFetcher implements SecModelDataFetcher<TilgangBruker> {

		@Override
		public List<TilgangBruker> fetchAndFilter(ParameterContext parameterContext) {
			List<TilgangBruker> brukers = Lists.newArrayList(TilgangBruker.builder().aktoerId("123456789").build());
			return brukers.stream().filter(bruker -> parameterContext.getParameter("aktoerId").equals(bruker.getAktoerId())).collect(Collectors.toList());
		}
	}

	private class SakPepEvaluator extends StandalonePepEvaluator<TilgangSak> {

		public SakPepEvaluator(StandalonePepEvaluator parent, SecModelDataFetcher<TilgangSak> dataFetcher, Pep<TilgangSak> pep, SecModelParameterAdapter parameterAdapter) {
			super(parent, dataFetcher, pep, parameterAdapter);
		}
	}

	private class SakParameterAdapter implements SecModelParameterAdapter<TilgangSak> {

		@Override
		public ParameterContext extractSearchParameter(TilgangSak sak) {
			Map<String, String> parameterMap = Maps.newHashMap();
			parameterMap.put("aktoerId", sak.getAktoerId());
			return new ParameterContext(parameterMap);
		}
	}

	private class SakDataFetcher implements SecModelDataFetcher<TilgangSak>{

		@Override
		public List<TilgangSak> fetchAndFilter(ParameterContext parameterContext) {
			List<TilgangSak> saker = Lists.newArrayList( sak1, sak2);
			return saker.stream().filter( sak ->
					sak.getArkivsaksystem().equals(parameterContext.getParameter("arkivsaksystem"))
							&& sak.getArkivsaksnummer().equals(parameterContext.getParameter("arkivsaksnummer"))
			).collect(Collectors.toList());
		}
	}

	private class JPPepEvaluator extends StandalonePepEvaluator<TilgangJournalpost> {

		public JPPepEvaluator(StandalonePepEvaluator parent, SecModelDataFetcher<TilgangJournalpost> dataFetcher, Pep<TilgangJournalpost> pep, SecModelParameterAdapter parameterAdapter) {
			super(parent, dataFetcher, pep, parameterAdapter);
		}

	}

	private class JPParameterAdapter implements SecModelParameterAdapter<TilgangJournalpost> {
		@Override
		public ParameterContext extractSearchParameter(TilgangJournalpost journalpost) {
			Map<String, String> parameterMap = Maps.newHashMap();
			parameterMap.put("arkivsaksnummer", journalpost.getArkivsaksnummer());
			parameterMap.put("arkivsaksystem", journalpost.getArkivsaksystem());
			return new ParameterContext(parameterMap);
		}
	}

	private class JPDataFetcher implements SecModelDataFetcher<TilgangJournalpost> {
		@Override
		public List<TilgangJournalpost> fetchAndFilter(ParameterContext parameterContext) {
			List<TilgangJournalpost> jps = Lists.newArrayList(
					TilgangJournalpost.builder().journalpostId("1234").arkivsaksnummer("123").arkivsaksystem("FS22").build(),
					TilgangJournalpost.builder().journalpostId("2345").arkivsaksnummer("234").arkivsaksystem("PEN").build()
			);
			if (parameterContext.getListParameter("journalpostIds") != null) {
				return jps.stream().filter(jp -> parameterContext.getListParameter("journalpostIds").contains(jp.getJournalpostId())).collect(Collectors.toList());
			} else if (parameterContext.getParameter("journalpostId") != null){
				return jps.stream().filter(jp -> jp.getJournalpostId().equals(parameterContext.getParameter("journalpostId"))).collect(Collectors.toList());
			} else if (parameterContext.getListParameter("psakSaker") != null || parameterContext.getListParameter("gsakSaker") != null) {
				return jps.stream().filter(jp ->
						parameterContext.getListParameter("gsakSaker").contains(jp.getArkivsaksnummer()) && jp.getArkivsaksystem().equals("FS22")
								|| parameterContext.getListParameter("psakSaker").contains(jp.getArkivsaksnummer()) && jp.getArkivsaksystem().equals("PEN")
				).collect(Collectors.toList());
			} else {
				return Lists.newArrayList();
			}
		}
	}

	JPPepEvaluator jpPepEvaluator;
	SakPepEvaluator sakPepEvaluator;
	BrukerPepEvaluator brukerPepEvaluator;

	@Mock()
	Pep<TilgangBruker> pep1;
	@Mock
	Pep<TilgangSak> pep2;
	@Mock
	Pep<TilgangJournalpost> pep3;

	AccessDecisionContext accessDecisionContext;
	ParameterContext parameterContext;
	SecModelWorld secModelWorld;

	TilgangSak sak1 = TilgangSak.builder().aktoerId("123456789").arkivsaksnummer("123").arkivsaksystem("FS22").build();
	TilgangSak sak2 = TilgangSak.builder().aktoerId("123456789").arkivsaksnummer("234").arkivsaksystem("PEN").build();




	@BeforeEach
	public void setUp() {
		accessDecisionContext = new AccessDecisionContext();
		parameterContext = new ParameterContext();

		brukerPepEvaluator = new BrukerPepEvaluator(null, new BrukerDataFetcher(), pep1, null);
		sakPepEvaluator = new SakPepEvaluator(brukerPepEvaluator, new SakDataFetcher(), pep2, new SakParameterAdapter());
		jpPepEvaluator = new JPPepEvaluator(sakPepEvaluator, new JPDataFetcher(), pep3, new JPParameterAdapter());
		secModelWorld = new SecModelWorld();
	}

	@Test
	public void shouldReturnJournalpostInHappyPath() {
		when(pep1.hasAccesOn(any(TilgangBruker.class), any(AccessDecisionContext.class))).thenReturn(true);
		when(pep2.hasAccesOn(any(TilgangSak.class), any(AccessDecisionContext.class))).thenReturn(true);
		when(pep3.hasAccesOn(any(TilgangJournalpost.class), any(AccessDecisionContext.class))).thenReturn(true);

		parameterContext.putParameter("journalpostId", "1234");
		List<TilgangJournalpost> journalposts = jpPepEvaluator.fetchAndFilterAndEnforce(parameterContext, accessDecisionContext, secModelWorld);
		assertTrue(journalposts.size() == 1);
		assertThat(journalposts.get(0).getJournalpostId(), Is.is("1234"));

		assertTrue(secModelWorld.getBrukere().size() == 1);
		assertTrue(secModelWorld.getSaker().size() == 1);
		assertTrue(secModelWorld.getJournalposter().size() == 1);
	}

	@Test
	public void shouldReturnEmptyListIfAccessDenyOnBruker() {
		when(pep1.hasAccesOn(any(TilgangBruker.class), any(AccessDecisionContext.class))).thenReturn(false);
		when(pep2.hasAccesOn(any(TilgangSak.class), any(AccessDecisionContext.class))).thenReturn(true);
		when(pep3.hasAccesOn(any(TilgangJournalpost.class), any(AccessDecisionContext.class))).thenReturn(true);

		parameterContext.putParameter("journalpostId", "1234");
		List<TilgangJournalpost> journalposts = jpPepEvaluator.fetchAndFilterAndEnforce(parameterContext, accessDecisionContext, secModelWorld);
		assertTrue(CollectionUtils.isEmpty(journalposts));
	}

	@Test
	public void shouldSupportJournalpostListParameter() {
		when(pep1.hasAccesOn(any(TilgangBruker.class), any(AccessDecisionContext.class))).thenReturn(true);
		when(pep2.hasAccesOn(any(TilgangSak.class), any(AccessDecisionContext.class))).thenReturn(true);
		when(pep3.hasAccesOn(any(TilgangJournalpost.class), any(AccessDecisionContext.class))).thenReturn(true);

		List<String> journalpostIds = Lists.newArrayList("1234", "2345");
		parameterContext.putParameter("journalpostIds", journalpostIds);
		List<TilgangJournalpost> journalposts = jpPepEvaluator.fetchAndFilterAndEnforce(parameterContext, accessDecisionContext, secModelWorld);
		assertEquals(2, journalposts.size());
	}

	@Test
	public void shouldLimitAccessToOneSak() {
		when(pep1.hasAccesOn(any(TilgangBruker.class), any(AccessDecisionContext.class))).thenReturn(true);
		doReturn(false).when(pep2).hasAccesOn(eq(sak1), eq(accessDecisionContext));
		doReturn(true).when(pep2).hasAccesOn(eq(sak2), eq(accessDecisionContext));
		when(pep3.hasAccesOn(any(TilgangJournalpost.class), any(AccessDecisionContext.class))).thenReturn(true);

		List<String> journalpostIds = Lists.newArrayList("1234", "2345");
		parameterContext.putParameter("journalpostIds", journalpostIds);
		List<TilgangJournalpost> journalposts = jpPepEvaluator.fetchAndFilterAndEnforce(parameterContext, accessDecisionContext, secModelWorld);
		assertEquals(1, journalposts.size());
		assertThat(journalposts.get(0).getJournalpostId(), Is.is("2345"));
	}

	@Test
	public void shouldSupporSakListParameter() {
		when(pep1.hasAccesOn(any(TilgangBruker.class), any(AccessDecisionContext.class))).thenReturn(true);
		when(pep2.hasAccesOn(any(TilgangSak.class), any(AccessDecisionContext.class))).thenReturn(true);
		when(pep3.hasAccesOn(any(TilgangJournalpost.class), any(AccessDecisionContext.class))).thenReturn(true);

		List<String> gsakSaker = Lists.newArrayList("123");
		parameterContext.putParameter("gsakSaker", gsakSaker);
		List<String> psakSaker = Lists.newArrayList("234");
		parameterContext.putParameter("psakSaker", psakSaker);

		List<TilgangJournalpost> journalposts = jpPepEvaluator.fetchAndFilterAndEnforce(parameterContext, accessDecisionContext, secModelWorld);
		assertEquals(2, journalposts.size());
	}

}