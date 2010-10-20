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
import org.symcomp.wupsi.CdDescription;
import org.symcomp.openmath.OpenMathBase;

import java.util.LinkedList;
import java.util.List;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * handles the 'describe' command which looks up some documentation for
 * OM symbols.
 */
public class DescribeHandler extends WupsiHandler {

    Wupsifer w;

    public DescribeHandler(Wupsifer w) {
        this.w = w;
    }

    public String command() {
        return "describe ";
    }

    public void handle(String in) {
		//Deconstruct input
		Pattern pp = Pattern.compile("^describe (.*)$");
		Matcher mm = pp.matcher(in);
		if (!mm.matches()) {
			w.println("# syntax: " + help());
			return;
		}
		String command = mm.group(1);

        List<OpenMathBase> examples = w.getExamples();
        CdDescription description = CdDescription.describe(command);
        if (null == description) {
            w.println("# Sorry, no description for '"+command+"' available.");
            return;
        }
        w.println("# -- Description for '"+command+"' --");
        w.println(description.getDescription());
        String[] exx = description.getExamples();
        examples = new LinkedList<OpenMathBase>();
        int i = 0;
        for (String ex : exx) {
            w.println ("# Example $_example"+i);
            i++;
            try {
                OpenMathBase om = OpenMathBase.parse(ex);
                w.println(om);
                examples.add(om);
            } catch (Exception e) {
                w.println("# There was a syntax error in the provided example.");
            }
        }
        if (exx.length>0)
            w.setExamples(examples);
        w.println("# -- END description for '"+command+"' --");
    }

    public String help() {
        return "descibe <cd>[.name]";
    }

}
