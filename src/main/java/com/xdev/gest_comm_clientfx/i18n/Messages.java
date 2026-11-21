package com.xdev.gest_comm_clientfx.i18n;

import com.xdev.gest_comm_clientfx.ui.AppPreferences;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The application's externalized strings (specs/login-profile-settings US6,
 * D6). French is the base bundle, English the {@code _en} one; the choice
 * lives in the workstation preferences and applies on the next scene load.
 *
 * Keys used by FXML ({@code text="%key"}) are resolved by the loader in
 * {@code ui.Views}; keys used from Java go through {@link #get}.
 */
public final class Messages {

    /** Classpath base name, beside the views (both files must ship). */
    public static final String BUNDLE =
        "com.xdev.gest_comm_clientfx.views.messages";

    /**
     * Resolve exactly the requested language, then the base bundle. The
     * default control falls back to the JVM's default locale before the base
     * bundle, so on an English machine a request for French loaded the
     * English file — the opposite of what the language choice means.
     */
    private static final ResourceBundle.Control CONTROL =
        new ResourceBundle.Control() {
            @Override
            public Locale getFallbackLocale(String baseName, Locale locale) {
                return null;
            }
        };

    private Messages() { }

    /** The locale the user chose on this workstation. */
    public static Locale locale() {
        return "en".equals(AppPreferences.load().language())
            ? Locale.ENGLISH : Locale.FRENCH;
    }

    /** The bundle for the current choice; a new scene loader gets a fresh one. */
    public static ResourceBundle resourceBundle() {
        return ResourceBundle.getBundle(BUNDLE, locale(), CONTROL);
    }

    /** A string for the current language; a missing key shows the key itself. */
    public static String get(String key, Object... args) {
        return get(locale(), key, args);
    }

    /** A string for an explicit language (tests, capture tools). */
    public static String get(Locale locale, String key, Object... args) {
        try {
            String raw = ResourceBundle.getBundle(BUNDLE, locale, CONTROL)
                .getString(key);
            return args.length == 0 ? raw : MessageFormat.format(raw, args);
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
