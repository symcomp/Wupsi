/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.symcomp.webwupsi.rest;

import java.text.DateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.codehaus.jettison.json.JSONStringer;
import org.symcomp.openmath.OpenMathBase;
import org.symcomp.scscp.Computation;
import org.symcomp.scscp.SCSCPClient;
import org.symcomp.wupsi.Wupsifer;

/**
 *
 * @author hornp
 */
@Path("/systems/{sid}/computations")
public class ComputationsResource {

    @GET
    @Produces("application/json")
    public String index(@PathParam("sid") String systemIdd) {
        String systemId = java.net.URLDecoder.decode(systemIdd);
        Wupsifer w = Wupsifer.getInstance();
        SCSCPClient client = w.getClients().get(systemId);
        if (null==client) return "[]";
        List<Computation> cc = client.getComputations();
        DateFormat df = DateFormat.getInstance();
        JSONStringer js = new JSONStringer();
        try {
            // Begin array
            js.array();
            for (Computation c : cc) {
                js.object();
                js.key("id").value(c.getToken());
                js.key("startedAt").value(df.format(c.getStartedAt()));
                js.key("finishedAt").value(df.format(c.getStartedAt()));
                js.endObject();
            }
            js.endArray();
            // end array
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return js.toString();
    }

    @GET
    @Produces("application/json")
    @Path("/{id}")
    public String show(@PathParam("sid") String systemIdd, @PathParam("id") String idd) {
        String systemId = java.net.URLDecoder.decode(systemIdd);
        String id = java.net.URLDecoder.decode(idd);
        SCSCPClient sys = Wupsifer.getInstance().getClients().get(systemId);
        Computation c = sys.getComputation(id);
//        System.out.println(" ----> " + c.toString() + " " + id + " " + systemId);
        DateFormat df = DateFormat.getInstance();
        JSONStringer js = new JSONStringer();
        try {
            js.object();
            js.key("id").value(c.getToken());
            js.key("startedAt").value(df.format(c.getStartedAt()));
            js.key("finishedAt").value(df.format(c.getStartedAt()));
            js.key("commandL").value(c.getRequest().toLatex());
            js.key("commandP").value(c.getRequest().toPopcorn());
            js.key("commandX").value(c.getRequest().toXml());
            js.key("resultL").value(c.getResult().toLatex());
            js.key("resultP").value(c.getResult().toPopcorn());
            js.key("resultX").value(c.getResult().toXml());
            js.endObject();
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return js.toString();
    }

    @POST
    @Produces("application/json")
    public String create(@PathParam("sid") String systemIdd, @Context UriInfo ui) {
        String systemId = java.net.URLDecoder.decode(systemIdd);
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        String command = queryParams.getFirst("command");
        SCSCPClient sys = Wupsifer.getInstance().getClients().get(systemId);
        Wupsifer w = Wupsifer.getInstance();
        try {
            w.compute(sys, OpenMathBase.parse(command));
        } catch (Exception ignored) {
            return("Failure");
        }
        return "OK";
    }

}
