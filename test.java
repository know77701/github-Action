package walletCrawler.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class test {

 	private static final String url = "https://etherscan.io/address-tokenpage?m=normal&a=0x1938A448D105D26C40A52A1BFE99B8CA7A745AD0";
 	private static String fileName = "src/main/resources/" + getToday() + ".json";
	private static File file = new File(fileName);
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
    public static void main(String[] args) {
		try {
			getList();
			System.out.println(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.gc();
		}
	}
    
    public static void getList() throws IOException {
 		Document doc = Jsoup.connect(url).get();
 		getBody(doc);
 	}
 	
 	public static void getFile() throws IOException {
 		boolean isExists = file.exists(); 
 		if(!isExists) {
			file.createNewFile();
		}
 	}

 	public static JSONArray getFileRead(String fileName) {
 		JSONArray arr = new JSONArray();
		try {
			if(file.length() == 0) {
				return arr;
			} else {
			Reader re = new FileReader(fileName);
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(re);
			arr = (JSONArray)obj;
			re.close();
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return arr;
 	}
 	
 	public static JSONArray setJson(List<String> sb) {
 		JSONArray  arr = new JSONArray();
		for(int i=0; i < sb.size(); i++) {
			JSONObject json = new JSONObject();
			if(!sb.get(i).equals("")) {
				json.put("hash",sb.get(i++));
				sb.get(i++);
				sb.get(i++);
				sb.get(i++);
				json.put("inAndOut",sb.get(i++));
				sb.get(i++);
				json.put("value",sb.get(i++));
				json.put("token",sb.get(i++));
				
				arr.add(json);
			}
		}
 		return arr;
 	}
 	
 	public static String getToday() {
  		LocalDate now = LocalDate.now();
  		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
  		String format = now.format(formatter);
  		return format;
 	}

 	public static List<String> getHash(JSONArray arr){
 		List<String> list = new ArrayList<String>();
		for(int i =0; i < arr.size(); i++) {
			JSONObject jobj = (JSONObject) arr.get(i);
			list.add((String) jobj.get("hash"));
		}
 		
 		return list;
 	}
 	public static JSONArray getBody(Document doc) throws IOException {
 		Elements th = doc.select("table.table tbody tr");
		List<String> sb = new ArrayList<String>();
		
		for(Element el : th) {
			for(Element td : el.select("td")) {
				sb.add(td.text());
			}
		}
		
		getFile();
		JSONArray arr = setJson(sb);
		JSONArray compareArr = new JSONArray();
		for(int i =0; i < arr.size(); i++) {
			JSONObject obj = (JSONObject) arr.get(i);
			if(obj.get("inAndOut").equals("OUT")) {
				compareArr.add(arr.get(i));
			}
		}
		JSONArray list = getFileRead(fileName);
		
		List<String> compareList = getHash(compareArr);
		List<String> fileList = getHash(list);

		
		if(compareArr.size() > 0) {
			compareList.removeAll(fileList);
			if(compareList.size() > 0) {
				file.delete();
				for(int i =0; i < compareList.size(); i++) {
					JSONObject jobj = (JSONObject) compareArr.get(i);
					if(jobj.get("hash").equals(compareList.get(i))) {
						list.add(compareArr.get(i));s
					}
				}
				BufferedWriter fw = new BufferedWriter(new FileWriter(fileName,true));
				String wrjson = gson.toJson(list);
				fw.write(wrjson);
				fw.newLine();
				fw.flush();
				fw.close();
			}
		}
		return list;
	}

}
