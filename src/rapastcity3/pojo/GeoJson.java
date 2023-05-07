package rapastcity3.pojo;

//GeoJsonʵ����
public class GeoJson {
private int id;
private String lng;
private String lat;
private String AgentType;
private String Masks;
private String Vaccine;
private String Stage;
private Double Speed;


public Double getSpeed() {
	return Speed;
}
public void setSpeed(Double speed) {
	Speed = speed;
}
public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public String getLng() {
	return lng;
}
public void setLng(String lng) {
	this.lng = lng;
}
public String getLat() {
	return lat;
}
public void setLat(String lat) {
	this.lat = lat;
}
public String getAgentType() {
	return AgentType;
}
public void setAgentType(String AgentType) {
	this.AgentType = AgentType;
}
public String getMasks() {
    return this.Masks;
}

public void setMasks(String Masks) {
    this.Masks = Masks;
}

public String getVaccine() {
    return this.Vaccine;
}

public void setVaccine(String Vaccine) {
    this.Vaccine = Vaccine;
}
public String getStage() {
	return Stage;
}
public void setStage(String Stage) {
	this.Stage = Stage;
}

}