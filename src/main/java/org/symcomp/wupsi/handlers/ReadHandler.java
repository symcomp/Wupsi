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

import java.io.File;
import java.io.FileReader;
import org.symcomp.wupsi.Wupsifer;
import org.symcomp.openmath.OpenMathBase;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * handles the 'read' command which reads a single file which is expected
 * to contain precisely one OpenMath Object in either XML, Popcorn or
 * OpenMath Binary.
 */
public class ReadHandler extends WupsiHandler {

    Wupsifer w;

    public ReadHandler(Wupsifer w) {
        this.w = w;
    }

    public String command() {
        return "read ";
    }

    public void handle(String in) {
		//Deconstruct input
		Pattern pp = Pattern.compile("^read (.*)$");
		Matcher mm = pp.matcher(in);
		if (!mm.matches()) {
			w.error("# syntax: " + help());
			return;
		}
		String filename = mm.group(1).trim();

		OpenMathBase omb;
            File file = new File(filename);
            if (!file.exists()) {
                w.error("# ERROR: Could not find file '" + filename + "'");
                return;
            }
        try {
            FileReader fr = new FileReader(file);
            omb = OpenMathBase.parse(fr);
            fr.close();
        } catch (Exception e) {
            omb = null;
        }
        if (omb == null) {
                w.error("# ERROR: Could not parse file '" + filename + "'");
            return;
        }
            omb = w.replace(omb);
        w.compute(w.getClient(w.getActiveClient()), omb.toXml());
    }

    public String help() {
        return "read <filename>";
    }
}
