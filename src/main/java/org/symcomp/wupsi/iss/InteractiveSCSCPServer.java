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

package org.symcomp.wupsi.iss;

import java.io.BufferedReader;
import java.io.PrintWriter;
import org.symcomp.openmath.OMSymbol;
import org.symcomp.scscp.ProcedureCallHandler;
import org.symcomp.scscp.SCSCPServer;
import org.symcomp.scscp.SCSCPServerInfo;

/**
 * InteractiveScscpServer -- offers the possiblilty to manually enter
 * the result of computations
 */
@SCSCPServerInfo(serviceName="InteractiveSCSCPServer", serviceVersion="1.6-SNAPSHOT", serviceDescription="...")
public class InteractiveSCSCPServer extends SCSCPServer {

    ProcedureCallHandler iisHandler;

    public InteractiveSCSCPServer(String s, PrintWriter printWriter, BufferedReader bufferedReader) {
        super(InteractiveSCSCPServer.class, s, printWriter, bufferedReader);
        iisHandler = new InteractiveCallHandler();
        System.out.println("# Got Connection "+s);
    }

    @Override
    public ProcedureCallHandler findHandler(OMSymbol servicename) {
        ProcedureCallHandler h = super.findHandler(servicename);
        if (null != h) return h;
        return iisHandler;
    }
}
