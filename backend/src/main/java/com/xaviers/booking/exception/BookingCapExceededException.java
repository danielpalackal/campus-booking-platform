package com.xaviers.booking.exception;

public class BookingCapExceededException extends RuntimeException {
    public BookingCapExceededException(String msg) { super(msg); }
}
