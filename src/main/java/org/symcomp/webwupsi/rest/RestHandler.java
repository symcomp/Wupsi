/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.symcomp.webwupsi.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;



/**
 *
 * @author hornp
 */
@Path("/helloworld")
public class RestHandler {

    @GET
    @Produces("text/plain")
    public String getClichedMessage() {
        //throw new RuntimeException("Called!");
        return "GET";
    }

    @POST
    @Produces("text/plain")
    public String getClichedMessage2() {
        //throw new RuntimeException("Called!");
        return "POST";
    }
    
    @PUT
    @Produces("text/plain")
    public String getClichedMessage3() {
        //throw new RuntimeException("Called!");
        return "PUT";
    }

    @DELETE
    @Produces("text/plain")
    public String getClichedMessage4() {
        //throw new RuntimeException("Called!");
        return "DELETE";
    }


}
