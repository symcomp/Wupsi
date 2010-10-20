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

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;

/**
 * handles the 'set' command family, which are used to set a couple of
 * options.
 */
public class SetHandler extends WupsiHandler {

    Wupsifer w;

    public SetHandler(Wupsifer w) {
        this.w = w;
    }

    public String command() {
        return "set ";
    }

    public void handle(String in) {
		//Deconstruct input
		Pattern pp = Pattern.compile("^set (prompt|output format|encoding) (.*)$");
		Matcher mm = pp.matcher(in);
		if (!mm.matches()) {
			w.error("# syntax: " + help());
			return;
		}
		String command = mm.group(1);
		String arg = mm.group(2);

		//Do stuff.
		if (command.equals("prompt")) setPrompt(arg);
		else if (command.equals("output format")) setOutput(arg);
		else if (command.equals("encoding")) setEncoding(arg);
		else {
            w.error("# ERROR: " + help());
        }
    }

    private void setOutput(String formatin) {
        String format = formatin.toUpperCase();
        if (!format.equals("POPCORN") && !format.equals("XML") && !format.equals("LATEX") && !format.equals("HEXDUMP")) {
            w.error("# ERROR: illegal format specification: '"+format+"'");
            return;
        }
        w.setOutputFormat(format);
        w.info("# switched output format to "+format+".");
    }

    private void setPrompt(String arg) {
        w.setPrompt(arg);
    }

	private void setEncoding(String arg) {
		String ac = w.getActiveClient();
		if (ac == null) {
			w.error("# ERROR: No active system");
			return;
		}
		SCSCPClient c = w.getClient(ac);
		
		//List available encodings
		List<OpenMathBase.ENCODINGS> sup = c.getSupportedEncodings();
		String t = "# system '" + ac + "' supports:";
		for (OpenMathBase.ENCODINGS enc : sup) t += " " + enc.toString(); 
		w.info(t);
		
		//Get the wanted encoding
		OpenMathBase.ENCODINGS want = OpenMathBase.ENCODINGS.get(arg);
		if (want == null) {
			w.error("# Unrecognized encoding: '" + arg + "'");
			return;
		}
		
		//Set, if possible
		if (sup.indexOf(want) == -1) {
			w.error("# Encoding not supported by server: '" + want.toString() + "'");
			return;			
		}
		
		//Set.
		boolean success = c.setActiveEncoding(want, false);
		w.info("# Setting encoding to '" + want.toString() + "': " + (success ? "OK" : "FAIL"));
	}

    public String help() {
        return "set {prompt|output format {latex|xml|popcorn}|encoding {xml|popcorn|binary}}";
    }
}
