package com.xdev.gest_comm_clientfx.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArticleTest {

    @Test
    void accessorsRoundTripAllFields() {
        Article article = new Article();
        article.setId_article(42L);
        article.setRef_article("ART-42");
        article.setDesc_article("Widget");
        article.setPrix_achat_unitaire(new BigDecimal("10.50"));
        article.setRemise_achat(5.0);
        article.setTva_achat(20.0);
        article.setPrix_vente_unitaire(new BigDecimal("15.00"));
        article.setRemise_vente(0.0);
        article.setTva_vente(20.0);
        article.setDernier_prix_vente_unitaire(new BigDecimal("14.90"));

        assertEquals(42L, article.getId_article());
        assertEquals("ART-42", article.getRef_article());
        assertEquals("Widget", article.getDesc_article());
        assertEquals(new BigDecimal("10.50"), article.getPrix_achat_unitaire());
        assertEquals(5.0, article.getRemise_achat());
        assertEquals(20.0, article.getTva_achat());
        assertEquals(new BigDecimal("15.00"), article.getPrix_vente_unitaire());
        assertEquals(0.0, article.getRemise_vente());
        assertEquals(20.0, article.getTva_vente());
        assertEquals(new BigDecimal("14.90"), article.getDernier_prix_vente_unitaire());
    }
}
