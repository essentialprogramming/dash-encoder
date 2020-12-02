package com.exception;

import com.util.exceptions.ServiceException;
import com.util.exceptions.ValidationException;
import com.web.json.JsonResponse;
import org.springframework.web.client.HttpClientErrorException;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

public class ExceptionHandler {

    @FunctionalInterface
    interface Strategy<T> {
        Response getValue(T exception);
    }

    private final static Strategy<ValidationException> validationExceptionStrategy = (exception) -> {
        return Response
                .status(exception.getHttpCode())
                .header("Content-Range", "bytes */" + exception.getMessage()) // Required in 416.
                .build();
    };

    private final static Strategy<ServiceException> serviceExceptionStrategy = (exception) -> {
        JsonResponse jsonResponse;
        jsonResponse = new JsonResponse()
                .with("Message", exception.getMessage())
                .with("Code", exception.getFailureCode())
                .done();
        return Response
                .status(Response.Status.NO_CONTENT)
                .entity(jsonResponse)
                .build();
    };

    private final static Strategy<Exception> defaultStrategy = (exception) -> {
        JsonResponse jsonResponse;
        jsonResponse = new JsonResponse()
                .with("message", "INTERNAL_SERVER_ERROR")
                .with("code", Response.Status.INTERNAL_SERVER_ERROR)
                .with("Exception", exception)
                .done();

        return Response
                .serverError()
                .entity(jsonResponse)
                .build();
    };

    private final static Map<Class, Strategy> strategiesMap = new HashMap<Class, Strategy>() {
        {
            put(ValidationException.class, validationExceptionStrategy);
            put(HttpClientErrorException.class, serviceExceptionStrategy);
        }
    };

    public static Response handleException(CompletionException completionException) {

        Strategy strategy = strategiesMap.getOrDefault(completionException.getCause().getClass(), defaultStrategy);
        return strategy.getValue(completionException.getCause());
    }

}
