package lesson_02_02_广播;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSearcher {
    public static void main(String[] args) throws IOException{
        // 作为搜索方，让系统自动分配端口
        DatagramSocket ds = new DatagramSocket();

        // 构建一份发送的数据包
        String requestData = "神州";
        byte[] requestDataBytes = requestData.getBytes();
        DatagramPacket requestPacket = new DatagramPacket(requestDataBytes,
                requestDataBytes.length);
        requestPacket.setAddress(InetAddress.getLocalHost());
        requestPacket.setPort(20000);

        // 发送
        ds.send(requestPacket);

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

        // 完成
        System.out.println("UDPSearcher Finished.");
        ds.close();
    }
}
