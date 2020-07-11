/**
 * Copyright (c) 2020 Contributors to the openwebnet4j project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.openwebnet4j.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import org.openwebnet4j.message.OpenMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that wraps input/output streams to send and receive frames from a OpenWebNet gateway
 *
 * @author M. Valla - Initial contribution
 */
public class FrameChannel {

    protected Queue<String> readFrames = new LinkedList<>(); // the list of frames already read from InputStream

    private OutputStream out;
    private InputStream in;
    private String name;

    private final Logger logger = LoggerFactory.getLogger(FrameChannel.class);

    public FrameChannel(InputStream in, OutputStream out, String name) {
        this.name = name;
        this.out = out;
        this.in = in;
    }

    protected String getName() {
        return name;
    }

    /**
     * Sends a frame on the channel
     *
     * @param frame the frame
     * @throws IOException in case of problems while writing on the OutputStream
     */
    protected synchronized void sendFrame(String frame) throws IOException {
        out.write(frame.getBytes());
        out.flush();
        logger.info("-FC-{} -------> {}", name, frame);
    }

    /**
     * Returns the first frame String from the {@link readFrames} queue. If queue is empty, tries to read available data
     * from InputStream and extract all frames terminated with "##" putting them in the queue.
     * If no new frame can be read from InputStream, returns null.
     *
     * @return the first frame from receiving queue, or the first new frame read from InputStream, or null
     * @throws IOException in case of problems while reading from InputStream
     */
    protected String readFrames() throws IOException {
        if (readFrames.isEmpty()) { // no frames in queue, try reading from stream
            byte[] buf = new byte[1024];
            int size = readUntilDelimiter(in, buf);
            if (size > 0) {
                String longFrame = new String(buf, 0, size);
                logger.trace("-FC-{}   <---   {}", name, longFrame);
                // this is a fix to handle an error of the gateway in the response to device info 2-UNITS
                if (longFrame.contains("#9*66*")) { // it's a response to device info
                    // perform another read to receive the 2-UNITS info
                    if ((size = readUntilDelimiter(in, buf)) != -1) {
                        String otherFrame = new String(buf, 0, size);
                        logger.trace("-FC-{}   <---   {}", name, otherFrame);
                        if (longFrame.regionMatches(0, otherFrame, 0, 12)) {
                            // frames refer to same ZigBee device: remove first ACK
                            logger.debug("-FC- BUGFIX!!! Removing ACK from device info response");
                            longFrame = longFrame.replace(OpenMessage.FRAME_ACK, "");
                        }
                        longFrame += otherFrame;
                    }
                }
                // end-of-fix

                if (longFrame.contains(OpenMessage.FRAME_END)) {
                    String[] frames = longFrame.split(OpenMessage.FRAME_END);
                    // add each single frame queue
                    for (String singleFrame : frames) {
                        readFrames.add(singleFrame + OpenMessage.FRAME_END);
                    }
                } else {
                    throw new IOException("Error in readFrameMulti(): no delimiter found on stream: " + longFrame);
                }
                logger.info("-FC-{} <------- {}", name, readFrames.toString());
                return readFrames.remove();
            } else {
                logger.debug("-FC-{} <------- NO DATA (size={})", name, size);
                return null;
            }
        } else {
            return readFrames.remove();
        }
    }

    /**
     * Reads from InputStream until delimiter, putting data into buffer
     *
     * @returns number of bytes read
     * @throws IOException in case of problems with the InputStream
     */
    private int readUntilDelimiter(InputStream is, byte[] buffer) throws IOException {
        logger.trace("-FC-{} Trying readUntilDelimiter...", name);
        int numBytes = 0;
        int cint = 0;
        char cchar = ' ';
        Boolean hashFound = false;
        // reads one char each cycle and stop when the sequence ends with ## (OpenWebNet delimiter)
        do {
            cint = is.read();
            if (cint == -1) {
                logger.debug("-FC-{} readUntilDelimiter() returned -1 (end of stream)", name);
                return numBytes;
            } else {
                buffer[numBytes++] = (byte) cint;
                cchar = (char) cint;
                if (cchar == '#' && hashFound == false) { // Found first #
                    hashFound = true;
                } else if (cchar == '#') { // Found second #, frame terminated correctly -> EXIT
                    break;
                } else if (cchar != '#') { // Append char and start again finding the first #
                    hashFound = false;
                }
            }
        } while (true);
        return numBytes;
    }

    protected void disconnect() {
        try {
            if (out != null) {
                out.close();
                out = null;
                logger.trace("-FC-{}-out closed...", name);
            }
            if (in != null) {
                in.close();
                in = null;
                logger.trace("-FC-{}-in closed...", name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("-FC-{} in/out streams CLOSED", name);
    }
}