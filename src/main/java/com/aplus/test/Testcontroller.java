package com.aplus.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class Testcontroller {
	private static final Logger logger = LoggerFactory.getLogger(Testcontroller.class);
	
	@RequestMapping(value="/testup",method = RequestMethod.GET)
	public String test(MultipartFile file)  {
		logger.info("file="+file);
		return "admin/upload";
	}
	
	


}
