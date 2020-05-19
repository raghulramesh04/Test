package com.infosys.infytel.customer.controller;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import com.infosys.infytel.customer.dto.CustomerDTO;
import com.infosys.infytel.customer.dto.LoginDTO;
import com.infosys.infytel.customer.dto.PlanDTO;
import com.infosys.infytel.customer.service.CustHystrixService;
import com.infosys.infytel.customer.service.CustomerService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@RestController
@CrossOrigin

public class CustomerController {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	CustomerService custService;
	
	@Autowired
    CustHystrixService hystService;

	// Create a new customer
	@PostMapping(value = "/customers",  consumes = MediaType.APPLICATION_JSON_VALUE)
	public void createCustomer(@RequestBody CustomerDTO custDTO) {
		logger.info("Creation request for customer {}", custDTO);
		custService.createCustomer(custDTO);
	}

	// Login
	@PostMapping(value = "/login",consumes = MediaType.APPLICATION_JSON_VALUE)
	public boolean login(@RequestBody LoginDTO loginDTO) {
		logger.info("Login request for customer {} with password {}", loginDTO.getPhoneNo(),loginDTO.getPassword());
		return custService.login(loginDTO);
	}

	// Fetches full profile of a specific customer
	
    @GetMapping(value = "/customers/{phoneNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CustomerDTO getCustomerProfile(@PathVariable Long phoneNo) {
        long overAllStart=System.currentTimeMillis();    
        logger.info("Profile request for customer {}", phoneNo);
        
        
        
        CustomerDTO custDTO= custService.getCustomerProfile(phoneNo);
        long planStart=System.currentTimeMillis();
        System.out.println(planStart);
        PlanDTO planDTO= hystService.getPlan(custDTO.getCurrentPlan().getPlanId());
        long planStop=System.currentTimeMillis();
        custDTO.setCurrentPlan(planDTO);
        
        @SuppressWarnings("unchecked")
        long friendStart=System.currentTimeMillis();
        List<Long> friends=hystService.getSpecificFriends(phoneNo);
        long friendStop=System.currentTimeMillis();
        custDTO.setFriendAndFamily(friends);
        long overAllStop=System.currentTimeMillis();
        
        System.out.println("total time for plan "+(planStop-planStart));
        System.out.println("total time for plan "+(friendStop-friendStart));
        System.out.println("Total Overall time for request"+(overAllStop-overAllStart));
        
        return custDTO;
        
    }
	
}