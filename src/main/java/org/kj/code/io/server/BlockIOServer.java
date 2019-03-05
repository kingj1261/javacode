/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package org.kj.code.io.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * Q1: 基于BIO实现的Server端，当建立了100个连接时，会有多少个线程？如果基于NIO，又会是多少个线程？ 为什么？
 *
 *
 *
 * 通过运行server和客户端可以得出结论
 *
 * 1.当服务器端不开线程分别处理客户端请求的情况下，虚拟机线程数为11个，其中10个为jvm虚拟机线程，1个为用户主线程
 *   这种情况下多个链接的读写IO是阻塞的
 *
 * 2.当服务器端对每个链接都开一个线程处理读写操作时，总线程数为111个，其中10个是jvm线程，1个为main主线程，100个为处理IO操作的线程，
 *   单个IO线程是阻塞的，每个线程只能等待当前IO是否有真实的读写操作，而不能分身去干别的事情
 *
 * 
 * @author mapingmp
 * @version $Id: BlockIOServer.java, v 0.1 2019年03月05日 4:56 PM mapingmp Exp $
 */
public class BlockIOServer {

    private volatile boolean stop = false;

    public void start(int coreThreadSize, int maxPoolSize) throws IOException {

        ServerSocket ss = new ServerSocket();
        ss.bind(new InetSocketAddress("localhost", 9090));

        System.out.println("server startup at (localhost,9090)");

        ThreadPoolExecutor worker = new ThreadPoolExecutor(coreThreadSize, maxPoolSize, 0,
            TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

        while (!stop) {
            Socket s = ss.accept();
            worker.execute(() -> doWork(s));
            worker.execute(() -> send(s));
            /*
                        readOnce(s);
                        sendOnce(s);*/
        }
    }

    public void send(Socket s) {
        while (true) {
            try {
                s.getOutputStream().write("hello client \n".getBytes());
                s.getOutputStream().flush();

                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendOnce(Socket s) {
        try {
            s.getOutputStream().write("hello client \n".getBytes());
            s.getOutputStream().flush();

            Thread.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doWork(Socket s) {
        try {
            InputStream in = s.getInputStream();
            byte[] buffer = new byte[1024];
            while (true) {
                int len = in.read(buffer);
                System.out.println("收到客户端信息:" + new String(buffer, 0, len));
                Thread.sleep(500);
            }
        } catch (Exception io) {
            io.printStackTrace();
        }
    }

    public void readOnce(Socket s) {
        try {
            InputStream in = s.getInputStream();
            byte[] buffer = new byte[1024];
            int len = in.read(buffer);
            System.out.println("收到客户端信息:" + new String(buffer, 0, len));
            Thread.sleep(500);
        } catch (Exception io) {
            io.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new BlockIOServer().start(10, 100);
    }

}