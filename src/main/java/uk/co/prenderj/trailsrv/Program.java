package uk.co.prenderj.trailsrv;

import uk.co.prenderj.trailsrv.handler.CommentAdder;
import uk.co.prenderj.trailsrv.handler.CommentLoader;

public class Program {
    private static Server srv;
    
    /**
     * Main execution point.
     * @param args command line arguments
     * @throws Exception if any exception is uncaught
     */
    public static void main(String[] args) throws Exception {
        srv = new Server(args.length > 0 ? args[0] : "cfg/server.properties"); // Override the properties path by providing an argument
        srv.createContext(new CommentAdder(srv));
        srv.createContext(new CommentLoader(srv));
        srv.start();
    }
    
    /**
     * Stops the running server. Blocks until terminated.
     */
    public static void stop() {
        if (srv != null) {
            srv.stop();
            srv = null;
        }
    }
}
