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
import org.symcomp.scscp.ProcedureCallHandler;
import org.symcomp.scscp.SCSCPClient;
import org.symcomp.notification.NotificationReceiver;
import org.symcomp.notification.Notification;
import org.symcomp.notification.NotificationCenter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.HashMap;
import java.util.Collection;

/**
 * is a specialized ProcedureCallHandler prpared for distribution of
 * computations.
 */
public abstract class AbstractSPSDHandler extends ProcedureCallHandler implements NotificationReceiver {

    protected BlockingQueue<SCSCPClient> workers;
    protected OpenMathBase[] results;
    protected HashMap<String, Integer> positions;

    public OMSymbol getServiceName() {
        return new OMSymbol("spsd", getServiceNameStr());
    }

    public AbstractSPSDHandler() {
        workers = new ArrayBlockingQueue<SCSCPClient>(100);
        NotificationCenter.getNC().register(this, "ComputationFinished");
    }

    public int setWorkers(Collection<SCSCPClient> workers) {
        for (SCSCPClient s : workers) {
            this.workers.offer(s);
        }
        return workers.size();
    }

	//To be implemented by the actual handlers
	public abstract void initSPSDComputation(OpenMathBase omoo) throws OpenMathException;
	public abstract int getSPSDNumTasks();
	public abstract OpenMathBase getSPSDTask(int i);

	//Where THINGS are going on
    public synchronized OpenMathBase handlePayload(OpenMathBase omoo)
	throws OpenMathException {
        // check whether there are &uuml;berhaupt any workers
        if (0 == this.workers.size()) {
			throw new OpenMathException("SPSD: No systems given.");
        }
		int numworkers = this.workers.size();

		//Initialize computation
		this.initSPSDComputation(omoo);

		//Initialize results and position arrays
		int numTasks = getSPSDNumTasks();
		this.results = new OpenMathBase[numTasks];
        this.positions = new HashMap<String, Integer>();

		//Perforum the computation
        SCSCPClient client = null;
        for (int i = 0; i < numTasks; ++i) {
			//Get a client
            try {
                client = workers.take();
            } catch (InterruptedException ignored) { }

			//Start a computation
            assert client != null;
            String token = client.compute(getSPSDTask(i));

			//Put the token into the positions hashmap
			this.positions.put(token, i);
        }

		//We are done, but to ensure that we have consumed all results
		//we wait till all workers were available.
        for (int i = 1; i <= numworkers; i++) {
            try {
                workers.take();
            } catch (InterruptedException ignored) { }
        }

		//Now we are done, and we dare to return the result.
        return (new OMSymbol("list1", "list")).apply(results);
    }


    public void receiveNotification(Notification notification) {
        if (null == this.positions)
            return;

        if (notification.getMessage().equals("ComputationFinished")) {
            try {
				//Parse notification
                String token = (String) notification.getData().get("token");
                OpenMathBase result = (OpenMathBase) notification.getData().get("result");
                SCSCPClient client = (SCSCPClient) notification.getSender();

                //EVIL HACK, need to improve the Notification stuff
                if (client.getServiceName().equals("SPSD")) return;

				//Store result
                Integer p = (Integer) this.positions.get(token);
                if (null == p)
                    return;
                OpenMathBase res = result.deOMObject();
                synchronized (results) {
                    this.results[p] = res;
                }

				//Give worker back
                workers.offer(client);

            } catch(Exception ignored) {}
        } else {
            throw new RuntimeException("Unexpected Notification");
        }
    }
}
