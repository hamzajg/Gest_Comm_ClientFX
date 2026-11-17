package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;

/**
 * Contract for the create/edit dialogs launched from HomeScreenCtrl:
 * the host injects the shared ApiClient and a refresh callback.
 */
public interface ApiDialog {

    void setApi(ApiClient api);

    void setOnSaved(Runnable onSaved);
}
