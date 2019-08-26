package org.drathveloper.exceptions;

import org.apache.http.HttpException;

public class HttpGenericException extends HttpException {

    public HttpGenericException(int code, String message){
        super(message, new Throwable(String.valueOf(code)));
    }

}
