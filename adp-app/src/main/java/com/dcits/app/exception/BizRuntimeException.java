package com.dcits.app.exception;

@SuppressWarnings("serial")
public class BizRuntimeException extends RuntimeException {

	public BizRuntimeException() {
		super();
	}

	public BizRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public BizRuntimeException(String message) {
		super(message);
	}

	public BizRuntimeException(Throwable cause) {
		super(cause);
	}

}