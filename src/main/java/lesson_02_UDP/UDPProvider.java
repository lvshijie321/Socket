
package lesson_02_UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.sql.SQLOutput;

/**
 * UDP 提供者，用于提供服务
 */
public class UDPProvider {
    public static void main(String[] args) throws IOException {
        System.out.println("UDPProvider Started");

        // 作为接收者，指定一个端口用于数据接收
        DatagramSocket ds = new DatagramSocket(20000);

        // 构建接收数据包
        final byte[] buf = new byte[512];
        System.out.println(buf.length);
        DatagramPacket receivePack = new DatagramPacket(buf, buf.length);

        // 接收（阻塞）
        ds.receive(receivePack);

        // 打印接收到的信息和发送者信息
        String ip = receivePack.getAddress().getHostAddress(); // 发送者的 IP 地址
        int port = receivePack.getPort(); // 发送者的端口
        int dataLen = receivePack.getLength(); // 1 个中文字符 3 字节，1 个英文字符 1 字节
        System.out.println(dataLen);
        String data = new String(receivePack.getData(), 0, dataLen);
        //System.out.println(receivePack.getData() instanceof byte[] );//true
        System.out.println("UDPProvider receive from ip: " + ip + "\tport: " + port + "\tdata: " + data);

        // 构建一份回送数据包
        String responseData = "Receive data with len：" + dataLen;
        byte[] responseDataBytes = responseData.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(responseDataBytes,
                responseDataBytes.length,
                receivePack.getAddress(),
                receivePack.getPort());

        // 发送
        ds.send(responsePacket);

        // 完成
        ds.close();
        System.out.println("UDPProvider Finished.");
    }
}