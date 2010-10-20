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

package org.symcomp.wupsi;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.symcomp.scscp.SCSCPClient;
import org.symcomp.scscp.ComputationState;
import org.symcomp.openmath.*;
import org.symcomp.wupsi.handlers.*;
import org.symcomp.notification.NotificationReceiver;
import org.symcomp.notification.Notification;
import org.symcomp.notification.NotificationCenter;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;

import jline.ConsoleReader;
import jline.History;
import jline.SimpleCompletor;

/**
 * is main loop class. It first evaluates the options, then registeres the 
 * handlers for the commands. Most state infos of the WUPSI are stored here
 * and all console user interaction takes place here.
 */
public class Wupsifer implements NotificationReceiver {
    Map<String, SCSCPClient> clients;
    String activeClient;
    String prompt;
    BufferedReader stdin;
    PrintStream stdout;
    String outputFormat;
    Map<String, OpenMathBase> locals;
    ConsoleReader consoleReader;
    List<OpenMathBase> inputHistory;
    List<OpenMathBase> outputHistory;
    List<OpenMathBase> examples;
    List<WupsiHandler> handlers;
    WupsiOptions options;
    private static Wupsifer instance;

    private Wupsifer(WupsiOptions options) {
        if (instance != null)
            throw new RuntimeException("Only one Instance, please!");
        this.options = options;
        clients = new Hashtable<String, SCSCPClient>();
        locals = new Hashtable<String, OpenMathBase>();
        inputHistory = new LinkedList<OpenMathBase>();
        outputHistory = new LinkedList<OpenMathBase>();
        examples = new LinkedList<OpenMathBase>();
        activeClient = null;
        prompt = "WUPSI[$ACT]$N> ";
        handlers = new LinkedList<WupsiHandler>();
        handlers.add(new AliasHandler(this));
        handlers.add(new ConnectHandler(this));
        handlers.add(new DescribeHandler(this));
        handlers.add(new DisconnectHandler(this));
        handlers.add(new HelpHandler(this));
        handlers.add(new LocalHandler(this));
        handlers.add(new QuitHandler(this));
        handlers.add(new SetHandler(this));
        handlers.add(new ShowHandler(this));
        handlers.add(new UseHandler(this));
        handlers.add(new PrintHandler(this));
        handlers.add(new ReadHandler(this));
        handlers.add(new SPSDHandler(this));
        WebHandler webHandler = new WebHandler(this);
        handlers.add(new WebHandler(this));
        handlers.add(new IrcHandler(this));
        // now, do processing of command line parameters
        // 1. find the 'in' channel
        try {
            consoleReader = new ConsoleReader();
            History history = new History(new File(".wupsi_history"));
            consoleReader.setHistory(history);
            consoleReader.addCompletor(new SimpleCompletor(CdDescription.completions()));
        } catch (IOException ignore) {
            if (options.output == null)
                info("# could not initialize JLine, falling back: "+ignore.getMessage());
            consoleReader = null;
            stdin = new BufferedReader(new InputStreamReader(System.in));
        }
        if (options.commDir != null) {
            if (options.input != null || options.output != null) {
                System.err.println("Error: If -c is specified, -i and -o are not reasonable.");
                System.exit(1);
            }
        }
        if (options.input != null) {
            try {
                stdin = new BufferedReader(new FileReader(options.input));
            } catch (FileNotFoundException e) {
                error("# ERROR: can't open input file '"+options.input+"'");
                System.exit(2);
            }
        }
        // 2. define the 'out' channel
        if (options.output == null) {
            stdout = System.out;
        } else {
            try {
                stdout = new PrintStream(options.output);
            } catch (FileNotFoundException e) {
                error("# ERROR: can't open output file '"+options.output+"'");
                System.exit(2);
            }
        }
        // 3. define standard output format
        if (options.outputFormat != null) {
            String of = options.outputFormat.toUpperCase().trim();
            if (of.equals("XML") || of.equals("POPCORN") || of.equals("LATEX") || of.equals("BINARY")) {
                outputFormat = of;
            } else {
                warning("# WARNING: Illegal output format '"+of+"' given, ignoring.");
            }
        } else {
            outputFormat = "POPCORN";
        }
        // 4. handle the a-priori-connects
        if (options.connects!=null) {
            for(String c : options.connects) {
                Pattern p = Pattern.compile("(([0-9A-Za-z\\_\\-\\.]+)(\\:([0-9]+))?) ([A-Za-z][0-9A-Za-z\\_]*)");
                Matcher m = p.matcher(c);
                if (!m.matches()) {
                    error("# ERROR: illegal connect specification: '"+c+"'");
                    System.exit(3);
                }
                connect(m.group(1), m.group(5));
            }
        }
        // 5. start webserver
        if (options.webserver != null) {
            webHandler.startWeb(options.webserver);
        }
        // 99. done
        info("\nWUPSI 1.6-SNAPSHOT -- WUPSI Universal Popcorn SCSCP Interface");
        if (Math.random()<0.5) info("(c) 2010 D. Roozemond & P. Horn"); else info("(c) 2010 P. Horn & D. Roozemond");
        instance = this;
    }

