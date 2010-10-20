//---------------------------------------------------------------------------
//  Copyright 2006-2009
//    Dan Roozemond, d.a.roozemond@tue.nl, (TU Eindhoven, Netherlands)
//    Peter Horn, horn@math.uni-kassel.de (University Kassel, Germany)
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//---------------------------------------------------------------------------

package org.symcomp.webwupsi;

import org.mortbay.jetty.Request;
import org.symcomp.wupsi.Wupsifer;
import org.symcomp.scscp.SCSCPClient;
import org.symcomp.openmath.OpenMathBase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.http.HttpServlet;

/**
 * This is the Servlet that is responsible for handling the 'compute' action
 */
public class Wupslet extends HttpServlet {

    private final static String HOME = "/capp/index.html";

    Wupsifer w;

    public Wupslet() {
        w = Wupsifer.getInstance();
    }

    public Wupslet(Wupsifer w) {
        this.w = w;
    }

    /**
     * Entry point of all the handled actions
     * @param target
     * @param request
     * @param response
     * @param dispatch
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        System.out.println("REQUEST path: "+path);
        String error = "";
        String res = null;
        
        if (path.equals("/") || path.equals("/index") || path.equals("/index.html")) {
            response.sendRedirect(HOME);
            return;
        }

        if (path.equals("/compute")) {
            String systemId = request.getParameter("systemId");
            if (null == systemId) { error += "ERROR. Must specify \"systemId\". "; }
            SCSCPClient client = w.getClients().get(systemId);
            if (null == client) { error += "ERROR. No system with systemId '"+systemId+"' found. "; }
            String command = request.getParameter("command");
            if (null == command) { error += "ERROR. Must specify 'command'. "; }
            String outputFormat = request.getParameter("outputFormat");
            if (null == outputFormat) { outputFormat = "xml"; }
            OpenMathBase omb = null;
            try {
                omb = OpenMathBase.parse(command);
            } catch(Exception e) {
                error += "ERROR. Command could not be parsed: "+e.getMessage();
            }
            if (error.length() == 0) {
                OpenMathBase result = null;
                try {
                    String token = client.compute(omb);
                    while (!client.resultAvailable(token)) { try { Thread.sleep(80); } catch(Exception e) {} }
                    result = client.getResult(token);
                } catch (Exception oops) {
                    error = "ERROR. Parsing or Computing failed.";
                }
                if (error.length() == 0) {
                    if (outputFormat.equalsIgnoreCase("ALL")) {
                        res = "POPCORN: "+result.toPopcorn()+"\n\nLATEX: "+result.toLatex()+"\n\nOPENMATH: "+result.toXml()+"\n\n";
                    } else if (outputFormat.equalsIgnoreCase("XML")) {
                        res = result.toXml();
                    } else if (outputFormat.equalsIgnoreCase("POPCORN")) {
                        res = result.toPopcorn();
                    } else if (outputFormat.equalsIgnoreCase("LATEX")) {
                        res = result.toLatex();
                    } else {
                        error = "Illegal output format: "+outputFormat;
                    }
                }
            }
        } else if (path.equals("/print")) {
            String command = request.getParameter("command");
            if (null == command) { error += "ERROR. Must specify 'command'. "; }
            String outputFormat = request.getParameter("outputFormat");
            if (null == outputFormat) { outputFormat = "xml"; }
            OpenMathBase omb = null;
            try {
                omb = OpenMathBase.parse(command);
            } catch(Exception e) {
                error += "ERROR. Command could not be parsed: "+e.getMessage();
            }
            if (error.length() == 0) {
                OpenMathBase result = omb;
                if (error.length() == 0) {
                    if (outputFormat.equalsIgnoreCase("ALL")) {
                        res = "POPCORN: "+result.toPopcorn()+"\n\nLATEX: "+result.toLatex()+"\n\nOPENMATH: "+result.toXml()+"\n\n";
                    } else if (outputFormat.equalsIgnoreCase("XML")) {
                        res = result.toXml();
                    } else if (outputFormat.equalsIgnoreCase("POPCORN")) {
                        res = result.toPopcorn();
                    } else if (outputFormat.equalsIgnoreCase("LATEX")) {
                        res = result.toLatex();
                    } else {
                        error = "Illegal output format: "+outputFormat;
                    }
                }
            }
        } else {
            response.sendRedirect(HOME);
            return;
        }

        if (res != null && error.length() == 0) {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(res);
            ((Request)request).setHandled(true);
        } else if (res != null && error.length() > 0) {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(error);
            ((Request)request).setHandled(true);
        } else {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("ERROR. Was war denn das?!");
            ((Request)request).setHandled(true);
        }
    }

}
