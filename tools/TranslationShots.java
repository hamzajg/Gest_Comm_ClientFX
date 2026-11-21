import com.xdev.gest_comm_clientfx.controllers.HomeScreenCtrl;
import com.xdev.gest_comm_clientfx.i18n.Messages;
import com.xdev.gest_comm_clientfx.models.Client;
import com.xdev.gest_comm_clientfx.ui.AppPreferences;
import com.xdev.gest_comm_clientfx.ui.Theme;
import com.xdev.gest_comm_clientfx.ui.Views;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Scale;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * US7 capture + assertion: the five record tables and the record forms
 * follow the language chosen on the workstation.
 *
 *   mvn -B -q compile
 *   javac -cp "target/classes:$(cat /tmp/gestcomm-cp.txt)" -d /tmp/tr tools/TranslationShots.java
 *   java -cp "/tmp/tr:target/classes:$(cat /tmp/gestcomm-cp.txt)" TranslationShots <outDir>
 *
 * No backend is needed: the tables are filled with synthetic rows. The
 * language preference is restored before exiting.
 */
public class TranslationShots {

    private static File outDir;
    private static String previousLanguage;

    public static void main(String[] args) {
        Locale.setDefault(Locale.FRANCE);
        outDir = new File(args.length > 0 ? args[0] : "docs/translation");
        outDir.mkdirs();
        previousLanguage = AppPreferences.load().language();

        Platform.startup(() -> new Thread(() -> {
            int status = 1;
            try {
                runAll();
                status = 0;
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                AppPreferences.load().setLanguage(previousLanguage);
            }
            System.exit(status);
        }).start());
    }

    private static void runAll() throws Exception {
        // --- record tables, both languages -------------------------------
        Scene englishTables = tableScene("en");
        assertCol(englishTables, 2, "Tax ID");
        assertCol(englishTables, 4, "Phone 2");
        shot(englishTables, "01-tables-en.png");
        check("English tables", "6 clients loaded.",
            label(englishTables.getRoot(), "#clientStatus").getText());

        Scene frenchTables = tableScene("fr");
        assertCol(frenchTables, 2, "Mat. fiscale");
        assertCol(frenchTables, 4, "Tél. 2");
        shot(frenchTables, "02-tables-fr.png");
        check("French tables", "6 clients chargés.",
            label(frenchTables.getRoot(), "#clientStatus").getText());

        // --- record forms, both languages --------------------------------
        Scene englishForm = formScene("en");
        layout(englishForm);
        check("English quote form", "New quote",
            firstText(englishForm, ".form-title"));
        // String.format follows the JVM locale; the label and the assertion
        // must agree on the separator.
        String zero = String.format("%.2f", 0.0);
        check("English totals", Messages.get(Locale.ENGLISH, "form.totals",
            zero, zero, zero),
            label(englishForm.getRoot(), "#totalsLabel").getText());
        shot(englishForm, "03-form-en.png");

        Scene frenchForm = formScene("fr");
        layout(frenchForm);
        check("French quote form", "Nouveau devis",
            firstText(frenchForm, ".form-title"));
        check("French totals", Messages.get(Locale.FRENCH, "form.totals",
            zero, zero, zero),
            label(frenchForm.getRoot(), "#totalsLabel").getText());
        shot(frenchForm, "04-form-fr.png");

        System.out.println("PASS — record tables and forms follow the language");
    }

    /** Workspace with the client list open, in the requested language. */
    private static Scene tableScene(String language) throws Exception {
        AppPreferences.load().setLanguage(language);
        final Scene[] out = new Scene[1];
        fx(() -> {
            FXMLLoader loader = Views.loader(TranslationShots.class,
                "HomeScreen.fxml");
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 800);
            Theme.install(scene, Theme.Skin.DARK);
            ((HomeScreenCtrl) loader.getController()).showClients();
            out[0] = scene;
        });
        // Let the (backend-less) refresh fail and repaint, then pose rows.
        Thread.sleep(800);
        fx(() -> {
            Scene scene = out[0];
            @SuppressWarnings("rawtypes")
            TableView table = (TableView) scene.getRoot()
                .lookup("#clientTable");
            table.setItems(FXCollections.observableArrayList(clients()));
            table.getSelectionModel().select(2);
            label(scene.getRoot(), "#clientStatus")
                .setText(Messages.get("record.loaded.clients", 6));
        });
        return out[0];
    }

    /** New quote form: the richest record form (header, lines, totals). */
    private static Scene formScene(String language) throws Exception {
        AppPreferences.load().setLanguage(language);
        final Scene[] out = new Scene[1];
        fx(() -> {
            FXMLLoader loader = Views.loader(TranslationShots.class,
                "HomeScreen.fxml");
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 800);
            Theme.install(scene, Theme.Skin.DARK);
            HomeScreenCtrl ctrl = (HomeScreenCtrl) loader.getController();
            ctrl.showNewDevis();
            out[0] = scene;
        });
        Thread.sleep(400);
        fx(() -> {
            out[0].getRoot().applyCss();
            out[0].getRoot().layout();
            Label status = label(out[0].getRoot(), "#statusLabel");
            if (status != null) {
                status.setText("");
            }
        });
        return out[0];
    }

    // ---------- assertions / capture ----------

    private static void layout(Scene scene) throws Exception {
        fx(() -> {
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        });
    }

    private static void assertCol(Scene scene, int index, String expected) {
        @SuppressWarnings("rawtypes")
        TableView table = (TableView) scene.getRoot().lookup("#clientTable");
        TableColumn<?, ?> column =
            (TableColumn<?, ?>) table.getColumns().get(index);
        check("column " + index, expected, column.getText());
    }

    private static String firstText(Scene scene, String selector) {
        for (Node node : scene.getRoot().lookupAll(selector)) {
            if (node instanceof Label label) {
                return label.getText();
            }
        }
        return null;
    }

    private static void check(String what, String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(what + ": expected \"" + expected
                + "\" but was \"" + actual + "\"");
        }
        System.out.println("ok " + what + " -> " + actual);
    }

    private static void shot(Scene scene, String name) throws Exception {
        final BufferedImage[] image = new BufferedImage[1];
        fx(() -> image[0] = png(scene.getRoot()));
        ImageIO.write(image[0], "png", new File(outDir, name));
        System.out.println("  wrote " + name);
    }

    private static BufferedImage png(Parent root) throws Exception {
        root.applyCss();
        root.layout();
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(new Scale(2, 2));
        WritableImage image = root.snapshot(params, null);
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        BufferedImage out = new BufferedImage(w, h,
            BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        return out;
    }

    private static Label label(Node root, String id) {
        return (Label) root.lookup(id);
    }

    private interface FxAction {
        void run() throws Exception;
    }

    private static void fx(FxAction action) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        final Throwable[] failure = new Throwable[1];
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                failure[0] = t;
            } finally {
                done.countDown();
            }
        });
        if (!done.await(20, TimeUnit.SECONDS)) {
            throw new IllegalStateException("FX action timed out");
        }
        if (failure[0] != null) {
            throw new IllegalStateException(failure[0]);
        }
    }

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
}
