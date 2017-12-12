package com.samyuktatech.service;

import java.util.Date;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.samyuktatech.comman.model.UserFriend;
import com.samyuktatech.comman.util.RestUtil;


@RestController
@RequestMapping("/")
public class FriendService {
	
	@Value("${mysqlService.host}")
	private String mysqlServiceHost;
	
	@Autowired
	private RestTemplate restTemplate;
	
	/**
	 * Send request to other User
	 * 
	 * @param userId
	 * @param friendId
	 * @return
	 */
	@PostMapping("/send-request/{userId}/{friendId}")
	public ResponseEntity<?> sendRequest(
			@PathVariable("userId") Long userId,
			@PathVariable("friendId") Long friendId) {
		
		// Prepare UserFriend object
		UserFriend userFriend = new UserFriend();
		userFriend.setUserId(userId);
		userFriend.setFriendId(friendId);
		userFriend.setRequestSent(true);
		userFriend.setRequestSentDate(new Date());
		
		HttpEntity<UserFriend> httpEntity = RestUtil.getHttpEntityJson(userFriend);			
	    // Save UserFriend into Mysql database
	    ResponseEntity<UserFriend> resp = restTemplate.postForEntity(mysqlServiceHost + "/user/friend", httpEntity, UserFriend.class);		    
	    if (resp.getStatusCode() == HttpStatus.CREATED) {
	    	// TODO Save User into Elasticsearch
	    	
	    	return ResponseEntity.ok("Request send successfully");
	    }
		
	    return new ResponseEntity<>(HttpStatus.NO_CONTENT);		
	}
	
	@PostMapping("/accept-request/{userId}/{friendId}")
	public ResponseEntity<?> acceptRequest(
			@PathVariable("userId") Long userId,
			@PathVariable("friendId") Long friendId) {
		
		// Get UserFriend by UserId and FriendId 			
		ResponseEntity<UserFriend> resp = restTemplate.getForEntity(
				mysqlServiceHost + "/user/friend/" + userId + "/" + friendId, UserFriend.class);
		if (resp.getStatusCode() == HttpStatus.OK) {
			
			UserFriend userFriend = resp.getBody();
			
			// Check if request has been sent
			if (!userFriend.isRequestSent()) {
				return ResponseEntity.badRequest().body("This Request has not been sent");
			}
			
			// Check if request is already accepted
			if (userFriend.isRequestAccepted()) {
				return ResponseEntity.badRequest().body("This Request has already been accepted");
			}
			
			// Update and save UserFriend
			userFriend.setRequestAccepted(true);
			userFriend.setRequestAcceptedDate(new Date());
			
			// Update UserFriend back into Mysql database
		    ResponseEntity<UserFriend> resp1 = restTemplate.postForEntity(mysqlServiceHost + "/user/friend", 
		    		RestUtil.getHttpEntityJson(userFriend), UserFriend.class);		    
		    if (resp1.getStatusCode() == HttpStatus.CREATED) {		    	
		    	return ResponseEntity.ok("Request accepted successfully");
		    }
		}		
		
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);	
	}
			

}
