package rapastcity3.utils;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSONObject;

/**
 * @ProjectName: ChatSocket
 * @Package: com.hzw.utils
 * @ClassName: OutputJsonUtils
 * @Author: Lemon
 * @Description:输出JSON工具类
 * @Date: 2022-11-19 0:14
 * @Version: 1.0
 */
public class OutputJsonUtils {
    public static void OutputSuccessJson(Socket client) throws Exception{
    	JSONObject jsonObject=new JSONObject();
    	jsonObject.put("code", 200);
    	jsonObject.put("msg", "success");
    	String jsonString=jsonObject.toString();
        BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));
        int length = jsonString.getBytes(StandardCharsets.UTF_8).length;
        writer.write("HTTP/1.1 200 OK\r\n");
        writer.write("Connection: close\r\n");
        writer.write("Content-Type: application/json\r\n");
        writer.write("Content-Length: " + length + "\r\n");
        writer.write("\r\n"); // 空行标识Header和Body的分隔
        writer.write(jsonString.toString());
        writer.flush();
    }
    public static void OutputDataJson(Socket client,JSONObject jsonObject) throws Exception{
    	String jsonString=jsonObject.toString();
    	BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));
        int length = jsonString.getBytes(StandardCharsets.UTF_8).length;
        writer.write("HTTP/1.1 200 OK\r\n");
        writer.write("Connection: close\r\n");
        writer.write("Content-Type: application/json\r\n");
        writer.write("Content-Length: " + length + "\r\n");
        writer.write("\r\n"); // 空行标识Header和Body的分隔
        writer.write(jsonString.toString());
        writer.flush();
    }
}
