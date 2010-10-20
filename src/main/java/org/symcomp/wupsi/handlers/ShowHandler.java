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
import org.symcomp.openmath.OpenMathBase;

import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * handles the 'show' command family that is used to display some state
 * infos about the WUPSI.
 */
public class ShowHandler extends WupsiHandler {

    Wupsifer w;

    public ShowHandler(Wupsifer w) {
        this.w = w;
    }

    public String command() {
        return "show ";
    }

    public void handle(String in) {
		//Deconstruct input
		Pattern pp = Pattern.compile("^show (systems|locals|history|examples)$");
		Matcher mm = pp.matcher(in);
		if (!mm.matches()) {
			w.error("# syntax: " + help());
			return;
		}
		String command = mm.group(1);

        if (command.equals("systems")) showSystems();
        else if (command.equals("locals")) showLocals();
        else if (command.equals("history")) showHistory();
        else if (command.equals("examples")) showExamples();
        else {
            w.error("# ERROR: " + help());
        }
    }

    private void showSystems() {
        Map<String, SCSCPClient> clients = w.getClients();
        if(clients.size() == 0) {
            w.println("# There are currently no systems attached");
            return;
        }
        w.println("# name, serviceName, serviceVersion, host, port");
        for (String n : clients.keySet()) {
            if (n.equals(w.getActiveClient())) {
                w.print("# * ");
            } else {
                w.print("#   ");
            }
            w.print(n+", ");
            SCSCPClient client = clients.get(n);
            w.println(client.getServiceName()+", "+client.getServiceVersion()+", "+client.getSCSCPUri()+", "+client.getSCSCPPort());
        }
    } // listSystems

    private void showLocals() {
        Map<String,OpenMathBase> locals = w.getLocals();
        if(locals.size() == 0) {
            w.println("# There are currently no local variables.");
            return;
        }
        w.println("# name = value");
        for (String n : locals.keySet()) {
            w.print("# $"+n+" = ");
            w.println(locals.get(n));
        }
    }

    private void showHistory() {
        List<OpenMathBase> inputHistory = w.getInputHistory();
        List<OpenMathBase> outputHistory = w.getOutputHistory();
        if(inputHistory.size() == 0) {
            w.println("# The History is currently empty.");
            return;
        }
        w.println("# name = value");
        for (int i=0; i<inputHistory.size(); i++) {
            w.print("# $_in"+i+" = ");
            w.println(inputHistory.get(i));
            w.print("# $_out"+i+" = ");
            w.println(outputHistory.get(i));
        }
    }

    private void showExamples() {
        List<OpenMathBase> examples = w.getExamples();
        if(examples.size() == 0) {
            w.println("# There are no stored examples.");
            return;
        }
        w.println("# name = value");
        for (int i=0; i<examples.size(); i++) {
            w.print("# $_example"+i+" = ");
            w.println(examples.get(i));
        }
    }


    public String help() {
        return "show {systems|locals|history|examples}";
    }
}
