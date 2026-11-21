import com.xdev.gest_comm_clientfx.app.StartApp;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * UI smoke for sign-out (specs/login-profile-settings US3, D2): the workspace
 * scene is replaced by the login gate on the same stage, and nothing of the
 * workspace is left behind.
 *
 *   javac -cp "target/classes:$(cat /tmp/cp.txt)" -d /tmp/smoke tools/SignOutSmoke.java
 *   java -cp "/tmp/smoke:target/classes:$(cat /tmp/cp.txt)" SignOutSmoke
 *
 * No backend is needed, and the saved session is neither read nor modified:
 * the workspace is requested explicitly, exactly as `showWorkspace()` does
 * after a successful sign-in. Whether the *button* is wired to this path is
 * covered by ProfileScreenTest; this checks the scene swap itself.
 */
public class SignOutSmoke {

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.FRANCE);
        CountDownLatch done = new CountDownLatch(1);
        final Throwable[] failure = new Throwable[1];

        Platform.startup(() -> {
            try {
                StartApp app = new StartApp();
                Stage stage = new Stage();
                app.start(stage);

                app.showWorkspace();
                Scene workspace = stage.getScene();
                require(workspace != null
                        && !hasClass(workspace.getRoot(), "login-panel"),
                    "a signed-in session must show the workspace scene");
                System.out.println("OK  a session builds the workspace scene");

                // Exactly what ProfileCtrl and the Application menu call.
                app.signOut();
                Scene gate = stage.getScene();
                require(gate != workspace
                        && hasClass(gate.getRoot(), "login-panel"),
                    "sign-out must return to the login gate");
                System.out.println("OK  sign-out returns to the login gate");

                stage.close();
                System.out.println("PASS");
            } catch (Throwable t) {
                failure[0] = t;
                t.printStackTrace();
            } finally {
                done.countDown();
            }
        });

        if (!done.await(60, TimeUnit.SECONDS)) {
            System.err.println("FAIL timed out waiting for the FX thread");
            System.exit(1);
        }
        // Leave through System.exit: stopping the toolkit from this program
        // (no Application.launch) trips a macOS shutdown assertion.
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
}
