package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;

/**
 * Contract for the create/edit dialogs launched from HomeScreenCtrl:
 * the host injects the shared ApiClient and a refresh callback.
 */
public interface ApiDialog {

    void setApi(ApiClient api);

    void setOnSaved(Runnable onSaved);

    /**
     * Where closing goes when the form is hosted in a tab: the host closes
     * the tab instead of the window. A form opened in its own window ignores
     * this, so the default does nothing.
     */
    default void setOnClosed(Runnable onClosed) { }
}
