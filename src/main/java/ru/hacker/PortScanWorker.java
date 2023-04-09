package ru.hacker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class PortScanWorker  implements Runnable {
    static int globalId = 1;

    private int id;
    private List<Integer> ports;
    private List<Integer> openPorts;
    private InetAddress inetAddress;
    private int timeout = 200;
    CyclicBarrier barrier;

    public PortScanWorker() {
        this.id = globalId++;
    }

    public int getId() {
        return id;
    }

    public void setBarrier(CyclicBarrier cyclicBarrier) {
        this.barrier = cyclicBarrier;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public List<Integer> getOpenPorts() {
        return openPorts;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }
    @Override
    public void run() {
        System.out.printf("Открыли тред с id: %d\n", id);
        scan(inetAddress);
        try {
            barrier.await();
        } catch (InterruptedException e) {
            return;
        } catch (BrokenBarrierException e) {
            return;
        }
    }

    void scan(InetAddress inetAddress) {
        openPorts = new ArrayList<Integer>();
        System.out.println("Сканирую порты: ");
        for (Integer port : ports) {
            System.out.print(port);
            try {
                InetSocketAddress iSA = new InetSocketAddress(inetAddress, port);
                Socket socket = new Socket();
                socket.connect(iSA, timeout);
                System.out.printf("Найден открытый порт %d\n", port);
                openPorts.add(port);
                socket.close();
            } catch (IOException e) {

            }
        }
        System.out.printf("Финиш, id = %d\n", id);
    }
}
