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
import org.symcomp.openmath.OMApply;
import org.symcomp.openmath.OMVariable;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * handles the 'local' command, which is (currently) used to locally store the
 * result of a remote computation.
 */
public class LocalHandler extends WupsiHandler {

    Wupsifer w;

    public LocalHandler(Wupsifer w) {
        this.w = w;
    }

    public String command() {
        return "local ";
    }

    public void handle(String in) {
		//Deconstruct input
		Pattern pp = Pattern.compile("^local (.*)$");
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
		if (w.getActiveClient() == null) {
			w.error("# ERROR: no active client, so no assignments for you!");
			return;
		}
		omb = omb.deOMObject();
        if(omb.isApplication("prog1", "assign")) {
            OMApply oma = (OMApply) omb;
            OpenMathBase lhs = oma.getParam(0);
            if(!lhs.isVariable()) {
                w.error("# ERROR: left-hand-side of 'local' assignment must be a variable.");
                return;
            }
            OMVariable v = (OMVariable) lhs;
            OpenMathBase rhs = oma.getParam(1);
            OpenMathBase res = w.compute(w.getClients().get(w.getActiveClient()), rhs);
            w.getLocals().put(v.getName(), res.deOMObject());
            w.info("# Stored this in local variable '"+v.toPopcorn()+"':");
            w.println(res);
            return;
        }
        w.error("# ERROR: currently, only assignments are available through 'local'.");
    }

    public String help() {
        return "local <var> := <openmath expression>";
    }
}
