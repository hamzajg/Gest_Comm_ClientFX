package com.xdev.gest_comm_clientfx.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClientTest {

    @Test
    void accessorsRoundTripAllBackendFields() {
        Client client = new Client();
        client.setIdClient(7L);
        client.setCodeClient("CLI-001");
        client.setNomPrenomClient("Dupont Jean");
        client.setMatFiscalClient("123456789");
        client.setAddresseClient1("1 rue de la Paix");
        client.setAddresseClient2("Appt 4B");
        client.setNumTelClient1(201234567);
        client.setNumTelClient2(987654321);

        assertEquals(7L, client.getIdClient());
        assertEquals("CLI-001", client.getCodeClient());
        assertEquals("Dupont Jean", client.getNomPrenomClient());
        assertEquals("123456789", client.getMatFiscalClient());
        assertEquals("1 rue de la Paix", client.getAddresseClient1());
        assertEquals("Appt 4B", client.getAddresseClient2());
        assertEquals(201234567, client.getNumTelClient1());
        assertEquals(987654321, client.getNumTelClient2());
    }

    @Test
    void fieldsDefaultToNull() {
        Client client = new Client();
        assertNull(client.getIdClient());
        assertNull(client.getCodeClient());
        assertNull(client.getNumTelClient1());
    }
}
