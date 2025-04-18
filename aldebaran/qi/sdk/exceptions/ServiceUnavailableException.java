package com.aldebaran.qi.sdk.exceptions;

import static java.lang.String.format;

public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String serviceName) {
        super(format("Service %s is unavailable", serviceName));
    }

}

