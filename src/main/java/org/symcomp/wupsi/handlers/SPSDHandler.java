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

import org.symcomp.wupsi.Wupsifer;
import org.symcomp.scscp.SCSCPClient;
import org.symcomp.spsd.SPSD;
import org.symcomp.spsd.server.SPSDServer;

import java.util.List;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * handle the 'spsd' command family which is used to administer the
 * 'Simple Parallel SCSCP Dispatcher'.
 */
public class SPSDHandler extends WupsiHandler  {

    Wupsifer w;
    SPSD spsd;
    String spsdName;

    public SPSDHandler(Wupsifer w) {
        this.w = w;
        spsd = null;
        spsdName = null;
    }

    public String command() {
        return "spsd ";
    }

	public void startServer(String spsdName) {
        spsd = new SPSD("asa", 1);
        w.info("#SPSD connected to SPSD using symbolic name '"+spsdName+"'");
        w.info("#SPSD Service Info: "+w.serviceInfo(spsd));
        w.getClients().put(spsdName, spsd);
        w.setActiveClient(spsdName);
	}
	
	public void addWorkers(String[] args) {
        for (String s : args) {
            if (s.equals("*")) {
                spsd.clearWorkers();
                for(String name : w.getClients().keySet()) {
                    SCSCPClient c = w.getClients().get(name);
                    if (c.getServiceName().equals("SPSD")) {
                        w.info("#SPSD Cowardly refusing to add SPSD system '"+name+"'");
                    } else {
                        w.info("#SPSD Adding system '" + name + "'.");
                        spsd.addWorker(c);
                    }
                }
            } else {
                SCSCPClient c = w.getClients().get(s);
                if (c == null) {
                    w.error("#SPSD ERROR: No system named '" + s + "' found.");
                } else if (c.getServiceName().equals("SPSD")) {
                    w.warning("#SPSD WARNING: Cowardly refusing to add SPSD system '"+s+"'");
                } else {
                    w.info("#SPSD Adding system '" + s + "'.");
                    spsd.addWorker(c);
                }
            }
        }		
	}
	
	public void clearWorkers() {
        w.info("#SPSD Removing all workers");
        spsd.clearWorkers();
	}
	
	public void clearWorker(SCSCPClient client) {
		if (client.getServiceName().equals("SPSD")) return;
		if (spsd == null) return;
		
		boolean rmvd = spsd.removeWorker(client);
		if (rmvd) {
			w.info("#SPSD Removed worker '" + client.getServiceName() + "'"); 
		} 
	}
	
	public void showWorkers() {
        w.println("#SPSD There are "+spsd.getWorkerCount()+" workers attached.");
        for(SCSCPClient c : spsd.getWorkers()) {
            w.println("# "+w.serviceInfo(c));
        }
	}
	
	public void startServer(int port) {
        SPSDServer.setSpsd(spsd);
        try {
            SPSDServer.spawn(SPSDServer.class, port);
        } catch (Exception e) {
            e.printStackTrace();
            w.error("#SPSD ERROR: Spawning SPSDS went wrong");
        }		
	}

    public void handle(String in) {
        Pattern pp = Pattern.compile("^spsd (start as|add|clear|show|start server)( (.+))?$");
        Matcher mm = pp.matcher(in);
        if (!mm.matches()) {
            w.error("# syntax: " + help());
            return;
        }
        String command = mm.group(1);
		String arg = mm.group(3);

        if (command.equals("start as")) {
			if (arg == null) { w.error("# syntax: " + help()); return;}

            if (null != spsd) {
                w.warning("#SPSD WARNING: SPSD already connected as '"+spsdName+"'");
                return;
            } else if (arg == null || arg.length() == 0) {
                w.error("#SPSD ERROR: Usage: spsd start as <name>");
                return;
            }
			startServer(arg);
			return;
        }

        if (null==spsd) {
            w.error("#SPSD ERROR: SPSD not running, start with 'spsd start as <name>' first.");
            return;
        }

        if (command.startsWith("add")) {
			if (arg == null) { w.error("# syntax: " + help()); return;}

            String[] p = arg.split(" ");
			addWorkers(p);
        } else if (command.startsWith("clear")) {
			clearWorkers();
        } else if (command.startsWith("show")) {
			showWorkers();
        } else if (command.equals("start server")) {
            int port;
        	try {
                port = Integer.parseInt(arg);
                if (port<1000 || port > 65534) { throw new Exception("Don't use Exceptions as goto! that's bad style!"); }
            } catch (Exception e) {
                w.error("# syntax: spsd start server <port>"); return;
            }

			startServer(port);
        }

    }

    public String help() {
        return "spsd {start as <name>|add <name(s)>|clear|show|start server <port>}";
    }

	@Override
    public void shutdownHook() {
        if (null==spsd) return;
        spsd.quit();
        w.info("#SPSD Quitting spsd");
    }

	@Override
	public void systemDiedHook(SCSCPClient client) {
		clearWorker(client);
	}
}
