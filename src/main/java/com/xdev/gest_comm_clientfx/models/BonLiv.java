package com.xdev.gest_comm_clientfx.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * BonLiv (delivery note) as returned by PyGestComm /api/bonliv. Field
 * names match the backend JSON 1:1. `client` is the backend idClient;
 * `codeClient` / `nomPrenomClient` / totals are read-only extras.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BonLiv {

    private Long idBonLiv;
    private String codeBonLiv;
    private String dateBonLiv;
    private Long client;
    private String statutBonLiv;
    private String observationBonLiv;
    private List<LigneBonLiv> lignes;
    private String codeClient;
    private String nomPrenomClient;
    private Double totalHT;
    private Double totalTVA;
    private Double totalTTC;

    public Long getIdBonLiv() { return idBonLiv; }
    public void setIdBonLiv(Long idBonLiv) { this.idBonLiv = idBonLiv; }
    public String getCodeBonLiv() { return codeBonLiv; }
    public void setCodeBonLiv(String codeBonLiv) { this.codeBonLiv = codeBonLiv; }
    public String getDateBonLiv() { return dateBonLiv; }
    public void setDateBonLiv(String dateBonLiv) { this.dateBonLiv = dateBonLiv; }
    public Long getClient() { return client; }
    public void setClient(Long client) { this.client = client; }
    public String getStatutBonLiv() { return statutBonLiv; }
    public void setStatutBonLiv(String statutBonLiv) { this.statutBonLiv = statutBonLiv; }
    public String getObservationBonLiv() { return observationBonLiv; }
    public void setObservationBonLiv(String observationBonLiv) { this.observationBonLiv = observationBonLiv; }
    public List<LigneBonLiv> getLignes() { return lignes; }
    public void setLignes(List<LigneBonLiv> lignes) { this.lignes = lignes; }
    public String getCodeClient() { return codeClient; }
    public void setCodeClient(String codeClient) { this.codeClient = codeClient; }
    public String getNomPrenomClient() { return nomPrenomClient; }
    public void setNomPrenomClient(String nomPrenomClient) { this.nomPrenomClient = nomPrenomClient; }
    public Double getTotalHT() { return totalHT; }
    public void setTotalHT(Double totalHT) { this.totalHT = totalHT; }
    public Double getTotalTVA() { return totalTVA; }
    public void setTotalTVA(Double totalTVA) { this.totalTVA = totalTVA; }
    public Double getTotalTTC() { return totalTTC; }
    public void setTotalTTC(Double totalTTC) { this.totalTTC = totalTTC; }

    @Override
    public String toString() {
        return codeBonLiv + " — " + nomPrenomClient
            + " — TTC " + totalTTC;
    }
}
