package com.hwx.ranger;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonUtils {

	protected static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
	
    protected Response parseJSON(String jsonContent)  {
    	 Response inpObject=null;
        if ((jsonContent == null) || jsonContent.isEmpty()) {
          return (Response) Collections.emptyMap();
        } else {
          try {
        	inpObject = new Gson().fromJson(jsonContent, Response.class);
          } 
          catch (JsonSyntaxException e) {
        	  logger.error(e.getMessage());
          }
          return inpObject;
        }

      }
    
    protected HDFSListStatusResponse parseHDFSList(String jsonContent)  {
   	 HDFSListStatusResponse inpObject=null;
       if ((jsonContent == null) || jsonContent.isEmpty()) {
         return (HDFSListStatusResponse) Collections.emptyMap();
       } else {
         try {
       	inpObject = new Gson().fromJson(jsonContent, HDFSListStatusResponse.class);
         } 
         catch (JsonSyntaxException e) {
       	  logger.error(e.getMessage());
         }
         return inpObject;
       }

     }
    protected RangerPolicyResponse parseRangerPolicy(String jsonContent)  {
      	 RangerPolicyResponse inpObject=null;
          if ((jsonContent == null) || jsonContent.isEmpty()) {
            return (RangerPolicyResponse) Collections.emptyMap();
          } else {
            try {
          	inpObject = new Gson().fromJson(jsonContent, RangerPolicyResponse.class);
            } 
            catch (JsonSyntaxException e) {
          	  logger.error(e.getMessage());
            }
            return inpObject;
          }

        }
    protected String prettyPrint(String jsonContent)  {
   	 String prettyJsonString=null;
   	String data=null;
       if ((jsonContent == null) || jsonContent.isEmpty()) {
         return "EmptyJson";
       } else {
         try {
    		JsonParser jp = new JsonParser();
    		JsonElement je = jp.parse(jsonContent);
    		
    		if (je.isJsonObject())
    		{
    			JsonObject response = je.getAsJsonObject();
    			data = response.get("data").getAsString();
    		}
    		//prettyJsonString = gson.toJson(data);
    		prettyJsonString = data;
         } 
         catch (JsonSyntaxException e) {
       	  logger.error(e.getMessage());
         }
         return prettyJsonString;
       }

     }
    
}