    public void run() throws IOException {
        while(true) {
            boolean handeled = false;
            String in = getInput();

			if (in == null) in = "quit";  //handle ^D

            if(in.length() == 0)
                continue;

            for (Iterator<WupsiHandler> it = handlers.iterator(); it.hasNext() && !handeled;) {
                WupsiHandler wh = it.next();
                if (in.startsWith(wh.command())) {
                    wh.handle(in);
                    handeled = true;
                }
            }

            if (!handeled) {
				if (activeClient != null) {
	                compute(clients.get(activeClient), in);
				} else {
					error("# ERROR: no system active.");					
				}
            }

        }
    } // run

    /**
     * meta-handler for computing
     * @param client the client on which to execute
     * @param in the input string
     */
    public void compute(SCSCPClient client, String in) {
        String s, m;
        OpenMathBase omb;

        try {
            m = "";
            omb = OpenMathBase.parse(in);
        } catch (Exception e) {
            omb = null;
            m = e.getMessage();
        }
        if (omb == null) {
            error("# ERROR: Could not parse command: " + m);
            return;
        }

        OpenMathBase res = compute(client, omb);
        if (null == res) return;
        println(res);
    } // compute

    /**
     * does the actual computation
     * @param client
     * @param omb
     * @return
     */
    public OpenMathBase compute(SCSCPClient client, OpenMathBase omb) {
        if (client==null) {
            error("# ERROR: no system chosen so far.");
            return null;
        }

        omb = replace(omb);

        String token = client.compute(omb);
        while (!client.resultAvailable(token)) { try { Thread.sleep(80); } catch(Exception e) {} }

        Integer st = client.getComputationState(token);
        if (st == ComputationState.READY) {
            OpenMathBase res = client.getResult(token);
            inputHistory.add(omb);
            outputHistory.add(res);
            return res;
        } else if (st == ComputationState.ERRONEOUS) {
			      info("# ComputationState: ERRONEOUS");
            OpenMathBase res = client.getResult(token);
            inputHistory.add(omb);
            outputHistory.add(res);
			      return res;
		} else {
            if (st == ComputationState.WAITING) info("# ComputationState: WAITING");
            if (st == ComputationState.COMPUTING) info("# ComputationState: COMPUTING");
        }
        return null;
    } // compute

    /**
     * replace all occurrencies of special variables in the OpenMath tree
     * @param omb the OpenMath tree
     * @return a replaced version
     */
    public OpenMathBase replace(OpenMathBase omb) {
        omb = omb.toOMObject();
        omb = omb.traverse(new OpenMathVisitor() {
            public OpenMathBase visitVariable(OMVariable om) {
                String name = om.getName();
                OpenMathBase r = locals.get(name);
                if (null != r) return r;
                if (name.matches("_in\\d+")) {
                    int n = Integer.parseInt(name.substring(3));
                    if (inputHistory.get(n) != null) return inputHistory.get(n);
                }
                if (name.matches("_out\\d+")) {
                    int n = Integer.parseInt(name.substring(4));
                    if (outputHistory.get(n) != null) return outputHistory.get(n);
                }
                if (name.matches("_example\\d+")) {
                    int n = Integer.parseInt(name.substring(8));
                    if (examples.get(n) != null) return examples.get(n);
                }
                return om;
            }
        });
        return omb.deOMObject();
    } // replace

