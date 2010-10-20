/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.symcomp.webwupsi.rest;

import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.codehaus.jettison.json.JSONStringer;
import org.symcomp.scscp.SCSCPClient;
import org.symcomp.wupsi.Wupsifer;

/**
 *
 * @author hornp
 */
@Path("/systems")
public class SystemsResource {

    @GET
    @Produces("application/json")
    public String index() {
        Wupsifer w = Wupsifer.getInstance();
        Map<String, SCSCPClient> systems = w.getClients();
        JSONStringer js = new JSONStringer();
        try {
            // Begin array
            js.array();
            for (String k : systems.keySet()) {
                SCSCPClient sys = systems.get(k);
                js.object();
                js.key("id").value(k);
                js.key("name").value(sys.getServiceName());
                js.key("version").value(sys.getServiceVersion());
                js.key("host").value(sys.getSCSCPUri());
                js.key("port").value(sys.getSCSCPPort());
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
    public String show(@PathParam("id") String idd) {
        String id = java.net.URLDecoder.decode(idd);
        SCSCPClient sys = Wupsifer.getInstance().getClients().get(id);
        JSONStringer js = new JSONStringer();
        try {
            js.object();
            js.key("id").value(id);
            js.key("name").value(sys.getServiceName());
            js.key("version").value(sys.getServiceVersion());
            js.key("host").value(sys.getSCSCPUri());
            js.key("port").value(sys.getSCSCPPort());
            js.key("computations").value(sys.getComputations().size());
            js.endObject();
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return js.toString();
    }

    @POST
    @Produces("application/json")
    public String create(@Context UriInfo ui) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        String host = queryParams.getFirst("host");
        String port = queryParams.getFirst("port");
        String id = queryParams.getFirst("id");
//        System.out.println(" ------> "+host+"  "+port+"  "+id);
        Wupsifer w = Wupsifer.getInstance();
        if (w.connect(host + ":" + port, id)) {
            return "OK";
        }
        return "FAILURE";
    }

}
