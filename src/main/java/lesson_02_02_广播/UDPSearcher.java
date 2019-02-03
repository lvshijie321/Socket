package lesson_02_02_广播;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * UDP 搜索者，用于搜索服务支持方
 */
public class UDPSearcher {
    private static final int LISTEN_PORT = 30000;

    public static void main(String[] args) throws IOException, InterruptedException {
        Listener listener = listen();
        sendBroadcast();

        // 读取任意键盘信息后可以退出
        System.in.read(); // 阻塞函数
        List<Device> devices = listener.getDevicesAndClose();
        for (Device device: devices) {
            System.out.println("Device：" + device.toString());
        }

        // 完成
        System.out.println("UDPSearcher Finished.");
    }

    private static Listener listen() throws InterruptedException {
        System.out.println("UDPSearcher start listen.");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, countDownLatch);
        listener.start();

        countDownLatch.await();
        return listener;
    }

    private static void sendBroadcast() throws IOException{
        System.out.println("UDPSearcher sendBroadcast started.");

        // 作为搜索方，让系统自动分配端口
        DatagramSocket ds = new DatagramSocket();

        // 构建一份发送的数据包
        String requestData = MessageCreator.buildWithPort(LISTEN_PORT);
        byte[] requestDataBytes = requestData.getBytes();
        DatagramPacket requestPacket = new DatagramPacket(requestDataBytes,
                requestDataBytes.length);

        // 设置端口和广播地址
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255")); // 广播地址
        requestPacket.setPort(20000);

        // 发送
        ds.send(requestPacket);

        // 完成
        System.out.println("UDPSearcher sendBroadcast Finished.");
        ds.close();
    }

    private static class Device {
        final int port;
        final String ip;
        final String sn;

        private Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", ip='" + ip + "\'" +
                    ", sn='" + sn + "\'" +
                    "}";
        }
    }

    private static class Listener extends Thread {


        private final int listenPort;

        public Listener(int listenPort, CountDownLatch countDownLatch) {
            super();
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        private final CountDownLatch countDownLatch;
        private final List<Device> devices = new ArrayList<Device>();
        private boolean done = false;
        private DatagramSocket ds = null;


        @Override
        public void run() {
            super.run();

            // 通知已启动
            countDownLatch.countDown();

            try {
                ds = new DatagramSocket(listenPort);

                while (!done) {
                    // 构建接收数据包
                    final byte[] buf = new byte[512];
                    DatagramPacket receivePack = new DatagramPacket(buf, buf.length);

                    // 接收
                    ds.receive(receivePack);

                    // 打印接收到的信息和发送者信息
                    String ip = receivePack.getAddress().getHostAddress(); // 发送者的 IP 地址
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    String data = new String(receivePack.getData(), 0, dataLen);
                    System.out.println("UDPSearcher receive from ip: " + ip + "\tport: " + port + "\tdata: " + data);

                    String sn = MessageCreator.parseSn(data);
                    if (sn != null) {
                        Device device = new Device(port, ip, sn);
                        devices.add(device);
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                close();
            }
            System.out.println("UDPSearcher listener finished");
        }

        private  void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        List<Device> getDevicesAndClose() {
            done = true;
            close();
            return devices;
        }
    }
}