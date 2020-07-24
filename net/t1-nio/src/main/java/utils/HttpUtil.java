package utils;

public class HttpUtil {

    public static String getHttpResponseContext(int code,String content,String errorMsg){
        if (code==200) {
            return "HTTP/1.1 200 OK \n" +
                    "Content-Type: text/html;Charset=utf-8\n" +
                    "\r\n" + content;
        }else if(code == 500){
            return "HTTP/1.1 500 Internal Error="+errorMsg+" \n" +
                    "Content-Type: text/html\n" +
                    "\r\n";
        }
        return "HTTP/1.1 404 NOT Found \n" +
                "Content-Type: text/html\n" +
                "\r\n" +
                "<h1>404 not found</h1>";
    }

    public static String getHttpResponseContext404(){
        return getHttpResponseContext(404,null,null);
    }

    public static String getHttpResponseContext200(String content){
        return getHttpResponseContext(200,content,null);
    }

    public static String getHttpResponseContext500(String errorMsg){
        return getHttpResponseContext(500,null,errorMsg);
    }

}
