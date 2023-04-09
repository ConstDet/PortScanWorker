package ru.hacker;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static final int MIN_PORTS_PER_THREAD = 20;
    public static final int MAX_THREADS = 0xFF;

    static InetAddress inetAddress;
    static List<Integer> allPorts;

    static List<Integer> allOpenPorts = new ArrayList<Integer>();
    static List<PortScanWorker> workers = new ArrayList<PortScanWorker>(MAX_THREADS);

    static Date startTime;
    static Date endTime;

    static void processArgs(String[] args) {
        if (args.length < 1) {
            usage();
            System.exit(1);
        }

        String host = args[0];
        try {
            inetAddress = InetAddress.getByName(host);
        } catch (IOException ioe) {
            System.out.println("Ошибка при подключении к хосту!");
            System.exit(2);
        }

        System.out.println("Сканирую хост " + host);

        int minPort = 0;
        int maxPort = 0x10000-1;

        if (args.length==2) {
            if (args[1].contains("-")) {
                // указан диапазон портов
                String[] ports = args[1].split("-");
                try {
                    minPort = Integer.parseInt(ports[0]);
                    maxPort = Integer.parseInt(ports[1]);
                } catch (NumberFormatException nfe) {
                    System.out.println("Неверные порты!");
                    System.exit(3);
                }
            } else {
                // one port pointed out
                try {
                    minPort = Integer.parseInt(args[1]);
                    maxPort = minPort;
                } catch (NumberFormatException nfe) {
                    System.out.println("Неверные порты!");
                    System.exit(3);
                }
            }
        }

        allPorts = new ArrayList<Integer>(maxPort-minPort+1);

        for (int i=minPort; i<=maxPort; i++) {
            allPorts.add(i);
        }
    }

    static void usage() {
        System.out.println("Использование сканера портов Java: ");
        System.out.println("порт основного хоста java");
        System.out.println("Пример:");
        System.out.println("java Main 192.168.1.1 1-1024");
        System.out.println("java Main 192.168.1.1 1099");
        System.out.println("java Main 192.168.1.1 (сканирует все порты из диапазона от 0 до 65535)");
    }
    public static void main(String[] args) {
        startTime = new Date();

        processArgs(args);

        if (allPorts.size() / MIN_PORTS_PER_THREAD > MAX_THREADS) {
            final int PORTS_PER_THREAD = allPorts.size()/MAX_THREADS;

            List<Integer> threadPorts = new ArrayList<>();
            for (int i=0,counter=0; i<allPorts.size();i++,counter++) {
                if (counter<PORTS_PER_THREAD) {
                    threadPorts.add(allPorts.get(i));
                } else {
                    PortScanWorker psw = new PortScanWorker();
                    psw.setInetAddress(inetAddress);
                    psw.setPorts(new ArrayList<>(threadPorts));
                    workers.add(psw);
                    threadPorts.clear();
                    counter=0;
                }
            }
            PortScanWorker psw = new PortScanWorker();
            psw.setInetAddress(inetAddress);
            psw.setPorts(new ArrayList<>(threadPorts));
            workers.add(psw);
        } else {
            List<Integer> threadPorts = new ArrayList<>();
            for (int i=0,counter=0; i<allPorts.size();i++,counter++) {
                if (counter<MIN_PORTS_PER_THREAD) {
                    threadPorts.add(allPorts.get(i));
                } else {
                    PortScanWorker psw = new PortScanWorker();
                    psw.setInetAddress(inetAddress);
                    psw.setPorts(new ArrayList<>(threadPorts));
                    workers.add(psw);
                    threadPorts.clear();
                    counter=0;
                }
            }
            PortScanWorker psw = new PortScanWorker();
            psw.setInetAddress(inetAddress);
            psw.setPorts(new ArrayList<>(threadPorts));
            workers.add(psw);
        }
        System.out.println("Порты для сканирования: "+allPorts.size());
        System.out.println("Нить для работы: "+workers.size());

        Runnable summarizer = () -> {
            System.out.println("Сканирование остановленно...");

            for (PortScanWorker psw : workers) {
                List<Integer> openPorts = psw.getOpenPorts();
                allOpenPorts.addAll(openPorts);
            }

            Collections.sort(allOpenPorts);

            System.out.println("Лист открытых портов:");
            for (Integer openedPort : allOpenPorts) {
                System.out.println(openedPort);
            }

            endTime = new Date();

            System.out.println("Время работы: " + (endTime.getTime() - startTime.getTime()) + " мсек");
        };

        CyclicBarrier barrier = new CyclicBarrier(workers.size(),summarizer);

        for (PortScanWorker psw : workers) {
            psw.setBarrier(barrier);
        }

        System.out.println("Начало сканирования...");

        for (PortScanWorker psw : workers) {
            new Thread(psw).start();
        }
    }
}