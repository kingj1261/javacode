/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package org.kj.code.io.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * io  client
 * 
 * @author mapingmp
 * @version $Id: BlockIOClient.java, v 0.1 2019年03月05日 6:10 PM mapingmp Exp $
 */
public class BlockIOClient {

    public void connect(String host, int port) throws IOException {
        Socket client = new Socket(host, port);

        Thread readWorker = new Thread(() -> {
            try {
                InputStream in = client.getInputStream();

                while (!client.isClosed()) {

                    byte[] buffer = new byte[2048];

                    int len = in.read(buffer);

                    while (true) {
                        System.out.println(Thread.currentThread().getName() + "["
                                           + client.hashCode() + "]" + "->response from server : "
                                           + new String(buffer, 0, len));
                    }
                }
            } catch (IOException io) {
                io.printStackTrace();
            }

        });

        Thread sendWorker = new Thread(() -> {
            try {
                OutputStream os = client.getOutputStream();
                while (!client.isClosed()) {
                    os.write("ping\n".getBytes());
                    os.flush();
                    Thread.sleep(300);
                    System.out.println(Thread.currentThread().getName() + "[" + client.hashCode()
                                       + "]" + "send ping success");
                }
            } catch (IOException io) {
                io.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        });

        readWorker.setName("Read");
        sendWorker.setName("Send");

        readWorker.start();
        sendWorker.start();
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 100; i++) {
            new BlockIOClient().connect("localhost", 9090);
        }
    }
}