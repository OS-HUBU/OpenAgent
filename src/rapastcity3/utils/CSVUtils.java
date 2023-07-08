package rapastcity3.utils;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import rapastcity3.pojo.GeoJson;

public class CSVUtils {

	public static void WriteCsv(String savePath,JSONObject jsonObject)
	{
		String csvSplitBy=",";
		try(FileWriter fw=new FileWriter(savePath))
		{
			String unit=jsonObject.getString("unit");
			String agentNumber=jsonObject.getString("agent");
			String infectedagentLatentCount=jsonObject.getString("infectedagentLatentCount");
			String stepflag=jsonObject.getString("stepflag");
			String infectedagentCount=jsonObject.getString("infectedagentCount");
			String deathNumber=jsonObject.getString("infectedagentCount");
			String infectedagentSevereCount=jsonObject.getString("infectedagentSevereCount");
			String infectedagentOnsetCount=jsonObject.getString("infectedagentOnsetCount");
			List<GeoJson> geojsonList=(List<GeoJson>) jsonObject.get("geojsonList");
			if(stepflag=="true"||FlagClazz.recordFlag==false)
			{
				fw.append("unit");
				fw.append(csvSplitBy);
				System.out.println(fw.toString());
				fw.append("agentNumber");
				fw.append(csvSplitBy);
				fw.append("infectedagentLatentCount");
				fw.append(csvSplitBy);
				fw.append("stepflag");
				fw.append(csvSplitBy);
				fw.append("infectedagentCount");
				fw.append(csvSplitBy);
				fw.append("deathNumber");
				fw.append(csvSplitBy);
				fw.append("infectedagentSevereCount");
				fw.append(csvSplitBy);
				fw.append("infectedagentOnsetCount");
				fw.append(csvSplitBy);
				fw.append("id");
				fw.append(csvSplitBy);
				fw.append("AgentType");
				fw.append(csvSplitBy);
				fw.append("lat");
				fw.append(csvSplitBy);
				fw.append("lng");
				fw.append(csvSplitBy);
				fw.append("masks");
				fw.append(csvSplitBy);
				fw.append("speed");
				fw.append(csvSplitBy);
				fw.append("stage");
				fw.append(csvSplitBy);
				fw.append("vacce");
				fw.append(csvSplitBy);
				fw.append("\n");
				for(GeoJson geoJson:geojsonList)
				{
					//full context
					fw.append(unit);
					fw.append(csvSplitBy);
					fw.append(agentNumber);
					fw.append(csvSplitBy);
					fw.append(infectedagentLatentCount);
					fw.append(csvSplitBy);
					fw.append(stepflag);
					fw.append(csvSplitBy);
					fw.append(infectedagentCount);
					fw.append(csvSplitBy);
					fw.append(deathNumber);
					fw.append(csvSplitBy);
					fw.append(infectedagentSevereCount);
					fw.append(csvSplitBy);
					fw.append(infectedagentOnsetCount);
					fw.append(csvSplitBy);
					fw.append(String.valueOf(geoJson.getId()));
					fw.append(csvSplitBy);
					fw.append(geoJson.getAgentType());
					fw.append(csvSplitBy);
					fw.append(geoJson.getLat());
					fw.append(csvSplitBy);
					fw.append(geoJson.getLng());
					fw.append(csvSplitBy);
					fw.append(geoJson.getMasks());
					fw.append(csvSplitBy);
					fw.append(String.valueOf(geoJson.getSpeed()));
					fw.append(csvSplitBy);
					fw.append(geoJson.getStage());
					fw.append(csvSplitBy);
					fw.append(geoJson.getVaccine());
					fw.append(csvSplitBy);
					fw.append("\n");
				}
			}else {
				FlagClazz.recordFlag=true;
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
}
