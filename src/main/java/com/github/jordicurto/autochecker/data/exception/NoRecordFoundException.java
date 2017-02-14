package com.github.jordicurto.autochecker.data.exception;

public class NoRecordFoundException extends Exception {

	private static final String MESSAGE_TEXT = "No record found for this location : ";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NoRecordFoundException(int locationId) {
		super(MESSAGE_TEXT + "id = " + locationId);
	}
	
	public NoRecordFoundException(String locationName) {
		super(MESSAGE_TEXT + "name = " + locationName);
	}
}
