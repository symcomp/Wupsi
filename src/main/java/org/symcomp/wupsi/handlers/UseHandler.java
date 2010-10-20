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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * used to handle the 'use' command, which switches the currently active system.
 */
public class UseHandler extends WupsiHandler {

    Wupsifer w;

    public UseHandler(Wupsifer w) {
        this.w = w;
    }

    public String command() {
        return "use";
    }

    public void handle(String in) {
		//Deconstruct input
		Pattern pp = Pattern.compile("^use (.*)$");
		Matcher mm = pp.matcher(in);
		if (!mm.matches()) {
			w.error("# syntax: " + help());
			return;
		}
		String name = mm.group(1);

		//Do
        SCSCPClient client = w.getClients().get(name);
        if (null == client) {
            w.error ("# ERROR: There is no system named '"+name+"' attached.");
            return;
        }
        String s = "system with symbolic name '"+name+"', "+w.serviceInfo(client);
        w.setActiveClient(name);
        w.info("# switched to "+s+".");
    }

    public String help() {
        return "disconnect <name>";
    }
}
