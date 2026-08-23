package com.xaviers.booking.exception;

public class UnauthorizedRoleException extends RuntimeException {
    public UnauthorizedRoleException(String msg) { super(msg); }
}
