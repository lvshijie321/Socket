
package lesson_02_02_广播;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * UDP 提供者，用于提供服务
 */
public class UDPProvider {
    public static void main(String[] args) throws IOException {
        // 生成一份唯一标示
        String sn = UUID.randomUUID().toString();
        // 构建线程
        Provider provider = new Provider(sn);
        // 启动线程
        provider.start();
        // 读取任意键盘信息后可以退出
        System.in.read(); // 阻塞函数
        provider.exit();
    }

    private static class Provider extends  Thread {
        private final String sn;
        private boolean done = false;
        private DatagramSocket ds = null;
        public Provider(String sn) {
            super();
            this.sn = sn;
        }

        @Override
        public void run() {
            super.run();

            try {
                // 监听 2000 端口
                ds = new DatagramSocket(20000);
                while (!done) {
                    System.out.println("UDPProvider Started");

                    // 构建接收数据包
                    final byte[] buf = new byte[512];
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

                    // 解析端口号
                    int responsePort = MessageCreator.parsePort(data);
                    if (responsePort != -1) {
                        // 构建一份回送数据包
                        String responseData = MessageCreator.buildWithSn(sn);
                        byte[] responseDataBytes = responseData.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(responseDataBytes,
                                responseDataBytes.length,
                                receivePack.getAddress(),
                                responsePort);

                        // 发送
                        ds.send(responsePacket);
                    }

                 }



            } catch (Exception ignored) {
            } finally {
                close();
            }
            // 完成
            System.out.println("UDPProvider Finished.");
        }

        private void close() {
            if (ds !=null) {
                ds.close();
                ds = null;
            }
        }

        void exit() {
            done = true; // 仅仅赋值 done = true 不会停止循环，因为循环体内正阻塞，必须调用 close 方法，是的发生异常，最后进入 finally
            close();
        }
    }
}