package com.radiorunt.utilities;

public class TokenExpiredException extends Exception {
	private static final long serialVersionUID = 1L;

	public TokenExpiredException(String message) {
		super(message);
	}
}