package uk.co.prenderj.trailsrv.handler;

import javax.servlet.http.HttpServlet;

import uk.co.prenderj.trailsrv.TrailServer;

public abstract class BaseServlet extends HttpServlet {
    private TrailServer srv;
    
    public BaseServlet(TrailServer srv) {
        this.srv = srv;
    }
    
    public TrailServer getServer() {
        return srv;
    }
}
