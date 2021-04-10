package me.grishka.houseclub.api.methods;

import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIRequest;
import me.grishka.houseclub.api.model.User;

public class StartPhoneNumberAuth extends ClubhouseAPIRequest<StartPhoneNumberAuth.Response>{
	public StartPhoneNumberAuth(String phoneNumber){
		super("POST", "start_phone_number_auth", Response.class);
		requestBody=new Body(phoneNumber);
	}

	public static class Response{
		public boolean success;
		public boolean is_blocked;
		public String error_message;
	}

	private static class Body{
		public String phoneNumber;

		public Body(String phoneNumber){
			this.phoneNumber=phoneNumber;
		}
	}
}
