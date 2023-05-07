package rapastcity3.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import rapastcity3.pojo.Request;

/**
 * @ProjectName: ChatSocket
 * @Package: com.hzw.utils
 * @ClassName: AnalysisRequest
 * @Author: Lemon
 * @Description: 解析请求类
 * @Date: 2022-11-18 23:42
 * @Version: 1.0
 */
public class AnalysisRequest {
    public static Request analysisRequestHandler(String requestString){
    	System.out.println(requestString);
        Request request=new Request();
        String[] requestArr=requestString.split("\n");
        //获取类型
        String type=requestArr[0].substring(0,requestArr[0].indexOf("/")).trim();
        request.setType(type);
        String host=requestArr[1].substring(requestArr[1].indexOf(":")+1).trim();
        request.setHost(host);
        int k=requestArr.length-1;
        JSONObject messageJson=JSON.parseObject(requestArr[k]);
        request.setMessageJson(messageJson);
        return request;
    }
}
