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

package org.symcomp.wupsi;

import org.kohsuke.args4j.Option;

import java.util.List;

/**
 * these options are set by the org.kohsuke.args4j.CmdLineParser 
 */
public class WupsiOptions {

    @Option(name="-c", aliases="--connect", metaVar = "url[:port] name", usage="connects to system running at specified url and port, assigning the given name")
    List<String> connects;

    @Option(name="-f", aliases="--output-format", metaVar = "<xml|popcorn|latex>", usage="sets the initial output format")
    String outputFormat;

    @Option(name="-i", aliases="--input", metaVar = "file", usage="read input from file instead of prompt" )
    String input;

    @Option(name="-o", aliases="--output", metaVar = "file", usage="write output to file instead of console" )
    String output;

    @Option(name="-Q", aliases="--quit-on-eof", usage="When reading from a file, quit when EOF is reached, otherwise switch to prompt" )
    boolean quit;

    @Option(name="-q", aliases="--quiet", usage="print only results and errors" )
    boolean quiet;

    @Option(name="-a", aliases="--atomic", usage="when reading from a file, treat the whole file as one input")
    boolean atomic;

    @Option(name="-w", aliases="--webserver", metaVar="port", usage="starts a webserver on the given port")
    Integer webserver;

    @Option(name="-s", aliases="--interactive-scscp-server", metaVar="port", usage="starts an SCSCP server on the given port, where you can handle incoming requests by hand. All other options are ignored.")
    Integer scscpserver;

    @Option(name="-d", aliases="--communication-directory", metaVar = "directory", usage="read in<x> files from the directory and write the result to out<x> files there" )
    String commDir;

}
