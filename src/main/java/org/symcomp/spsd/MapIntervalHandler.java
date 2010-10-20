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

import java.math.BigInteger;

/**
 * maps on an implicitely generated list by an interval.
 */
public class MapIntervalHandler extends AbstractSPSDHandler {

    public MapIntervalHandler() {
        super();
    }

	//Data about this handler
    @Override
    public OMSymbol getServiceName() {
        return new OMSymbol("spsd", "map_interval");
    }

    public String getServiceNameStr() { return "map_interval"; }
    public String getDescription(OMSymbol oms) { return "parallel map by integer interval"; }

	//Computation data
	protected BigInteger intFrom, intTo;
	protected OpenMathBase function;

	//THINGS
	public void initSPSDComputation(OpenMathBase omoo)
	throws OpenMathException {
        // params[0] -- integer2.interval containing the objects
        // params[1] -- the function. May be an OMS or an OMBIND or whatever
        OpenMathBase omo = omoo.deOMObject();
        OpenMathBase[] params = ((OMApply) omo).getParams();

		if (!(params[0].isApplication("interval1", "integer_interval"))) {
			throw new OpenMathException("spsd.map_interval: First argument should be an interval1.integer_interval");
		}
		OpenMathBase[] args = ((OMApply) params[0]).getParams();
		if (!(args[0].isInteger() && args[1].isInteger())) {
			throw new OpenMathException("spsd.map_interval: Not a correct interval");
		}
        this.intFrom = ((OMInteger) args[0]).getIntValue();
        this.intTo = ((OMInteger) args[1]).getIntValue();
		if (!(intTo.compareTo(intFrom) >= 0)) {
			throw new OpenMathException("spsd.map_interval: Not a correct interval");
		}

        this.function = params[1];
	}

	public int getSPSDNumTasks() {
		return intTo.subtract(intFrom).intValue() + 1;
	}
	public OpenMathBase getSPSDTask(int i) {
		BigInteger j = this.intFrom.add(BigInteger.valueOf(i));
        OpenMathBase tas = new OMApply(function, new OpenMathBase[]{ new OMInteger(j) });
        if (!function.isSymbol()) {
            tas = new OMSymbol("fns1", "identity").apply(new OpenMathBase[]{tas});
        }
		return tas;
	}

}
