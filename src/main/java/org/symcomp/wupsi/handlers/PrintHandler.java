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
import org.symcomp.openmath.OpenMathBase;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * handles the 'print' command which is merely a translator into the currently
 * selected format.
 */
public class PrintHandler extends WupsiHandler {

    Wupsifer w;

    public PrintHandler(Wupsifer w) {
        this.w = w;
    }

    public String command() {
        return "print ";
    }

    public void handle(String in) {
		//Deconstruct input
		Pattern pp = Pattern.compile("^print (.*)$");
		Matcher mm = pp.matcher(in);
		if (!mm.matches()) {
			w.error("# syntax: " + help());
			return;
		}
		String command = mm.group(1);

		OpenMathBase omb;
        String m;
        try {
            m = "";
            omb = OpenMathBase.parse(command);
        } catch (Exception e) {
            omb = null; m = e.getMessage();
        }
        if (omb == null) {
            w.error("# ERROR: Could not parse command: " + m);
            return;
        }
		omb = w.replace(omb);
        w.println(omb);
    }

    public String help() {
        return "print <expression>";
    }
}
