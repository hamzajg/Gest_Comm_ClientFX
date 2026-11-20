import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiConfig;
import com.xdev.gest_comm_clientfx.controllers.ApiDialog;
import com.xdev.gest_comm_clientfx.controllers.AssistantCtrl;
import com.xdev.gest_comm_clientfx.controllers.HomeScreenCtrl;
import com.xdev.gest_comm_clientfx.models.Article;
import com.xdev.gest_comm_clientfx.models.BonLiv;
import com.xdev.gest_comm_clientfx.models.Client;
import com.xdev.gest_comm_clientfx.models.Devis;
import com.xdev.gest_comm_clientfx.models.Facture;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Evidence-walkthrough screenshot generator.
 *
 * Renders every screen/dialog of Gest_Comm_ClientFX to PNG without
 * showing windows, against the live PyGestComm API. Not part of the
 * Maven build — compile and run manually:
 *
 *   mvn -B -q compile
 *   mvn -B -q dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt
 *   javac -cp "target/classes:$(cat /tmp/cp.txt)" \
 *       -d /tmp/shots tools/Screenshots.java
 *   java -cp "/tmp/shots:target/classes:$(cat /tmp/cp.txt)" \
 *       Screenshots [baseUrl] [user] [password] [outDir]
 *
 * Requires the Django dev server running (default http://localhost:8000)
 * and a JavaFX-capable display (macOS/Windows/Linux desktop session).
 */
public class Screenshots {

    private static final String VIEWS =
        "/com/xdev/gest_comm_clientfx/views/";

    private static ApiClient api;
    private static File outDir;

    public static void main(String[] args) {
        String baseUrl = args.length > 0 ? args[0] : "http://localhost:8000";
        String user = args.length > 1 ? args[1] : "admin";
        String pass = args.length > 2 ? args[2] : "admin";
        outDir = new File(args.length > 3 ? args[3] : "docs/walkthrough");
        outDir.mkdirs();

        ApiConfig config = ApiConfig.load();
        config.setBaseUrl(baseUrl);
        config.setUsername(user);
        config.setPassword(pass);
        config.save();
        api = new ApiClient(config);

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
        // Entities for edit-mode dialogs (fetched off the FX thread).
        List<Client> clients = api.listClients();
        List<Article> articles = api.listArticles();
        List<Devis> devis = api.listDevis();
        List<Facture> factures = api.listFactures();
        List<BonLiv> bonlivs = api.listBonLivs();
        System.out.println("data: " + clients.size() + " clients, "
            + articles.size() + " articles, " + devis.size() + " devis, "
            + factures.size() + " factures, " + bonlivs.size() + " bonlivs");

        shootHome(clients, devis, factures, bonlivs);
        shootSimple("ConfigScreen.fxml", "07-config.png", 620, 380, 800);
        shootDialog("NouveauClient.fxml", "08-nouveau-client.png",
            620, 460, 800, c -> fill(c, new String[][]{
                {"codeClientField", "C-DEMO"},
                {"nomPrenomField", "Client Démo SAS"},
                {"matFiscalField", "0000000X"},
                {"adresse1Field", "12 rue de la Démo"},
                {"tel1Field", "20123456"}}));
        shootDialog("NouvelArticle.fxml", "09-nouvel-article.png",
            620, 460, 800, c -> fill(c, new String[][]{
                {"refArticleField", "ART-DEMO"},
                {"descArticleField", "Prestation démo"},
                {"prixVenteField", "150.00"}}));
        shootDocDialog("NouveauDevis.fxml", "10-nouveau-devis.png",
            "codeDevisField", "D-DEMO", 780, 680,
            devis.isEmpty() ? null : devis.get(0));
        shootDocDialog("NouvelFacture.fxml", "11-nouvelle-facture.png",
            "codeFactureField", "F-DEMO", 780, 680,
            factures.isEmpty() ? null : factures.get(0));
        shootDocDialog("NouveauBonLiv.fxml", "12-nouveau-bonliv.png",
            "codeBonLivField", "BL-DEMO", 780, 680,
            bonlivs.isEmpty() ? null : bonlivs.get(0));
        if (!devis.isEmpty()) {
            shootEdit("NouveauDevis.fxml", "13-edit-devis.png", 760, 640,
                devis.get(0), (ctrl, root) -> { });
        }
        if (!factures.isEmpty()) {
            shootEdit("NouvelFacture.fxml", "14-edit-facture.png", 760, 640,
                factures.get(0), (ctrl, root) -> { });
        }
        if (!bonlivs.isEmpty()) {
            shootEdit("NouveauBonLiv.fxml", "15-edit-bonliv.png", 760, 640,
                bonlivs.get(0), (ctrl, root) -> { });
        }
        shootAssistant();
        System.out.println("screenshots written to " + outDir.getAbsolutePath());
    }

    // ---------- screen builders ----------

    private interface Fx { void run() throws Exception; }

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

    private static Object[] loadScene(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            Screenshots.class.getResource(VIEWS + fxml));
        Object root = loader.load();
        return new Object[]{root, loader.getController()};
    }

    private static void injectApiDialog(Object controller) {
        if (controller instanceof ApiDialog d) {
            d.setApi(api);
            d.setOnSaved(() -> { });
        }
    }

    private static void shootHome(List<Client> clients, List<Devis> devis,
                                  List<Facture> factures,
                                  List<BonLiv> bonlivs) throws Exception {
        final Object[] holder = new Object[3];
        fx(() -> {
            Object[] pair = loadScene("HomeScreen.fxml");
            holder[0] = pair[0];
            holder[1] = new Scene((Parent) pair[0], 1280, 800);
            holder[2] = pair[1];
            HomeScreenCtrl ctrl = (HomeScreenCtrl) pair[1];
            ctrl.refreshClients();
            ctrl.refreshArticles();
            ctrl.refreshDevis();
            ctrl.refreshFactures();
            ctrl.refreshBonLivs();
        });
        Thread.sleep(3500);
        // Default state: the workspace opens with Bienvenue only.
        fx(() -> savePng((Node) holder[0], new File(outDir, "01-bienvenue.png")));
        // Tabs are opened by the workspace, so drive them through the
        // controller (the old build had one static tab per panel).
        String[] methods = {"showClients", "showArticles", "showDevis",
            "showFactures", "showBonLivs"};
        String[] names = {"02-clients.png", "03-articles.png",
            "04-devis.png", "05-factures.png", "06-bonlivs.png"};
        for (int i = 0; i < methods.length; i++) {
            final String method = methods[i];
            final String fileName = names[i];
            fx(() -> ((HomeScreenCtrl) holder[2]).getClass()
                .getMethod(method).invoke(holder[2]));
            Thread.sleep(1200);
            fx(() -> savePng((Node) holder[0], new File(outDir, fileName)));
        }
        // The drawer collapsed to its rail (it expands back, never closes).
        fx(() -> ((HomeScreenCtrl) holder[2]).getClass()
            .getMethod("toggleDrawer").invoke(holder[2]));
        Thread.sleep(400);
        fx(() -> savePng((Node) holder[0], new File(outDir, "18-drawer-collapsed.png")));
        fx(() -> ((HomeScreenCtrl) holder[2]).getClass()
            .getMethod("toggleDrawer").invoke(holder[2]));
        Thread.sleep(300);
        // A form hosted in a tab, with the drawer alongside it.
        fx(() -> ((HomeScreenCtrl) holder[2]).getClass()
            .getMethod("showNewClient").invoke(holder[2]));
        Thread.sleep(1500);
        fx(() -> savePng((Node) holder[0], new File(outDir, "17-form-tab.png")));
        System.out.println("  home tabs done (" + clients.size() + "c/"
            + devis.size() + "d/" + factures.size() + "f/"
            + bonlivs.size() + "bl)");
    }

    private static void shootSimple(String fxml, String name, int w, int h,
                                    long delay) throws Exception {
        final Object[] holder = new Object[2];
        fx(() -> {
            Object[] pair = loadScene(fxml);
            holder[0] = pair[0];
            holder[1] = new Scene((Parent) pair[0], w, h);
            injectApiDialog(pair[1]);
        });
        Thread.sleep(delay);
        fx(() -> savePng((Node) holder[0], new File(outDir, name)));
    }

    private interface Filler { void fill(Node root) throws Exception; }

    private static void fill(Node root, String[][] fields)
        throws Exception {
        for (String[] f : fields) {
            Node n = root.lookup("#" + f[0]);
            if (n instanceof TextField tf) tf.setText(f[1]);
        }
    }

    private static void shootDialog(String fxml, String name, int w, int h,
                                    long delay, Filler filler)
        throws Exception {
        final Object[] holder = new Object[2];
        fx(() -> {
            Object[] pair = loadScene(fxml);
            holder[0] = pair[0];
            holder[1] = new Scene((Parent) pair[0], w, h);
            injectApiDialog(pair[1]);
            filler.fill((Node) pair[0]);
        });
        Thread.sleep(delay);
        fx(() -> savePng((Node) holder[0], new File(outDir, name)));
    }

    /** Create-mode document dialog: header fields + one article line. */
    private static void shootDocDialog(String fxml, String name,
                                       String codeId, String code,
                                       int w, int h, Object entity)
        throws Exception {
        final Object[] holder = new Object[2];
        final Object[] ctrl = new Object[1];
        fx(() -> {
            Object[] pair = loadScene(fxml);
            holder[0] = pair[0];
            holder[1] = new Scene((Parent) pair[0], w, h);
            ctrl[0] = pair[1];
            injectApiDialog(pair[1]);
            Node root = (Node) pair[0];
            setText(root, codeId, code);
            // Wait for lookup combos, then pick first article + add line.
        });
        Thread.sleep(2500);
        fx(() -> {
            Node root = (Node) holder[0];
            Object article = firstComboItem(root, "#articleCombo");
            if (article != null) {
                selectCombo(root, "#articleCombo", article);
            }
            setText(root, "#quantiteField", "2");
            setText(root, "#prixField", "100");
            setText(root, "#remiseField", "0");
            setText(root, "#tvaField", "20");
            setText(root, "#designationField", "Prestation démo");
            try {
                ctrl[0].getClass().getMethod("addLine").invoke(ctrl[0]);
            } catch (Exception e) {
                System.out.println("  addLine failed: " + e);
            }
        });
        Thread.sleep(600);
        fx(() -> savePng((Node) holder[0], new File(outDir, name)));
    }

    private interface Editor { void edit(Object ctrl, Node root)
        throws Exception; }

    private static void shootEdit(String fxml, String name, int w, int h,
                                  Object entity, Editor editor)
        throws Exception {
        final Object[] holder = new Object[2];
        fx(() -> {
            Object[] pair = loadScene(fxml);
            holder[0] = pair[0];
            holder[1] = new Scene((Parent) pair[0], w, h);
            injectApiDialog(pair[1]);
            try {
                pair[1].getClass()
                    .getMethod("setEditing", entity.getClass())
                    .invoke(pair[1], entity);
            } catch (NoSuchMethodException e) {
                // setEditing overloads take the concrete model type.
                for (java.lang.reflect.Method m
                        : pair[1].getClass().getMethods()) {
                    if (m.getName().equals("setEditing")
                        && m.getParameterCount() == 1) {
                        m.invoke(pair[1], entity);
                        break;
                    }
                }
            }
            editor.edit(pair[1], (Node) pair[0]);
        });
        Thread.sleep(2500);
        fx(() -> savePng((Node) holder[0], new File(outDir, name)));
    }

    private static void shootAssistant() throws Exception {
        final Object[] holder = new Object[2];
        final Object[] ctrl = new Object[1];
        fx(() -> {
            Object[] pair = loadScene("Assistant.fxml");
            holder[0] = pair[0];
            holder[1] = new Scene((Parent) pair[0], 680, 560);
            ctrl[0] = pair[1];
            injectApiDialog(pair[1]);
            TextField q = (TextField) ((Node) pair[0]).lookup("#questionField");
            q.setText("how many devis do we have?");
        });
        fx(() -> {
            try {
                ctrl[0].getClass().getMethod("ask").invoke(ctrl[0]);
            } catch (Exception e) {
                System.out.println("  ask failed: " + e);
            }
        });
        Thread.sleep(2000);
        fx(() -> savePng((Node) holder[0], new File(outDir, "16-assistant.png")));
    }

    // ---------- fx helpers ----------

    private static void setText(Node root, String id, String value) {
        Node n = root.lookup(id.startsWith("#") ? id : "#" + id);
        if (n instanceof TextField tf) tf.setText(value);
    }

    @SuppressWarnings("unchecked")
    private static Object firstComboItem(Node root, String id) {
        Node n = root.lookup(id);
        if (n instanceof ComboBox<?> cb && !cb.getItems().isEmpty()) {
            return cb.getItems().get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void selectCombo(Node root, String id, Object item) {
        Node n = root.lookup(id);
        if (n instanceof ComboBox cb) {
            cb.getSelectionModel().select(item);
        }
    }

    // ---------- PNG output ----------

    private static void savePng(Node root, File file) throws Exception {
        root.applyCss();
        ((javafx.scene.Parent) root).layout();
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(new Scale(2, 2));
        WritableImage img = root.snapshot(params, null);
        PixelReader reader = img.getPixelReader();
        int w = (int) img.getWidth(), h = (int) img.getHeight();
        BufferedImage buffered = new BufferedImage(
            w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                buffered.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        ImageIO.write(buffered, "png", file);
        System.out.println("  wrote " + file.getName());
    }
}
