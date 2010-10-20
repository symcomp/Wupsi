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

/**
 * handle the 'help' command.
 */
public class HelpHandler extends WupsiHandler {

    Wupsifer w;

    public HelpHandler(Wupsifer w) {
        this.w = w;
    }

    public String command() {
        return "help";
    }

    public void handle(String in) {
        w.println("# Using SCSCP servers:");
        w.println("  # connect <host>[:port] as <name>");
        w.println("  # use <name>");
        w.println("  # disconnect <name>");
        w.println("  # show systems");
        w.println("# Using locally stored objects:");
        w.println("  # local $a := ...");
        w.println("  # alias $a := ...");
        w.println("  # (simply use $a in some expression)");
        w.println("  # show locals");
        w.println("# OpenMath information:");
        w.println("  # describe <cd>");
        w.println("  # describe <cd>.<symbolname>");
        w.println("  # show examples");
        w.println("# Misc:");
        w.println("  # read <filename>");
        w.println("  # set output format {popcorn|latex|xml}");
        w.println("  # set encoding {popcorn|latex|xml}");
        w.println("  # print ...");
        w.println("  # show history");
        w.println("  # quit");
        w.println("# Simple Parallell SCSCP Dispatcher:");
        w.println("  # spsd start as <name>");
        w.println("  # spsd add {<name>|*}");
        w.println("  # spsd show");
        w.println("  # spsd clear");
        w.println("  # spsd.map(<list>, <expression>)");
        w.println("  # spsd.zip(<list>, <list>, <expression>)");
        w.println("  # spsd.all_pairs(<list>, <list>, <expression>)");
        w.println("  # spsd start server <port>");
    }

    public String help() {
        return "help";
    }
}
