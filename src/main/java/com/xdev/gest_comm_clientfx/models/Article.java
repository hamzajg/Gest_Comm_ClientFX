package com.xdev.gest_comm_clientfx.models;

import java.math.BigDecimal;

public class Article {

    private Long id_article;
    private String ref_article;
    private String desc_article;
    private BigDecimal prix_achat_unitaire;
    private Double remise_achat;
    private Double tva_achat;
    private BigDecimal prix_vente_unitaire;
    private Double remise_vente;
    private Double tva_vente;
    private BigDecimal dernier_prix_vente_unitaire;
}
