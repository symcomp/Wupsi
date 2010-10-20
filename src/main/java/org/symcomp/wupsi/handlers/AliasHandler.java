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
 * responsible for handling the 'alias' command which stores an unevaluated
 * OM expression in a local variable
 */
public class AliasHandler extends WupsiHandler {

    Wupsifer w;

    public AliasHandler(Wupsifer w) {
        this.w = w;
    }

    public String command() {
        return "alias ";
    }

    public void handle(String in) {
		//Deconstruct input
		Pattern pp = Pattern.compile("^alias (.*)$");
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
            w.warning("# WARNING: Could not parse command: " + m);
            return;
        }
		omb = omb.deOMObject();
        if(omb.isApplication("prog1", "assign")) {
            OMApply oma = (OMApply) omb;
            OpenMathBase lhs = oma.getParam(0);
            if(!lhs.isVariable()) {
                w.error("# ERROR: left-hand-side of 'alias' assignment must be a variable.");
                return;
            }
            OMVariable v = (OMVariable) lhs;
            OpenMathBase rhs = oma.getParam(1);
            w.getLocals().put(v.getName(), rhs);
            w.info("# Stored in local variable '"+v.toPopcorn()+"'.");
            return;
        }
        w.error("# ERROR: currently, only assignments are available through 'alias'.");
    }

    public String help() {
        return "alias <var> := <openmath expression>";
    }

}
