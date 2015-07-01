/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.util.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class NetUtils {

	    /**
     * Attempts to get a free port starting at the passed in port and working up.
     *
     * @param startPort
     * @return
     */
	public static int getFreePort(final int startPort) {

	    int port = startPort;
	    while(!NetUtils.isPortFree(port)) port++;

	    return port;
	}

    /**
     * Attempts to get a free port by asking a ServerSocket to find one, and then closing that socket and returning the port it used.
     *
     * @return
     */
    public static int getFreePort() throws IOException {
        int port = 0;
        ServerSocket s = null;
        try {
            s = new ServerSocket(0);
            port = s.getLocalPort();

        } finally {
            if (s != null) {
                s.close();
            }
        }

        if (port < 0) {
            // The idea of this check is based on some code in PyDev which
            // implies that ServerSocket
            // can return -1 when a firewall configuration is causing a problem
            throw new IOException("Unable to obtain free port, is a firewall running?");
        }

        return port;
    }

	/**
	 * Checks if a port is free.
	 * @param port
	 * @return
	 */
	public static boolean isPortFree(int port) {

	    ServerSocket ss = null;
	    DatagramSocket ds = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        ds = new DatagramSocket(port);
	        ds.setReuseAddress(true);
	        return true;
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }

	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }

	    return false;
	}

}
