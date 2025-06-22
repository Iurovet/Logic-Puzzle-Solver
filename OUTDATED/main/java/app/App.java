package app;

import io.javalin.Javalin;
import io.javalin.core.util.RouteOverviewPlugin;


/**
 * Main Application Class.
 * <p>
 * Running this class as regular java application will start the 
 * Javalin HTTP Server and our web application.
 *
 * @author Timothy Wiley, 2023. email: timothy.wiley@rmit.edu.au
 * @author Santha Sumanasekara, 2021. email: santha.sumanasekara@rmit.edu.au
 */
public class App {
    public static final int         JAVALIN_PORT    = 7001;
    public static final String      CSS_DIR         = "css/";
    // public static final String      IMAGES_DIR      = "images/";

    public static void main(String[] args) {        
        Javalin app = Javalin.create(config -> {
            config.registerPlugin(new RouteOverviewPlugin("/help/routes"));
            config.addStaticFiles(CSS_DIR);
            // config.addStaticFiles(IMAGES_DIR);
        }).start(JAVALIN_PORT);

        configureRoutes(app);
    }

    public static void configureRoutes(Javalin app) {
        app.get(PageIndex.URL, new PageIndex());
        app.post(PageIndex.URL, new PageIndex());
    }
}