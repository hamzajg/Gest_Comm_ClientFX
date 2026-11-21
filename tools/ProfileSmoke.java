import com.xdev.gest_comm_clientfx.app.StartApp;
import com.xdev.gest_comm_clientfx.i18n.Messages;
import com.xdev.gest_comm_clientfx.ui.AppPreferences;
import com.xdev.gest_comm_clientfx.ui.Theme;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Scene smoke for the profile preferences (specs/login-profile-settings US5,
 * US6, D4, D6): the saved skin is installed on the workspace scene, and the
 * saved language rebuilds the shell in that language.
 *
 *   javac -cp "target/classes:$(cat /tmp/cp.txt)" -d /tmp/smoke tools/ProfileSmoke.java
 *   java -cp "/tmp/smoke:target/classes:$(cat /tmp/cp.txt)" ProfileSmoke
 *
 * It writes the two preferences it checks and restores the previous values
 * before exiting, so the desktop app's choices are unchanged.
 */
public class ProfileSmoke {

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.FRENCH);
        CountDownLatch done = new CountDownLatch(1);
        final Throwable[] failure = new Throwable[1];

        Platform.startup(() -> {
            AppPreferences prefs = AppPreferences.load();
            Theme.Skin savedSkin = prefs.skin();
            String savedLanguage = prefs.language();
            try {
                StartApp app = new StartApp();
                Stage stage = new Stage();
                app.start(stage);

                prefs.setSkin(Theme.Skin.DARK);
                prefs.setLanguage("fr");
                app.showWorkspace();
                require(!hasClass(stage.getScene().getRoot(), "light"),
                    "the dark skin is the default");
                require(hasMenu(stage.getScene(), "Mon profil"),
                    "the French shell must offer Mon profil");
                System.out.println("OK  dark skin + French shell");

                prefs.setSkin(Theme.Skin.LIGHT);
                app.showWorkspace();
                require(hasClass(stage.getScene().getRoot(), "light"),
                    "the saved light skin must be installed on the scene");
                System.out.println("OK  light skin applied from the preference");

                prefs.setLanguage("en");
                Locale.setDefault(Messages.locale());
                app.showWorkspace();
                require(hasMenu(stage.getScene(), "My profile"),
                    "the English bundle must rebuild the shell");
                System.out.println("OK  English shell after the language change");

                stage.close();
                System.out.println("PASS");
            } catch (Throwable t) {
                failure[0] = t;
                t.printStackTrace();
            } finally {
                prefs.setSkin(savedSkin);
                prefs.setLanguage(savedLanguage);
                done.countDown();
            }
        });

        if (!done.await(60, TimeUnit.SECONDS)) {
            System.err.println("FAIL timed out waiting for the FX thread");
            System.exit(1);
        }
        System.exit(failure[0] == null ? 0 : 1);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static boolean hasClass(Parent root, String styleClass) {
        return root != null && root.getStyleClass().contains(styleClass);
    }

    /** Does any menu item in the shell carry this (translated) label? */
    private static boolean hasMenu(Scene scene, String label) {
        MenuBar bar = (MenuBar) scene.getRoot().lookup(".menu-bar");
        if (bar == null) {
            return false;
        }
        for (Menu menu : bar.getMenus()) {
            for (MenuItem item : menu.getItems()) {
                if (item != null && label.equals(item.getText())) {
                    return true;
                }
            }
        }
        return false;
    }
}
