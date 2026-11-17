package com.xdev.gest_comm_clientfx.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * LigneDevis as embedded in the PyGestComm devis payload. `article` is
 * the backend idArticle (nullable for free-text lines); `prixUnitaire`
 * may be null — the API falls back to the article's sale price.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LigneDevis {

    private Long idLigneDevis;
    private Long devis;
    private Long article;
    private String designation;
    private BigDecimal quantite;
    private BigDecimal prixUnitaire;
    private Double remise;
    private Double tva;
    private Double totalHT;
    private Double totalTVA;

    public Long getIdLigneDevis() { return idLigneDevis; }
    public void setIdLigneDevis(Long idLigneDevis) { this.idLigneDevis = idLigneDevis; }
    public Long getDevis() { return devis; }
    public void setDevis(Long devis) { this.devis = devis; }
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
