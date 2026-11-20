import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiConfig;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.models.UserProfile;

/**
 * End-to-end smoke for the login wave: signs in against a live PyGestComm,
 * prints the identity/role, refuses a wrong password, and proves the password
 * change endpoint with a round trip back to the original password.
 *
 * Not part of the Maven build. Run with an isolated Preferences root so it
 * never touches the desktop app's saved session:
 *
 *   javac -cp "target/classes:$(cat /tmp/cp.txt)" -d /tmp/smoke tools/LoginSmoke.java
 *   java -Djava.util.prefs.userRoot=/tmp/prefs-smoke \
 *        -cp "/tmp/smoke:target/classes:$(cat /tmp/cp.txt)" \
 *        LoginSmoke <baseUrl> <operator> <operatorPass> <admin> <adminPass>
 */
public class LoginSmoke {

    public static void main(String[] args) throws Exception {
        String baseUrl = args[0];
        String operator = args[1];
        String operatorPass = args[2];
        String admin = args[3];
        String adminPass = args[4];

        UserProfile op = new ApiClient(ApiConfig.load())
            .signIn(baseUrl, operator, operatorPass);
        System.out.println("OK  operator sign-in: username=" + op.getUsername()
            + " role=" + op.getRole() + " display=" + op.getDisplayName());

        try {
            new ApiClient(ApiConfig.load())
                .signIn(baseUrl, operator, "definitely-wrong");
            System.out.println("FAIL wrong password was accepted");
            System.exit(1);
        } catch (ApiException e) {
            System.out.println("OK  wrong password refused: HTTP "
                + e.getStatusCode() + " — " + e.getDetail());
        }

        UserProfile boss = new ApiClient(ApiConfig.load())
            .signIn(baseUrl, admin, adminPass);
        System.out.println("OK  admin sign-in: username=" + boss.getUsername()
            + " role=" + boss.getRole() + " admin=" + boss.isAdmin());

        // Password round trip through POST /api/me/password.
        String changed = operatorPass + "-rotated";
        ApiClient session = new ApiClient(ApiConfig.load());
        session.signIn(baseUrl, operator, operatorPass);
        session.send("POST", "/api/me/password",
            java.util.Map.of("currentPassword", operatorPass,
                             "newPassword", changed));
        System.out.println("OK  password changed");
        UserProfile after = new ApiClient(ApiConfig.load())
            .signIn(baseUrl, operator, changed);
        System.out.println("OK  sign-in with the new password: role="
            + after.getRole());

        ApiClient back = new ApiClient(ApiConfig.load());
        back.signIn(baseUrl, operator, changed);
        back.send("POST", "/api/me/password",
            java.util.Map.of("currentPassword", changed,
                             "newPassword", operatorPass));
        System.out.println("OK  password restored");
    }
}
