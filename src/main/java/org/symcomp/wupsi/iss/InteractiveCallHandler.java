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

import java.io.File;
import jline.ConsoleReader;
import jline.History;
import jline.SimpleCompletor;
import org.symcomp.openmath.OMSymbol;
import org.symcomp.openmath.OpenMathBase;
import org.symcomp.openmath.OpenMathException;
import org.symcomp.scscp.ProcedureCallHandler;
import org.symcomp.wupsi.CdDescription;

/**
 * Offers the possiblilty to manually enter
 * the result of computations
 */
public class InteractiveCallHandler extends ProcedureCallHandler {

    private ConsoleReader consoleReader;

    public InteractiveCallHandler() {
        try {
            consoleReader = new ConsoleReader();
            History history = new History(new File(".wupsi_history"));
            consoleReader.setHistory(history);
            consoleReader.addCompletor(new SimpleCompletor(CdDescription.completions()));
        } catch (Exception ignore) {
            println("Could not initialize JLine: "+ignore.getMessage());
            System.exit(1);
        }
    }

    @Override
    public String getDescription(OMSymbol arg0) {
        //!! TODO: add rescription
        return "[TBD]";
    }

    @Override
    public OpenMathBase handlePayload(OpenMathBase payload) throws OpenMathException {
        println("# Received Request:");
        println(payload.toPopcorn());
        String s;
        OpenMathBase answer = null;
        while (answer == null) {
            print("Your Response: ");
            try {
                s = consoleReader.readLine();
                answer = OpenMathBase.parse(s);
            } catch (Exception ex) {
                println("Problem with input: "+ex.getMessage());
                answer = null;
            }
        }
        return answer;
    }

    private void println(String s) {
        System.out.println(s);
    }

    private void print(String s) {
        System.out.print(s);
    }
}
