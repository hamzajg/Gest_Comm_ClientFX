import com.xdev.gest_comm_clientfx.controllers.HomeScreenCtrl;
import com.xdev.gest_comm_clientfx.models.Client;
import com.xdev.gest_comm_clientfx.ui.Theme;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Scale;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Skin comparison capture: renders each screen twice — once with the base
 * dark palette, once with the `.root.light` palette — and writes a single
 * side-by-side PNG per screen. Used to review the light prototype before it
 * is adopted; not part of the Maven build.
 *
 *   mvn -B -q compile
 *   javac -cp "target/classes:$(cat /tmp/cp.txt)" -d /tmp/skins tools/SkinShots.java
 *   java -cp "/tmp/skins:target/classes:$(cat /tmp/cp.txt)" SkinShots <outDir>
 *
 * No backend is needed: the tables are filled with synthetic rows so the
 * captures are stable and never depend on the API being up.
 */
public class SkinShots {

    private static final String VIEWS =
        "/com/xdev/gest_comm_clientfx/views/";

    private static File outDir;

    public static void main(String[] args) {
        // Mirror StartApp: JavaFX's own strings are French here too.
        Locale.setDefault(Locale.FRANCE);
        outDir = new File(args.length > 0 ? args[0] : "docs/light-skin");
        outDir.mkdirs();
        Platform.startup(() -> new Thread(() -> {
            try {
                runAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Platform.exit();
        }).start());
    }

    private static void runAll() throws Exception {
        shootPair("01-home-clients", SkinShots::homeClients, 900);
        shootPair("02-form-tab-min", SkinShots::formTabAtMinimum, 900);
        shootPair("03-assistant", SkinShots::assistant, 700);
        shootPair("04-config", SkinShots::config, 700);
        shootPair("05-welcome", SkinShots::welcome, 500);
        shootPair("06-rail-min", SkinShots::railAtMinimum, 500);
        shootPair("07-login", SkinShots::login, 300);
        shootPair("08-login-refused", SkinShots::loginRefused, 300);
        System.out.println("skin captures written to " + outDir.getAbsolutePath());
    }

    // ---------- screens ----------

    /** A screen builder: produces a themed scene and a settle step. */
    private interface Screen {
        Scene build() throws Exception;
    }

    private interface Settle {
        void run() throws Exception;
    }

    private static Scene homeClients() throws Exception {
        FXMLLoader loader = loader("HomeScreen.fxml");
        Parent root = loader.load();
        Scene scene = new Scene(root, 1280, 800);
        ((HomeScreenCtrl) loader.getController()).showClients();
        TableView<Client> table = table(root, "#clientTable");
        table.setItems(FXCollections.observableArrayList(clients()));
        table.getSelectionModel().select(2);
        label(root, "#clientStatus").setText("6 clients chargés.");
        return scene;
    }

    private static Scene formTabAtMinimum() throws Exception {
        FXMLLoader loader = loader("HomeScreen.fxml");
        Parent root = loader.load();
        Scene scene = new Scene(root, 1024, 640);
        ((HomeScreenCtrl) loader.getController()).showNewClient();
        return scene;
    }

    private static Scene assistant() throws Exception {
        FXMLLoader loader = loader("Assistant.fxml");
        Parent root = loader.load();
        Scene scene = new Scene(root, 760, 620);
        TextArea log = (TextArea) root.lookup("#logArea");
        if (log != null) {
            log.setText("Q: combien de devis ce mois-ci ?\n"
                + "R: 12 devis ont été créés en novembre 2026 : 8 validés, "
                + "2 refusés, 2 en attente.\n\n"
                + "Q: quel client a le plus gros encours ?\n"
                + "R: Atelier Moreau SARL, avec 12 480,00 € de factures "
                + "non réglées.\n");
        }
        return scene;
    }

    /** The gate in front of the workspace, in both palettes. */
    private static Scene login() throws Exception {
        FXMLLoader loader = loader("LoginView.fxml");
        Parent root = loader.load();
        javafx.scene.control.TextField username =
            (javafx.scene.control.TextField) root.lookup("#usernameField");
        if (username != null) {
            username.setText("operateur");
        }
        return new Scene(root, 1024, 640);
    }

    private static Scene loginRefused() throws Exception {
        Scene scene = login();
        Label status = label(scene.getRoot(), "#statusLabel");
        if (status != null) {
            status.setText(
                "Nom d'utilisateur ou mot de passe incorrect.");
        }
        return scene;
    }

    private static Scene config() throws Exception {
        FXMLLoader loader = loader("ConfigScreen.fxml");
        Parent root = loader.load();
        return new Scene(root, 900, 640);
    }

    private static Scene welcome() throws Exception {
        FXMLLoader loader = loader("HomeScreen.fxml");
        Parent root = loader.load();
        return new Scene(root, 1280, 800);
    }

    private static Scene railAtMinimum() throws Exception {
        FXMLLoader loader = loader("HomeScreen.fxml");
        Parent root = loader.load();
        Scene scene = new Scene(root, 1024, 640);
        HomeScreenCtrl ctrl = (HomeScreenCtrl) loader.getController();
        ctrl.showClients();
        TableView<Client> table = table(root, "#clientTable");
        table.setItems(FXCollections.observableArrayList(clients()));
        label(root, "#clientStatus").setText("6 clients chargés.");
        ctrl.toggleDrawer();
        return scene;
    }

    // ---------- capture ----------

    /**
     * Builds one scene, waits for its asynchronous loading to fail quietly
     * (there is no backend here), then snapshots the same scene in both
     * skins and writes them side by side.
     */
    private static void shootPair(String name, Screen screen,
                                  long settleMs) throws Exception {
        final Scene[] scene = new Scene[1];
        fx(() -> {
            scene[0] = screen.build();
            Theme.install(scene[0], Theme.Skin.DARK);
        });
        // The list panels kick off an API refresh; let it fail and repaint,
        // then restore the synthetic rows the capture is about.
        Thread.sleep(settleMs);
        fx(() -> {
            Parent root = scene[0].getRoot();
            @SuppressWarnings("rawtypes")
            TableView table = (TableView) root.lookup("#clientTable");
            if (table != null) {
                table.setItems(FXCollections.observableArrayList(clients()));
                // Re-select: replacing the items clears the selection, and
                // the selected row is the surface this comparison is about.
                table.getSelectionModel().select(2);
                label(root, "#clientStatus").setText("6 clients chargés.");
            }
        });

        BufferedImage dark = snapshot(() -> Theme.applySkin(scene[0], Theme.Skin.DARK),
            scene[0].getRoot());
        BufferedImage light = snapshot(() -> Theme.applySkin(scene[0], Theme.Skin.LIGHT),
            scene[0].getRoot());
        writeSideBySide(new File(outDir, name + ".png"), dark, light);
    }

    private static BufferedImage snapshot(Runnable before, Parent root)
        throws Exception {
        final BufferedImage[] out = new BufferedImage[1];
        fx(() -> {
            before.run();
            out[0] = png(root);
        });
        return out[0];
    }

    private static void writeSideBySide(File file, BufferedImage dark,
                                        BufferedImage light) throws Exception {
        int gap = 28;
        int header = 48;
        int width = dark.getWidth() + gap + light.getWidth();
        int height = header + Math.max(dark.getHeight(), light.getHeight());
        BufferedImage out = new BufferedImage(width, height,
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(0xf3f4f6));
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(0x8d9aa5));
        g.fillRect(0, header - 1, width, 1);
        g.setFont(new Font("Helvetica Neue", Font.BOLD, 17));
        g.setColor(new Color(0x101519));
        g.drawString("Sombre — palette actuelle", 18, 30);
        g.drawString("Clair — prototype", dark.getWidth() + gap + 18, 30);
        g.drawImage(dark, 0, header, null);
        g.drawImage(light, dark.getWidth() + gap, header, null);
        g.dispose();
        ImageIO.write(out, "png", file);
        System.out.println("  wrote " + file.getName());
    }