    /**
     * produces a nice info string
     * @param client the client for which to get the infos
     * @return the description
     */
    public String serviceInfo(SCSCPClient client) {
        return "service Name '"+client.getServiceName()+"', service version '"+client.getServiceVersion()+"'";
    } // serviceInfo

    /**
     * reads input -- either using JLine, or usind readLine() on a stream
     * @return the input
     * @throws IOException if s/th went wrong
     */
    private String getInput() throws IOException {
        if (options.commDir != null) {
            return getInputDirectory();
        }
        String p = prompt;
        if (null != activeClient) {
            p = prompt.replaceAll("\\$ACT", activeClient);
        } else {
            p = prompt.replaceAll("\\$ACT", "n/a");
        }
        p = p.replaceAll("\\$N", ""+inputHistory.size());
        String s = null;
        if (null==consoleReader || options.input!=null) {
            if (options.input == null) print("\n"+p);
            if (options.input != null && options.atomic) {
                s = stdin.readLine();
                if (s==null)
                    System.exit(0);
                String s2;
                while(null!=(s2=stdin.readLine()))
                    s += s2;
            } else {
                s = stdin.readLine();
            }
            if (s==null && options.input!=null && (options.quit || options.atomic)) {
                System.exit(0);
            } else if (s==null && options.input!=null && !options.quit) {
                options.input = null;
                return getInput();
            }
        } else {
            System.out.print("\n"+p);
            consoleReader.setDefaultPrompt(p);
            s = consoleReader.readLine();
        }
        if (s != null) s = s.trim();
        return s;
    } // getInput

    private String getInputConsole() {
        return "";
    }
    /**
     * Gets the next input from the input directory
     * @return the next input
     * @throws java.io.IOException
     */
    private String getInputDirectory() throws IOException {
        assert (options.commDir != null);
        int n = inputHistory.size();
        if (n > 0) {
            // clean up
            stdout.flush();
            stdout.close();
            // move the tempfile to the final name
            File oldfile=new File(options.commDir+"/tempout");
            File newfile=new File(options.commDir+"/output"+n);
            boolean ok = oldfile.renameTo(newfile);
            assert ok;
            // System.out.println("Things worked: " + ok);
        }
        // open new input file
        File infile=new File(options.commDir+"/input"+(n+1));
        try {
            while (!infile.exists()) { Thread.sleep(10); }
        } catch (InterruptedException ex) {
            return "quit";
        }
        // If we came here, the file obviously exists.
        stdin = new BufferedReader(new FileReader(infile));
        String s2, s="";
        while(null!=(s2=stdin.readLine()))
            s += s2;
        stdin.close();
        File outfile=new File(options.commDir+"/tempout");
        stdout = new PrintStream(outfile);
        return s;
    }

    /**
     * connect
     * @param url
     * @param name
     * @return true of the connection could be established
     */
    public boolean connect(String url, String name) {
        if (clients.containsKey(name)) {
            error("# ERROR: name '"+name+"' already used, try 'disconnect'.");
            return false;
        }
        Pattern p = Pattern.compile("([0-9A-Za-z\\_\\-\\.]+)(\\:([0-9]+))?");
        Matcher m = p.matcher(url);
        if (!m.matches()) {
            return false;
        }
        String host = m.group(1);
        int port = 26133;
        if (m.group(3) != null) {
            port = Integer.parseInt(m.group(3));
        }

        // Make connection.
        try {
            // connect
            SCSCPClient client = new SCSCPClient(host, port);
            clients.put(name, client);
            info("# connected to '"+host+"' on port '"+port+"' using symbolic name '"+name+"'");
            info("# Service Info: "+serviceInfo(client));
            activeClient = name;
            NotificationCenter.getNC().register(this, "DEAD");
        } catch (Exception e) {
            error("# ERROR: could not connect to '" + host +"' on port '" + port + "'");
            error("# ERROR: " + e.getMessage());
            return false;
        }
        return true;
    }


