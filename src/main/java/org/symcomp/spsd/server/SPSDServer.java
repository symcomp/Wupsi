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

package org.symcomp.spsd.server;

import org.symcomp.scscp.SCSCPServer;
import org.symcomp.scscp.SCSCPServerInfo;
import org.symcomp.scscp.ProcedureCallHandler;
import org.symcomp.openmath.OMSymbol;
import org.symcomp.spsd.SPSD;

import java.io.PrintWriter;
import java.io.BufferedReader;

/**
 * is an SCSCP Server offering the SPSD functionality to SCSCP clients, it is
 * administered by the org.symcomp.wupsi.handlers.SPSDHandler of Wupsifer.
 */
@SCSCPServerInfo(serviceName="SPSD", serviceVersion="1.6-SNAPSHOT", serviceDescription="")
public class SPSDServer extends SCSCPServer {

    private static SPSD spsd;
    ProcedureCallHandler handler;


    public SPSDServer(String s, PrintWriter printWriter, BufferedReader bufferedReader) {
        super(SPSDServer.class, s, printWriter, bufferedReader);
        if(spsd==null) throw new RuntimeException("ERROR: inject SPSD before creating instances of SPSDServer!");
        handler = new SPSDHandler(spsd);
    }

    public static void setSpsd(SPSD spsd) {
        SPSDServer.spsd = spsd;
    }

    public String getInstanceId() {
        return "qwiuqyuewqyeqwqe"; //mupad.getInstanceId();
    } // getInstanceId

    @Override
    public ProcedureCallHandler findHandler(OMSymbol servicename) {
        ProcedureCallHandler handler = super.findHandler(servicename);
        if(null!=handler)
            return handler;
        return this.handler;
    }

    @Override
    protected void cleanup() {
        super.cleanup();
    }
}