    // ---------- helpers ----------

    private static FXMLLoader loader(String fxml) {
        return new FXMLLoader(SkinShots.class.getResource(VIEWS + fxml));
    }

    @SuppressWarnings("unchecked")
    private static TableView<Client> table(Node root, String id) {
        return (TableView<Client>) root.lookup(id);
    }

    private static Label label(Node root, String id) {
        return (Label) root.lookup(id);
    }

    /** Synthetic rows: enough variety to judge zebra, density and selection. */
    private static List<Client> clients() {
        String[][] rows = {
            {"C-1001", "Atelier Moreau SARL", "0012345A", "20112233", ""},
            {"C-1002", "Boulangerie Ferrand", "0012346B", "71223344", ""},
            {"C-1003", "Cabinet Lemoine & Fils", "0012347C", "98223355", ""},
            {"C-1004", "Delta Logistique SA", "0012348D", "55443322", "55443323"},
            {"C-1005", "Ébénisterie Santini", "0012349E", "22334455", ""},
            {"C-1006", "Fonderie Guérin", "0012350F", "44778899", ""}
        };
        List<Client> list = new ArrayList<>();
        for (String[] r : rows) {
            Client c = new Client();
            c.setCodeClient(r[0]);
            c.setNomPrenomClient(r[1]);
            c.setMatFiscalClient(r[2]);
            c.setNumTelClient1(r[3].isEmpty() ? null : Integer.valueOf(r[3]));
            c.setNumTelClient2(r[4].isEmpty() ? null : Integer.valueOf(r[4]));
            list.add(c);
        }
        return list;
    }

    private static BufferedImage png(Parent root) {
        root.applyCss();
        root.layout();
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(new Scale(2, 2));
        WritableImage img = root.snapshot(params, null);
        PixelReader reader = img.getPixelReader();
        int w = (int) img.getWidth(), h = (int) img.getHeight();
        BufferedImage buffered = new BufferedImage(w, h,
            BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                buffered.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        return buffered;
    }

    private static void fx(Fx r) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Exception[] err = new Exception[1];
        Platform.runLater(() -> {
            try { r.run(); } catch (Exception e) { err[0] = e; }
            latch.countDown();
        });
        latch.await(30, TimeUnit.SECONDS);
        if (err[0] != null) throw err[0];
    }

    private interface Fx { void run() throws Exception; }
}
