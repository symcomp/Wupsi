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

import org.symcomp.scscp.ProcedureCallHandler;
import org.symcomp.openmath.OMSymbol;
import org.symcomp.openmath.OpenMathException;
import org.symcomp.openmath.OpenMathBase;
import org.symcomp.spsd.SPSD;

/**
 * is called by the SPSD -- the actual computation is delegated to the
 * SPSD registered by the Wupsifer.
 */
public class SPSDHandler extends ProcedureCallHandler {

    SPSD spsd;

    public SPSDHandler(SPSD spsd) {
        this.spsd = spsd;
    }

    @Override
    public OpenMathBase handlePayload(OpenMathBase openMathBase) throws OpenMathException {
        String token = spsd.compute(openMathBase);
        while (!spsd.resultAvailable(token)) { try { Thread.sleep(80); } catch(Exception e) {} }
        return spsd.getResult(token);
    }

    public String getDescription(OMSymbol omSymbol) {
        return "";
    }


}
