package com.xdev.gest_comm_clientfx.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * LigneBonLiv as embedded in the PyGestComm bonliv payload — same shape
 * as LigneDevis but with bonliv-side ids.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LigneBonLiv {

    private Long idLigneBonLiv;
    private Long bonliv;
    private Long article;
    private String designation;
    private BigDecimal quantite;
    private BigDecimal prixUnitaire;
    private Double remise;
    private Double tva;
    private Double totalHT;
    private Double totalTVA;

    public Long getIdLigneBonLiv() { return idLigneBonLiv; }
    public void setIdLigneBonLiv(Long idLigneBonLiv) { this.idLigneBonLiv = idLigneBonLiv; }
    public Long getBonliv() { return bonliv; }
    public void setBonliv(Long bonliv) { this.bonliv = bonliv; }
    public Long getArticle() { return article; }
    public void setArticle(Long article) { this.article = article; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public BigDecimal getQuantite() { return quantite; }
    public void setQuantite(BigDecimal quantite) { this.quantite = quantite; }
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public Double getRemise() { return remise; }
    public void setRemise(Double remise) { this.remise = remise; }
    public Double getTva() { return tva; }
    public void setTva(Double tva) { this.tva = tva; }
    public Double getTotalHT() { return totalHT; }
    public void setTotalHT(Double totalHT) { this.totalHT = totalHT; }
    public Double getTotalTVA() { return totalTVA; }
    public void setTotalTVA(Double totalTVA) { this.totalTVA = totalTVA; }
}
