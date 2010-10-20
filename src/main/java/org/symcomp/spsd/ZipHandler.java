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
 * zips two lists
 */
public class ZipHandler extends AbstractSPSDHandler {

    public ZipHandler() {
        super();
    }

    @Override
    public OMSymbol getServiceName() {
        return new OMSymbol("spsd", "zip");
    }

    public String getServiceNameStr() { return "zip"; }
    public String getDescription(OMSymbol oms) { return "parallel zip"; }


	//Computation data
	protected int numTasks;
	protected OpenMathBase[] operands1;
	protected OpenMathBase[] operands2;
	protected OpenMathBase function;

	//THINGS
	public void initSPSDComputation(OpenMathBase omoo)
	throws OpenMathException {
        // params[0] -- list1.list containing the objects of the first alist
        // params[1] -- list1.list containing the objects of the second list
        // params[2] -- the function. May be an OMS or an OMBIND or whatever
        OpenMathBase omo = omoo.deOMObject();
        OpenMathBase[] params = ((OMApply) omo).getParams();

		if (!(params[0].isApplication("list1", "list") && params[1].isApplication("list1", "list"))) {
			throw new OpenMathException("spsd.zip: First and second argument should be a list1.list");
		}

        this.operands1 = ((OMApply) params[0]).getParams();
        this.operands2 = ((OMApply) params[1]).getParams();
        this.function = params[2];
        this.numTasks = operands1.length;

		if (this.operands1.length != this.operands2.length) {
			throw new OpenMathException("spsd.zip: First and second argument should have the same length");
		}
	}

	public int getSPSDNumTasks() {
		return this.numTasks;
	}
	public OpenMathBase getSPSDTask(int i) {
        OpenMathBase tas = new OMApply(function, new OpenMathBase[]{operands1[i], operands2[i]});
        if (!function.isSymbol()) {
            tas = new OMSymbol("fns1", "identity").apply(new OpenMathBase[]{tas});
        }
		return tas;
	}
}
