package com.adintellig.ella.hbase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.adintellig.ella.derby.DBManager;
import com.adintellig.ella.derby.model.RegionRequestCount;
import com.adintellig.ella.derby.model.RequestDAO;
import com.adintellig.ella.hbase.beans.MasterServiceBean;
import com.adintellig.ella.hbase.beans.MasterServiceBeans;
import com.adintellig.ella.hbase.beans.RegionServer;
import com.adintellig.ella.hbase.beans.RegionsLoad;
import com.alibaba.fastjson.JSON;

public class JMXHMasterService {

	public static String url = "http://hadoop-node-20:60010/jmx?qry=hadoop:service=Master,name=Master";

	private DBManager dbm = null;
	private RequestDAO rdao = null;
	private int maxItemNumber = 0;

	public JMXHMasterService() {
		this.dbm = new DBManager();
		this.rdao = dbm.getRequestDAO();
		this.maxItemNumber = rdao.getMaxItemNumber();
	}

	public String request(String urlString) {
		URL url = null;
		BufferedReader in = null;
		StringBuffer sb = new StringBuffer();
		try {
			url = new URL(urlString);
			in = new BufferedReader(new InputStreamReader(url.openStream(),
					"UTF-8"));
			String str = null;
			while ((str = in.readLine()) != null) {
				sb.append(str);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return sb.toString();
	}

	public MasterServiceBeans parseBean(String jsonString) {
		MasterServiceBeans bean = null;
		if (null != jsonString && jsonString.trim().length() > 0)
			bean = JSON.parseObject(jsonString, MasterServiceBeans.class);
		return bean;
	}

	public static void main(String[] args) throws JsonParseException,
			JsonMappingException, IOException {
		long st = System.currentTimeMillis();
		JMXHMasterService service = new JMXHMasterService();

		String result = service.request(url);
		MasterServiceBeans bean = service.parseBean(result);

		List<RegionRequestCount> list = RequestPopulator.populate(bean);
		for (RegionRequestCount req : list) {
			System.out.println(req);
		}
		long en = System.currentTimeMillis();
		System.out.println((en - st) );
	}
}