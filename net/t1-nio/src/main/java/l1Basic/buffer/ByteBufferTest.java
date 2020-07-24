package l1Basic.buffer;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author: zxz
 * @date: 2020/7/24
 */
@Slf4j
public class ByteBufferTest {
    @Test
    public void test1() {
        ByteBuffer buffer= ByteBuffer.allocate(100);
        buffer.putChar('a');
        buffer.putInt(2);
        buffer.putLong(50000L);
        buffer.putShort((short) 2);
        buffer.putDouble(12.4);
        System.out.println(buffer.position());
        buffer.flip();
        System.out.println(buffer.getChar());
        System.out.println(buffer.getInt());
        System.out.println(buffer.getLong());
        System.out.println(buffer.getShort());
        System.out.println(buffer.getDouble());
    }

    @Test
    public void test2() {
        ByteBuffer byteBuffer= ByteBuffer.allocate(10);
        for(int i=0;i<byteBuffer.capacity();++i){
            byteBuffer.put((byte)i);
        }
        byteBuffer.position(2);
        byteBuffer.limit(8);
        java.nio.ByteBuffer resetBuffer = byteBuffer.slice();
        for(int i=0;i<resetBuffer.capacity();i++){
            byte anInt = resetBuffer.get();
            resetBuffer.put(i, (byte) (anInt*2));
        }

        byteBuffer.position(0);
        byteBuffer.limit(byteBuffer.capacity());
        while (byteBuffer.hasRemaining()){
            System.out.println(byteBuffer.get());
        }
    }

    public void test3() {
        ByteBuffer byteBuffer=ByteBuffer.allocate(10);
        for(int i=0;i<byteBuffer.capacity();i++){
            byteBuffer.put((byte)i);
        }
        ByteBuffer byteBuffer1 = byteBuffer.asReadOnlyBuffer();
        System.out.println(byteBuffer.getClass());
        System.out.println(byteBuffer1.getClass());
        byteBuffer1.flip();
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer1.position());
        for(int i=0;i<byteBuffer1.capacity();i++){
            System.out.println(byteBuffer1.get());
        }
    }

    public void test4() throws IOException {
        FileOutputStream fileOutputStream=new FileOutputStream("dome8write.txt");
        FileInputStream fileInputStream=new FileInputStream("dome8read.txt");

        FileChannel channelRead = fileInputStream.getChannel();
        FileChannel channelWrite = fileOutputStream.getChannel();

        ByteBuffer byteBuffer=ByteBuffer.allocateDirect(100);
        while (true){
            byteBuffer.clear();
            int readNumber = channelRead.read(byteBuffer);
            System.out.println(readNumber);
            if(readNumber==-1){
                break;
            }
            byteBuffer.flip();
            channelWrite.write(byteBuffer);
        }
        fileOutputStream.close();
        fileInputStream.close();
    }

    public void test5() {
        byte[] bytes=new byte[]{'a','b','c'};
        ByteBuffer byteBuffer=ByteBuffer.wrap(bytes);
        bytes[0]='b';
        byteBuffer.put(2,(byte)'b');
        for(int i=0;i<byteBuffer.capacity();i++){
            System.out.println((char)byteBuffer.get());
        }
    }
}
