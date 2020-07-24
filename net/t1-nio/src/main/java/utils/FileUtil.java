package utils;

import java.io.*;

public class FileUtil {

    public static boolean writeFile(InputStream inputStream, OutputStream outputStream){
        boolean success = false;
        BufferedInputStream bufferedInputStream;
        BufferedOutputStream bufferedOutputStream;
        try {
            bufferedInputStream = new BufferedInputStream(inputStream);
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(HttpUtil.getHttpResponseContext200("").getBytes());
            int count = 0;
            while(count == 0){
                count = inputStream.available();
            }
            int fileSize = inputStream.available();
            long written = 0;
            int beteSize = 1024;
            byte[] bytes = new byte[beteSize];
            while(written < fileSize){
                if(written+beteSize > fileSize){
                    beteSize = (int)(fileSize-written);
                    bytes = new byte[beteSize];
                }
                bufferedInputStream.read(bytes);
                bufferedOutputStream.write(bytes);
                bufferedOutputStream.flush();
                written+=beteSize;
            }
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public static boolean writeFile(File file,OutputStream outputStream) throws Exception {
        return writeFile(new FileInputStream(file),outputStream);
    }

    public static String getResoucePath(String path){
        String resource = FileUtil.class.getResource("/").getPath();
        return resource + "\\" +path;
    }
}
