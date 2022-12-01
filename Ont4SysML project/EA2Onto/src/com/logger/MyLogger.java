package com.logger;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class MyLogger {
	public final static Logger LOGGER ;

    static{
    	PropertyConfigurator.configure("log4j.properties"); 
    	LOGGER=  Logger.getLogger(MyLogger. class );
    }
    
    static Logger getLogger(){
    	return LOGGER;
    }
}
