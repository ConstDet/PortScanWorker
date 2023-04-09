package ru.hacker;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class PortScanWorker  implements Runnable {
    static int globalId = 1;

    private int id;
    private List<Integer> ports;
    private List<Integer> openPrts;
    private InetAddress inetAddress;
    private int timeout = 200;
    CyclicBarrier barrier;
    @Override
    public void run() {
        scan(InetAddress);
    }

    void scan(InetAddress inetAddress) {
        openPrts = new ArrayList<Integer>();
        System.out.println("Сканирую порты: ");
        for (Integer port : ports) {
            System.out.print(port);
            try {
                InetSocketAddress iSA = new InetSocketAddress(inetAddress, port);
                Socket socket = new Socket();

            }
        }
    }
}
