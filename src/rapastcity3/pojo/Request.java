package rapastcity3.pojo;

import java.util.HashMap;

import com.alibaba.fastjson.JSONObject;

public class Request {
	//请求类型
	private String type;
	//请求主机
	private String host;
	//请求数据
	private JSONObject messageJson;
	//其他
	private HashMap<String,Object> messageMap=new HashMap<String,Object>();
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public JSONObject getMessageJson() {
		return messageJson;
	}
	public void setMessageJson(JSONObject messageJson) {
		this.messageJson = messageJson;
	}
	public HashMap<String, Object> getMessageMap() {
		return messageMap;
	}
	public void setMessageMap(HashMap<String, Object> messageMap) {
		this.messageMap = messageMap;
	}
	

}
