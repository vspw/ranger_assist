package com.hwx.ranger;

import java.io.File;
import java.util.Scanner;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RangerAssistScheduler {
	protected static final Logger logger = LoggerFactory.getLogger(RangerAssistScheduler.class);
	public static void main(String[] args) {
		
		try
		{

		JsonUtils objJutils = new JsonUtils();
		logger.info("###############LETS BEGIN###############");
		logger.info("Make sure pass the input json file path with all necessary details");
		Scanner objScanner=new Scanner(new File(args[0]));
		String strJsonInput = objScanner.useDelimiter("\\Z").next();
		objScanner.close();
		logger.info("Parsing the json input file");
		Response objInput = objJutils.parseJSON(strJsonInput);
		logger.info("fromInputJson->Environment: "+ objInput.getEnvDetails());

		Timer timer = new Timer();
		timer.schedule(new RangerAssist(objInput), 0, objInput.getEnvDetails().getRepeatPeriod()*1000);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}
}
