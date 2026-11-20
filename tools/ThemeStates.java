import com.xdev.gest_comm_clientfx.controllers.HomeScreenCtrl;
import com.xdev.gest_comm_clientfx.ui.Theme;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Scale;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Theme state probe: renders the shell at the 1024x640 floor and forces the
 * hover / pressed / focused / selected pseudo-classes JavaFX sets on real
 * pointer and keyboard input, so the CSS states are visible without a driver.
 *
 * Not part of the Maven build:
 *   javac -cp "target/classes:$(cat /tmp/cp.txt)" -d /tmp/states tools/ThemeStates.java
 *   java -cp "/tmp/states:target/classes:$(cat /tmp/cp.txt)" ThemeStates <outDir>
 */
public class ThemeStates {

    private static final String VIEWS =
        "/com/xdev/gest_comm_clientfx/views/";
    private static final PseudoClass HOVER = PseudoClass.getPseudoClass("hover");
    private static final PseudoClass PRESSED = PseudoClass.getPseudoClass("pressed");
    private static final PseudoClass FOCUSED = PseudoClass.getPseudoClass("focused");

    private static File outDir;

    public static void main(String[] args) {
        // Mirror StartApp: JavaFX's own strings must be French here too.
        Locale.setDefault(Locale.FRANCE);
        outDir = new File(args.length > 0 ? args[0] : "docs/walkthrough");
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
        final Object[] h = new Object[3];
        fx(() -> {
            Object[] pair = load("HomeScreen.fxml");
            h[0] = pair[0];
            h[1] = scene((Parent) pair[0], 1024, 640);
            h[2] = pair[1];
            ((HomeScreenCtrl) pair[1]).showClients();
        });
        Thread.sleep(1500);

        // 1. The shell at the minimum window size.
        shoot(h[0], "31-min-shell.png");

        // 2. Tab hover on a non-selected tab.
        fx(() -> {
            TabPane tabs = (TabPane) ((Node) h[0]).lookup(".tab-pane");
            List<Node> headers = List.copyOf(tabs.lookupAll(".tab"));
            if (headers.size() > 1) {
                Node tab = headers.get(0);
                layout((Node) h[0]);
                System.out.println("  tab rest  bg=" + bg(tab));
                pseudo(tab, HOVER, true);
                layout((Node) h[0]);
                System.out.println("  tab hover bg=" + bg(tab));
            }
        });
        shoot(h[0], "32-tab-hover.png");
        fx(() -> {
            TabPane tabs = (TabPane) ((Node) h[0]).lookup(".tab-pane");
            tabs.lookupAll(".tab").forEach(n -> pseudo(n, HOVER, false));
        });

        // 3/4. Drawer action hover and pressed.
        fx(() -> {
            Node action = ((Node) h[0]).lookup(".drawer-action");
            pseudo(action, HOVER, true);
            layout((Node) h[0]);
        });
        shoot(h[0], "33-drawer-hover.png");
        fx(() -> {
            Node action = ((Node) h[0]).lookup(".drawer-action");
            pseudo(action, HOVER, false);
            pseudo(action, PRESSED, true);
            layout((Node) h[0]);
        });
        shoot(h[0], "34-drawer-pressed.png");
        fx(() -> System.out.println("  drawer pressed bg="
            + bg(((Node) h[0]).lookup(".drawer-action"))));
        fx(() -> pseudo(((Node) h[0]).lookup(".drawer-action"), PRESSED, false));

        // 5. Search field with focus.
        fx(() -> {
            Node field = ((Node) h[0]).lookup(".search-field");
            pseudo(field, FOCUSED, true);
            layout((Node) h[0]);
        });
        shoot(h[0], "35-search-focus.png");
        fx(() -> pseudo(((Node) h[0]).lookup(".search-field"), FOCUSED, false));

        // 6. Selected table row.
        fx(() -> {
            TableView<?> table = (TableView<?>) ((Node) h[0]).lookup("#clientTable");
            table.getSelectionModel().select(1);
            layout((Node) h[0]);
        });
        shoot(h[0], "36-row-selected.png");

        // 6b. Focus ring on a drawer button (keyboard tab-through).
        fx(() -> {
            Node action = ((Node) h[0]).lookupAll(".drawer-action").iterator().next();
            pseudo(action, FOCUSED, true);
            layout((Node) h[0]);
        });
        shoot(h[0], "40-drawer-focus.png");
        fx(() -> ((Node) h[0]).lookupAll(".drawer-action")
            .forEach(n -> pseudo(n, FOCUSED, false)));

        // 6c. Rail at the minimum window size.
        fx(() -> ((HomeScreenCtrl) h[2]).getClass()
            .getMethod("toggleDrawer").invoke(h[2]));
        Thread.sleep(300);
        shoot(h[0], "41-rail-min.png");
        fx(() -> ((HomeScreenCtrl) h[2]).getClass()
            .getMethod("toggleDrawer").invoke(h[2]));

        // 6d. A real form hosted as a tab at 1024x640 (clipping check).
        fx(() -> ((HomeScreenCtrl) h[2]).getClass()
            .getMethod("showNewClient").invoke(h[2]));
        Thread.sleep(600);
        shoot(h[0], "42-form-tab-min.png");

        // 6e. Empty table: the placeholder must read in French. Select the
        // Clients tab first — opening a form leaves the form tab active.
        fx(() -> ((HomeScreenCtrl) h[2]).getClass()
            .getMethod("showClients").invoke(h[2]));
        Thread.sleep(600);
        // Filter to a term nothing matches: a real empty list, real status.
        fx(() -> {
            TextField search = (TextField) ((Node) h[0]).lookup("#searchField");
            search.setText("zzz-aucun-resultat");
            invokeSearch(h[2]);
            layout((Node) h[0]);
        });
        Thread.sleep(400);
        shoot(h[0], "43-empty-table-fr.png");
        fx(() -> {
            ((TextField) ((Node) h[0]).lookup("#searchField")).setText("");
            invokeSearch(h[2]);
        });

        // 6f. A form at a short height must scroll, not clip its buttons.
        shootShortForm("NouveauClient.fxml", "44-form-scroll-short.png");

        // 7/8. The tallest forms inside a tab-sized area at the floor width.
        shootForm("NouveauClient.fxml", "37-form-min-client.png");
        shootForm("NouvelArticle.fxml", "38-form-min-article.png");
        shootForm("NouveauDevis.fxml", "39-form-min-devis.png");

        System.out.println("theme states written to " + outDir.getAbsolutePath());
    }

