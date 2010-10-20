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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * responsible to handle the connect command which is used to attach new clients
 * to the WUPSI.
 */
public class ConnectHandler extends WupsiHandler {

    Wupsifer w;

    public ConnectHandler(Wupsifer w) {
        this.w = w;
    }

    public String command() {
        return "connect";
    }

    public void handle(String in) {
        // Deconstruct the string "connect <host>[:port] as <name>"
		Pattern p = Pattern.compile("connect (([0-9A-Za-z\\_\\-\\.]+)(\\:([0-9]+))?) as ([A-Za-z][0-9A-Za-z\\_]*)");
		Matcher m = p.matcher(in);
		if (!m.matches()) {
			w.error("# syntax: " + help());
			return;
		}
        w.connect(m.group(1), m.group(5));
    }

    public String help() {
        return "connect <host>[:port] as <name>";
    }

}
