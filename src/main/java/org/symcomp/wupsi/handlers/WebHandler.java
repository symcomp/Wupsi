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

package org.symcomp.wupsi.handlers;

import java.util.Enumeration;
import org.symcomp.wupsi.Wupsifer;
import org.symcomp.webwupsi.Wupslet;
import org.mortbay.jetty.Server;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * handles the 'web' command family responsible for starting and stopping the
 * web server.
 */
public class WebHandler extends WupsiHandler {

    private Wupsifer w;
    private static Server server;

    public WebHandler(Wupsifer w) {
        server = null;
        this.w = w;
    }

    public String command() {
        return "web ";
    }

    public void handle(String in) {
        Pattern pp = Pattern.compile("^web (start (\\d+)|stop)$");
        Matcher mm = pp.matcher(in);
        if (!mm.matches()) {
            w.error("# syntax: " + help());
            return;
        }
        String command = mm.group(1);

        if (command.startsWith("start ")) {
            int port = Integer.parseInt(mm.group(2));
            if (startWeb(port))
                w.info("# started webserver on port "+port);
            else
                w.info("# could not start webserver (see message above).");
        } else if (command.equals("stop")) {
			stopWeb();
		}

    }

    public boolean startWeb(int port) {
        if (null != server) {
            w.error("# ERROR: webserver already running.");
            return false;
        }
        server = new Server(port);
        server.setAttribute("RequestLog", null);
        Enumeration s;

        //server.set
        try {
            // initialize Jetty
            Connector connector = new SelectChannelConnector();
            connector.setPort(Integer.getInteger("jetty.port", port));
            server.setConnectors(new Connector[]{connector});

            // add Tapestry5 app
            WebAppContext webapp = new WebAppContext();
            webapp.setContextPath("/");
            String jetty_home = System.getProperty("jetty.home", "./");
            webapp.setWar(jetty_home + "src/main/webapp/");
            // Remove slf4j from list of classes not exposed to webapp
            webapp.setServerClasses(new String[] {"-org.mortbay.jetty.plus.jaas.", "org.mortbay.jetty."});
            server.addHandler(webapp);

            server.start();
    		//server.join();
            s = server.getAttributeNames();
            while(s.hasMoreElements()) {
                System.out.println("lala "+s.nextElement());
            }


        } catch (Exception e) {
            e.printStackTrace();
            w.error("# ERROR: could not start webserver: "+e.getMessage());
			
			try { server.stop(); server = null; } catch (Exception ee) { }
			
            return false;
        }
        return true;
    }

	public void stopWeb() {
        if (null==server) return;
        try {
            w.info("# shutting down webserver...");
            server.stop();
			server = null;
        } catch (Exception ex) {
            w.error("# ERROR: Failure when shutting down webserver: "+ex.getMessage());
        }	
	}

    public String help() {
        return "web {start <port>|stop}";
    }

	@Override
    public void shutdownHook() {
		stopWeb();
    }
}
