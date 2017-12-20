package com.tahona.utils.di;

public class CreateInstanceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CreateInstanceException(String message, final Exception e) {
		super(message, e);
	}
	

}
