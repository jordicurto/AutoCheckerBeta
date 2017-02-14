package com.github.jordicurto.autochecker.data.exception;

public class NoLocationFoundException extends Exception {
	
	private static final String MESSAGE_TEXT = "This location doesn't exist: ";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NoLocationFoundException(int locationId) {
		super(MESSAGE_TEXT + "id = " + locationId);
	}
	
	public NoLocationFoundException(String locationName) {
		super(MESSAGE_TEXT + "name = " + locationName);
	}
}
