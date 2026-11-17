package com.xdev.gest_comm_clientfx.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.gest_comm_clientfx.models.Article;
import com.xdev.gest_comm_clientfx.models.Client;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
