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

import org.openwebnet4j.BUSGateway;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.Thermoregulation;

class MainTest {

    public static void main(String[] args) throws FrameException {

        // create BUS gateway with IP=192.168.1.50 and password=12345
        BUSGateway myGateway = new BUSGateway("192.168.0.8", 20000, "183Blank");
        // myGateway.subscribe(this);
        try {
            myGateway.connect();

            // Test Thermoregulation request Status
            Response status = myGateway
                    .send(Thermoregulation.requestWriteFanCoilSpeed("1", Thermoregulation.FAN_COIL_SPEED.OFF));

            System.out.println(status.getFinalResponse());

            if (status.isSuccess()) {
                // List<OpenMessage> statusList = status.getResponseMessages();
                System.out.println("requestStatus Response: " + status.getResponseMessages().toString());
            }

            /*
             * 0 <*#4*1*0*0196##> Temperature not adjusted by local offset
             * 1 <*#4*1*12*0185*3##>, Temperature adjusted by local offset
             * 2 <*4*1*1##>, Mode: 0=Conditioning, 1=Heating, 101=Antifreeze, 202=Thermal Protection, 303=Generic OFF
             * 3 <*#4*1*14*0185*3##>, Set Point Temperature
             * 4 <*#4*1*13*00##>, Local Offset: 00-5
             * 5 <*#*1##>] ACK or NACK
             */

            // Request Temperature
            /*
             * Response currentTemperature = myGateway.send(Thermoregulation.requestTemperature("1"));
             * if (currentTemperature.isSuccess()) {
             * System.out
             * .println("requestTemperature Response: " + currentTemperature.getResponseMessages().toString());
             * }
             *
             * // Request Set Point Temperature
             * Response setPointTemperature = myGateway.send(Thermoregulation.requestSetPointTemperature("1"));
             * if (setPointTemperature.isSuccess()) {
             * System.out.println(
             * "requestSetPointTemperature Response: " + setPointTemperature.getResponseMessages().toString());
             * }
             */

            // Write Set Point Temperature to 18.5, Mode Heating
            /*
             * Response writeSetPointTemperature = myGateway
             * .send(Thermoregulation.requestWriteSetpointTemperature("1", Double.parseDouble("19.7"), "1"));
             * if (writeSetPointTemperature.isSuccess()) {
             * System.out.println("requestWriteSetpointTemperature Response: "
             * + writeSetPointTemperature.getResponseMessages().toString());
             * }
             */

            // Request new Set Point Temperature
            /*
             * Response newSetPointTemperature = myGateway.send(Thermoregulation.requestSetPointTemperature("1"));
             * if (newSetPointTemperature.isSuccess()) {
             * System.out.println("requestSetPointTemperature Response: "
             * + newSetPointTemperature.getResponseMessages().toString());
             * }
             */

            // Request Valves Status
            /*
             * Response requestValvesStatus = myGateway.send(Thermoregulation.requestValvesStatus("1"));
             * if (requestValvesStatus.isSuccess()) {
             * System.out.println(
             * "requestValvesStatus Response: " + requestValvesStatus.getResponseMessages().toString());
             * }
             */

        } catch (OWNException e) {

        }
    }
}