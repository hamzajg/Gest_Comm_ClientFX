package com.xdev.gest_comm_clientfx.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.gest_comm_clientfx.models.Article;
import com.xdev.gest_comm_clientfx.models.Client;
import com.xdev.gest_comm_clientfx.models.Devis;
import com.xdev.gest_comm_clientfx.models.Facture;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifies the models deserialize the exact JSON PyGestComm returns —
 * camelCase keys, decimals as strings, phones as ints.
 */
class ApiJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void clientListMapsBackendPayload() throws Exception {
        String json = "[{\"idClient\":1,\"codeClient\":\"C001\","
            + "\"nomPrenomClient\":\"Ada Lovelace\","
            + "\"matFiscalClient\":\"1234567\","
            + "\"addresseClient1\":\"1 Way\",\"addresseClient2\":\"-\","
            + "\"numTelClient1\":11111111,\"numTelClient2\":0}]";

        List<Client> clients = mapper.readValue(json,
            mapper.getTypeFactory()
                .constructCollectionType(List.class, Client.class));

        assertEquals(1, clients.size());
        Client c = clients.get(0);
        assertEquals("C001", c.getCodeClient());
        assertEquals("Ada Lovelace", c.getNomPrenomClient());
        assertEquals(11111111, c.getNumTelClient1());
    }

    @Test
    void articleListMapsCamelCaseBackendPayload() throws Exception {
        String json = "[{\"idArticle\":1,\"refArticle\":\"ART-001\","
            + "\"descArticle\":\"Widget\","
            + "\"prixAchatUnitaire\":\"50.00\",\"remiseAchat\":0.0,"
            + "\"tvaAchat\":20.0,\"prixVenteUnitaire\":\"100.00\","
            + "\"remiseVente\":0.0,\"tvaVente\":20.0,"
            + "\"dernierPrixVenteUnitaire\":null}]";

        List<Article> articles = mapper.readValue(json,
            mapper.getTypeFactory()
                .constructCollectionType(List.class, Article.class));

        assertEquals(1, articles.size());
        Article a = articles.get(0);
        assertEquals("ART-001", a.getRef_article());
        assertEquals("Widget", a.getDesc_article());
        assertEquals("100.00", a.getPrix_vente_unitaire().toPlainString());
        assertEquals(20.0, a.getTva_vente());
    }

    @Test
    void devisListMapsBackendPayloadWithLinesAndTotals() throws Exception {
        String json = "[{\"idDevis\":1,\"codeDevis\":\"D001\","
            + "\"dateDevis\":\"2026-01-15\",\"client\":1,"
            + "\"statutDevis\":\"brouillon\",\"observationDevis\":\"-\","
            + "\"codeClient\":\"C001\",\"nomPrenomClient\":\"Ada Lovelace\","
            + "\"totalHT\":250.0,\"totalTVA\":45.0,\"totalTTC\":295.0,"
            + "\"lignes\":[{\"idLigneDevis\":1,\"devis\":1,\"article\":1,"
            + "\"designation\":\"Widget\",\"quantite\":\"2.00\","
            + "\"prixUnitaire\":\"100.00\",\"remise\":0.0,\"tva\":20.0,"
            + "\"totalHT\":200.0,\"totalTVA\":40.0},"
            + "{\"idLigneDevis\":2,\"devis\":1,\"article\":null,"
            + "\"designation\":\"Frais\",\"quantite\":\"1.00\","
            + "\"prixUnitaire\":\"50.00\",\"remise\":0.0,\"tva\":10.0,"
            + "\"totalHT\":50.0,\"totalTVA\":5.0}]}]";

        List<Devis> devis = mapper.readValue(json,
            mapper.getTypeFactory()
                .constructCollectionType(List.class, Devis.class));

        assertEquals(1, devis.size());
        Devis d = devis.get(0);
        assertEquals("D001", d.getCodeDevis());
        assertEquals(1L, d.getClient());
        assertEquals("C001", d.getCodeClient());
        assertEquals(295.0, d.getTotalTTC());
        assertEquals(2, d.getLignes().size());
        assertEquals("Widget", d.getLignes().get(0).getDesignation());
        assertEquals(1L, d.getLignes().get(0).getArticle());
        assertNull(d.getLignes().get(1).getArticle());
    }

    @Test
    void factureListMapsBackendPayloadWithDevisLink() throws Exception {
        String json = "[{\"idFacture\":1,\"codeFacture\":\"F0001\","
            + "\"dateFacture\":\"2026-01-20\",\"client\":1,\"devis\":1,"
            + "\"statutFacture\":\"brouillon\",\"observationFacture\":\"-\","
            + "\"codeClient\":\"C001\",\"nomPrenomClient\":\"Ada Lovelace\","
            + "\"codeDevis\":\"D001\","
            + "\"totalHT\":200.0,\"totalTVA\":40.0,\"totalTTC\":240.0,"
            + "\"lignes\":[{\"idLigneFacture\":1,\"facture\":1,"
            + "\"article\":1,\"designation\":\"Widget\","
            + "\"quantite\":\"2.00\",\"prixUnitaire\":\"100.00\","
            + "\"remise\":0.0,\"tva\":20.0,\"totalHT\":200.0,"
            + "\"totalTVA\":40.0}]}]";

        List<Facture> factures = mapper.readValue(json,
            mapper.getTypeFactory()
                .constructCollectionType(List.class, Facture.class));

        assertEquals(1, factures.size());
        Facture f = factures.get(0);
        assertEquals("F0001", f.getCodeFacture());
        assertEquals(1L, f.getDevis());
        assertEquals("D001", f.getCodeDevis());
        assertEquals(240.0, f.getTotalTTC());
        assertEquals(1, f.getLignes().size());
        assertEquals("Widget", f.getLignes().get(0).getDesignation());
    }
}
