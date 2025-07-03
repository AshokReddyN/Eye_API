package com.nayonikaeyecare.api;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamLambdaHandler implements RequestStreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(StreamLambdaHandler.class);
    private static SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(ApiApplication.class);
        } catch (ContainerInitializationException e) {
            // if we fail here. We re-throw the exception to force another cold start
            String errorMessage = "Could not initialize Spring Boot application";
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    public StreamLambdaHandler() {
        // Constructor is called once per container.
        // It's good practice to have a constructor, even if it's empty.
        logger.info("StreamLambdaHandler initialized");
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        logger.info("handleRequest");
        logger.info("inputStream: {}", inputStream.toString());
        logger.info("outputStream: {}", outputStream.toString());
        logger.info("context: {}", context.toString());
        handler.proxyStream(inputStream, outputStream, context);
    }
}
 