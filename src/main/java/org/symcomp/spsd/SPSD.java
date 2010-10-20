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

import org.symcomp.scscp.SCSCPClient;
import org.symcomp.scscp.ProcedureCall;
import org.symcomp.scscp.Computation;
import org.symcomp.openmath.*;
import org.symcomp.notification.NotificationCenter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;

/**
 * the 'Simple Parallel SCSCP Dispatcher' is responsible to parallely
 * dispatch work to some registered systems. It is derived from SCSCPClient
 * to allow for uniform access.
 */
public class SPSD extends SCSCPClient {

    private List<SCSCPClient> workers;
    private List<AbstractSPSDHandler> handlers;

    public SPSD(String uri, Integer port) {
        loglevel = 0;
        this.scscpUri = "direct-localhost";
        this.scscpPort = 0;
        computations = new HashMap<String,Computation>();
        waitingComputations = new ArrayBlockingQueue<Computation>(16383);
        this.serviceName = "SPSD";
        this.serviceVersion = "1.6-SNAPSHOT";
        this.serviceId = "SPSD";
        this.scscpVersion = "1.3";
        this.workers = new LinkedList();
        state = CLIENT_IDLE;
        resultThread = new Thread(this);
        resultThread.start();
        handlers = new LinkedList<AbstractSPSDHandler>();
        handlers.add(new AllPairsHandler());
        handlers.add(new MapHandler());
        handlers.add(new ZipHandler());
        handlers.add(new MapIntervalHandler());
    }

    public void addWorker(SCSCPClient worker) {
        if (!worker.getServiceName().equals("SPSD")) this.workers.add(worker);
    }

    public void addWorkers(Collection<SCSCPClient> workers) {
        for(SCSCPClient worker : workers)
            if (!worker.getServiceName().equals("SPSD")) this.workers.add(worker);
    }

    public List<SCSCPClient> getWorkers() {
        return workers;
    }

    public void clearWorkers() {
        workers.clear();
    }

	public boolean removeWorker(SCSCPClient c) {
		return workers.remove(c);
	}

    public int getWorkerCount() {
        return workers.size();
    }

    @Override
    public void run() {
            NotificationCenter.getNC().sendNotification(this, "RUNNING", null);
        // initialization
		log(1, "   # started waiting-for-result-thread ");
		// run-loop
        OpenMathBase result = null;
        try {
            while (true) {
                if (waitingComputations.size() == 0) this.state = CLIENT_IDLE;
                do {
                    currentComputation = ((ArrayBlockingQueue<Computation>) waitingComputations).poll(1, TimeUnit.SECONDS);
                } while (null == currentComputation);

                ProcedureCall pc = currentComputation.getProcedureCall();

                boolean handeled = false;
                for(AbstractSPSDHandler pch : handlers) {
                    if (pc.getServiceName().equals(pch.getServiceName())) {
                        pch.setWorkers(workers);

						try {
                        	result = pch.handlePayload(pc.getPayload());
						} catch (OpenMathException e) {
							result = (new OMSymbol("scscp1", "error_system_specific")).apply(
								new OpenMathBase[] { new OMString(e.getMessage()) });
						}
						currentComputation.finished(result.toOMObject());
                        handeled = true;
                        break;
                    }
                }
                if (!handeled) {
                    currentComputation.finished(OpenMathBase.parse("scscp1.error_system_specific!('Could not handle "+pc.getServiceName().toPopcorn()+"')"));
                }
                NotificationCenter.getNC().sendNotification(this, "ComputationFinished", null);
                currentComputation = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		// System.out.println("# SPSD run loop died -- This should not have happened.");
        NotificationCenter.getNC().sendNotification(this, "DEAD", null);
    } // run

}
