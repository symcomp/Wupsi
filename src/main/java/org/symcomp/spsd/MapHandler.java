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

package org.symcomp.spsd;

import org.symcomp.openmath.*;

/**
 * iterates over a list/set
 */
public class MapHandler extends AbstractSPSDHandler {

    public MapHandler() {
        super();
    }

	//Data about this handler
    @Override
    public OMSymbol getServiceName() {
        return new OMSymbol("spsd", "map");
    }

    public String getServiceNameStr() { return "map"; }
    public String getDescription(OMSymbol oms) { return "parallel map"; }

	//Computation data
	protected int numTasks;
	protected OpenMathBase[] operands;
	protected OpenMathBase function;

	//THINGS
	public void initSPSDComputation(OpenMathBase omoo)
	throws OpenMathException {
        // params[0] -- list1.list containing the objects of the first list
        // params[1] -- the function. May be an OMS or an OMBIND or whatever
        OpenMathBase omo = omoo.deOMObject();
        OpenMathBase[] params = ((OMApply) omo).getParams();

		if (!(params[0].isApplication("list1", "list"))) {
			throw new OpenMathException("spsd.map: First argument should be a list1.list");
		}

        this.operands = ((OMApply) params[0]).getParams();
        this.function = params[1];
        this.numTasks = operands.length;
	}

	public int getSPSDNumTasks() {
		return this.numTasks;
	}

	public OpenMathBase getSPSDTask(int i) {
        OpenMathBase tas = new OMApply(function, new OpenMathBase[]{operands[i]});
        if (!function.isSymbol()) {
            tas = new OMSymbol("fns1", "identity").apply(new OpenMathBase[]{tas});
        }
		return tas;
	}

}