    /** A form in the tab scroller at a height too short to fit it. */
    private static void shootShortForm(String fxml, String name) throws Exception {
        final Object[] holder = new Object[1];
        fx(() -> {
            Object[] pair = load(fxml);
            javafx.scene.control.ScrollPane scroller =
                new javafx.scene.control.ScrollPane((Parent) pair[0]);
            scroller.setFitToWidth(true);
            scroller.getStyleClass().add("tab-scroll");
            holder[0] = scroller;
            scene(scroller, 1000, 300);
        });
        Thread.sleep(700);
        shoot(holder[0], name);
    }

    /** A form rendered in the space a tab gives it at 1024x640. */
    private static void shootForm(String fxml, String name) throws Exception {
        final Object[] holder = new Object[1];
        fx(() -> {
            Object[] pair = load(fxml);
            holder[0] = pair[0];
            scene((Parent) pair[0], 1000, 520);
        });
        Thread.sleep(900);
        shoot(holder[0], name);
    }

    private static void shoot(Object root, String name) throws Exception {
        fx(() -> savePng((Node) root, new File(outDir, name)));
    }

    private static Object[] load(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(ThemeStates.class.getResource(VIEWS + fxml));
        Object root = loader.load();
        return new Object[]{root, loader.getController()};
    }

    private static Scene scene(Parent root, double w, double h) {
        Scene scene = new Scene(root, w, h);
        Theme.install(scene);
        return scene;
    }

    /** applySearch is private @FXML wiring; the probe drives it directly. */
    private static void invokeSearch(Object ctrl) {
        try {
            java.lang.reflect.Method m = ctrl.getClass()
                .getDeclaredMethod("applySearch");
            m.setAccessible(true);
            m.invoke(ctrl);
        } catch (Exception e) {
            System.out.println("  applySearch failed: " + e);
        }
    }

    private static String bg(Node n) {
        if (!(n instanceof javafx.scene.layout.Region r)
            || r.getBackground() == null
            || r.getBackground().getFills().isEmpty()) {
            return "none";
        }
        return String.valueOf(r.getBackground().getFills().get(0).getFill());
    }

    private static void pseudo(Node node, PseudoClass pc, boolean on) {
        if (node != null) {
            node.pseudoClassStateChanged(pc, on);
        }
    }

    private static void layout(Node root) {
        root.applyCss();
        ((Parent) root).layout();
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

    private static void savePng(Node root, File file) throws Exception {
        layout(root);
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(new Scale(2, 2));
        WritableImage img = root.snapshot(params, null);
        PixelReader reader = img.getPixelReader();
        int w = (int) img.getWidth(), h = (int) img.getHeight();
        BufferedImage buffered = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                buffered.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        ImageIO.write(buffered, "png", file);
        System.out.println("  wrote " + file.getName());
    }
}