    private void hexdump(char[] s) {
        stdout.printf ("---- Total Length: %d ----", s.length);
        for (int i = 0; i<s.length; i++) {
            if (i%20 == 0)
                stdout.println("");
            else if (i%10 == 0)
                stdout.print("  ");
            else if (i%5 == 0)
                stdout.print(" ");

            stdout.printf("%02x ", (int) s[i]);
        }
        stdout.println("");
    }

    public void print(OpenMathBase om) {
        if (outputFormat.toUpperCase().equals("POPCORN")) { print(om.toPopcorn()); }
        else if (outputFormat.toUpperCase().equals("XML")) { print(om.toXml()); }
        else if (outputFormat.toUpperCase().equals("LATEX")) { print(om.toLatex()); }
        else if (outputFormat.toUpperCase().equals("BINARY")) { print(om.toOMObject().toBinary()); }
        else if (outputFormat.toUpperCase().equals("HEXDUMP")) { hexdump(om.toOMObject().toBinary()); }
        else { print("[can't resolve output format '"+outputFormat+"']");
        }
    }

    public void println(OpenMathBase om) {
        print(om);
        println("");
    }

    public void println(String s) {
        stdout.println(s);
    }

    public void print(String s) {
        stdout.print(s);
    }

    public void print(char[] s) {
        stdout.print(s);
    }

    public void info(String s) {
        if (!options.quiet)
            println(s);
    }

    public void warning(String s) {
        if (!options.quiet)
            println(s);
    }

    public void error(String s) {
        System.out.println(s);
    }

	public SCSCPClient getClient(String name) {
		return clients.get(name);
	}

    public Map<String, SCSCPClient> getClients() {
        return clients;
    }

    public void setClients(Map<String, SCSCPClient> clients) {
        this.clients = clients;
    }

    public String getActiveClient() {
        return activeClient;
    }

    public void setActiveClient(String activeClient) {
        this.activeClient = activeClient;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Map<String, OpenMathBase> getLocals() {
        return locals;
    }

    public void setLocals(Map<String, OpenMathBase> locals) {
        this.locals = locals;
    }

    public List<OpenMathBase> getInputHistory() {
        return inputHistory;
    }

    public void setInputHistory(List<OpenMathBase> inputHistory) {
        this.inputHistory = inputHistory;
    }

    public List<OpenMathBase> getOutputHistory() {
        return outputHistory;
    }

    public void setOutputHistory(List<OpenMathBase> outputHistory) {
        this.outputHistory = outputHistory;
    }

    public List<OpenMathBase> getExamples() {
        return examples;
    }

    public void setExamples(List<OpenMathBase> examples) {
        this.examples = examples;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public static Wupsifer getInstance() {
        return instance;
    }

    public static Wupsifer getInstance(WupsiOptions options) {
        if (null==instance)
            return new Wupsifer(options);
        return instance;
    }

    public List<WupsiHandler> getHandlers() {
        return handlers;
    }

	public void systemDied(SCSCPClient client) {
		systemCleanUp(client, true);
	}
	public void systemDisconnect(SCSCPClient client) {
		systemCleanUp(client, false);
	}

	public void systemCleanUp(SCSCPClient client, boolean died) {
		//Traverse handlers to see if anything should happen
        for (WupsiHandler wh : getHandlers()) {
            wh.systemDiedHook(client);
        }

		//Quit the client
        try {
            client.quit();
		} catch (Exception idc) {}

		//Remove (awkwardly) from the clients hashmap, 
		//by first creating a temporary array...
		List<String> tormv = new ArrayList<String>();
        for (String key : clients.keySet()) {
            SCSCPClient val = clients.get(key);
            if (client == val) {
				if (died) error("# The system '" + key + "' died.");
				tormv.add(key);
			}
		}
		//...and then actually removing them
		for (String key : tormv) {
	        clients.remove(key);

			//Set active client to null if needed
	        if (activeClient != null && activeClient.equals(key)) {
	            activeClient = null;
	        }

			//Log
	        info("# The system '"+key+"' was removed from the list");
		}
	}

    public void receiveNotification(Notification notification) {
        assert notification.getMessage().equals("DEAD");
		systemDied((SCSCPClient) notification.getSender());
    }
}
