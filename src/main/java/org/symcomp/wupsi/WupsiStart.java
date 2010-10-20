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

package org.symcomp.wupsi;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.CmdLineException;

import java.io.IOException;
import org.symcomp.wupsi.iss.InteractiveSCSCPServer;

/**
 * used to start the wuspi
 */
public class WupsiStart {

    public static void main(String[] args) throws IOException {
        WupsiOptions options = new WupsiOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("wupsi.sh [options...] arguments...");
            parser.printUsage(System.err);
            return;
        }
        if (options.scscpserver != null) {
            try {
            System.out.println("\nWUPSI 1.6-SNAPSHOT -- Wonderful Universal Popcorn SCSCP Interface");
            System.out.println("running in ISS mode -- Interactive SCSCP Server");
            if (Math.random()<0.5) System.out.println("(c) 2010 D. Roozemond & P. Horn"); else System.out.println("(c) 2010 P. Horn & D. Roozemond");
                InteractiveSCSCPServer.breed(InteractiveSCSCPServer.class, options.scscpserver);
            } catch (Exception ex) {
                Logger.getLogger(WupsiStart.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Wupsifer.getInstance(options).run();
        }
    }

}
