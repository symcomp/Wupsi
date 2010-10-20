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

package org.symcomp.wupsi.handlers;
import org.symcomp.scscp.SCSCPClient;

/**
 * WupsiHandlers are used to encapsulate the functionality of the command line
 * commands of the WUPSI. These are the commands that are not evaluated by the
 * remote SCSCP systems.
 */
public abstract class WupsiHandler {

    /**
     * If the beginning of the current command mathes this string, this is the
     * correct handler to be called.
     * @return the command to match for
     */
    public abstract String command();

    /**
     * this method is called with the entered command by Wupsifer
     * @param in the entered command
     */
    public abstract void handle(String in);

    /**
     * some usage infos
     * @return usage info for this command (family)
     */
    public abstract String help();

    /**
     * called on shutdown so that the handler can do necessary cleanup actions
     */
    public void shutdownHook() {};

	/**
	 * called when a system dies so that the handler can do necessary cleanup actions
	 */
	public void systemDiedHook(SCSCPClient client) {};

}
