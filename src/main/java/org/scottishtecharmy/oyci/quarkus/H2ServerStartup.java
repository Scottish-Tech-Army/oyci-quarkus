package org.scottishtecharmy.oyci.quarkus;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.h2.tools.Server;
import org.jboss.logging.Logger;

/**
 * Starts an embedded H2 TCP server + Web Console alongside the Quarkus app.
 *
 * TCP Server  → lets external JDBC clients connect while the app is running
 *               JDBC URL: jdbc:h2:tcp://localhost:9092/./target/oyci-db
 *
 * Web Console → browser UI available at http://localhost:8082
 *               JDBC URL to enter in the UI: jdbc:h2:tcp://localhost:9092/./target/oyci-db
 *               User: sa  /  Password: sa
 */
@ApplicationScoped
public class H2ServerStartup {

    private static final Logger LOG = Logger.getLogger(H2ServerStartup.class);

    private Server tcpServer;
    private Server webServer;

    void onStart(@Observes StartupEvent ev) {
        try {
            // Start TCP server so other processes can connect to the live DB
            tcpServer = Server.createTcpServer(
                    "-tcpPort", "9092",
                    "-tcpAllowOthers",
                    "-ifNotExists"
            ).start();
            LOG.info("H2 TCP server started on port 9092");

            // Start Web Console
            webServer = Server.createWebServer(
                    "-webPort", "8082",
                    "-webAllowOthers"
            ).start();
            LOG.info("H2 Web Console started → http://localhost:8082");
            LOG.info("Connect with JDBC URL: jdbc:h2:tcp://localhost:9092/./target/oyci-db  user=sa  password=sa");

        } catch (Exception e) {
            LOG.error("Failed to start H2 server", e);
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        if (webServer != null) {
            webServer.stop();
            LOG.info("H2 Web Console stopped");
        }
        if (tcpServer != null) {
            tcpServer.stop();
            LOG.info("H2 TCP server stopped");
        }
    }
}

