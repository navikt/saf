package no.nav.saf.tilgangskontroll.abstraction;

import com.google.common.collect.Lists;
import lombok.Getter;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;

import java.util.List;

@Getter
public class SecModelWorld {

    List<TilgangBruker> brukere;
    List<TilgangSak> saker;
    List<TilgangJournalpost> journalposter;

     public <T> void put(List<T> entiteter) {
         if (!entiteter.isEmpty()) {
             T checkType = entiteter.get(0);
             if (checkType instanceof TilgangBruker) {
                 this.brukere = (List<TilgangBruker>)entiteter;
             } else if (checkType instanceof TilgangSak) {
                 this.saker = (List<TilgangSak>)entiteter;
             } else if (checkType instanceof TilgangJournalpost) {
                 this.journalposter = (List<TilgangJournalpost>)entiteter;
             }

         }
    }

    public void add(TilgangBruker bruker) {
        if (brukere == null) {
            brukere = Lists.newArrayList();
        }
        if (bruker != null) {
            brukere.add(bruker);
        }
     }

     public void add(TilgangSak sak) {
         if (saker == null) {
             saker = Lists.newArrayList();
         }
         if (sak != null) {
             saker.add(sak);
         }
     }

     public void add(TilgangJournalpost journalpost) {
         if (journalposter == null) {
             journalposter = Lists.newArrayList();
         }
         if (journalpost != null) {
             journalposter.add(journalpost);
         }
     }

}
