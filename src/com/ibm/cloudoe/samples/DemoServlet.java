
package com.ibm.cloudoe.samples;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import com.ibm.ws.xs.json.java.JSONArray;
import com.ibm.ws.xs.json.java.JSONObject;

@MultipartConfig
public class DemoServlet extends HttpServlet {
	private static Logger logger = Logger.getLogger(DemoServlet.class.getName());
	private static final long serialVersionUID = 1L;

	private String serviceName = "text_to_speech";


	private String baseURL = "https://stream.watsonplatform.net/text-to-speech/api";
	private String username = "bd546fff-2e43-42c9-8c76-71075c257128";
	private String password = "AL7sK4TRu0T0";

  
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (req.getParameter("text") == null || req.getParameter("voice") == null) { 
			req.getRequestDispatcher("/index.jsp").forward(req, resp);
		} else {
			boolean download = false;
			if (req.getParameter("download") != null && req.getParameter("download").equalsIgnoreCase("true")) { 
				download = true;
			}

			req.setCharacterEncoding("UTF-8");
			try {
				String queryStr = req.getQueryString();
				String url = baseURL + "/v1/synthesize";
				if (queryStr != null) {
					url += "?" + queryStr;
				}
				URI uri = new URI(url).normalize();

				Request newReq = Request.Get(uri); 
				newReq.addHeader("Accept", "audio/ogg; codecs=opus"); 

				Executor executor = Executor.newInstance().auth(username, password);
				Response response = executor.execute(newReq);
				if (download)
				{
					resp.setHeader("content-disposition", "attachment; filename=transcript.ogg");
				}
				ServletOutputStream servletOutputStream = resp.getOutputStream();
				response.returnResponse().getEntity()
				.writeTo(servletOutputStream);
				servletOutputStream.flush();
				servletOutputStream.close();
			} catch (Exception e) {
				// Log something and return an error message
				logger.log(Level.SEVERE, "got error: " + e.getMessage(), e);
				resp.setStatus(HttpStatus.SC_BAD_GATEWAY);
			}
		}
	}

    /**
     * 取得环境变量
     */
	private JSONObject getVcapServices() {
		String envServices = System.getenv("VCAP_SERVICES");
		if (envServices == null) return null;
		JSONObject sysEnv = null;
		try {
			sysEnv = JSONObject.parse(envServices);
		} catch (IOException e) {
			// Do nothing, fall through to defaults
			logger.log(Level.SEVERE, "Error parsing VCAP_SERVICES: "+e.getMessage(), e);
		}
		return sysEnv;
	}

	@Override
	public void init() throws ServletException {
		super.init();
		processVCAP_Services();
	}
	

	private void processVCAP_Services() {
		logger.info("Processing VCAP_SERVICES");
		JSONObject sysEnv = getVcapServices();
		if (sysEnv == null) return;
		logger.info("Looking for: "+ serviceName );

		for (Object key : sysEnv.keySet()) {
			String keyString = (String) key;
			logger.info("found key: " + key);
			if (keyString.startsWith(serviceName)) {
				JSONArray services = (JSONArray)sysEnv.get(key);
				JSONObject service = (JSONObject)services.get(0);
				JSONObject credentials = (JSONObject)service.get("credentials");
				baseURL  = (String)credentials.get("url");
				username = (String)credentials.get("username");
				password = (String)credentials.get("password");
				logger.info("baseURL  = "+baseURL);
				logger.info("username = "+username);
				logger.info("password = "+password);
			} else {
				logger.info("Doesn't match /^"+serviceName+"/");
			}
		}
	}
}
