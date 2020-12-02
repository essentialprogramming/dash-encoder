package com.util.exceptions;


import javax.ws.rs.core.Response;


public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final Response.Status httpCode;


	public ValidationException(String message, Response.Status statusCode) {
		super(message);
		this.httpCode = statusCode;
	}


	public ValidationException(String message, Response.Status statusCode, Throwable cause) {
		super(message, cause);
		this.httpCode = statusCode;
	}

	public Response.Status getHttpCode() {
		return httpCode;
	}

}
