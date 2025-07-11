package com.nayonikaeyecare.api;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class StreamLambdaHandler implements RequestStreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(StreamLambdaHandler.class);
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(ApiApplication.class);
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
        logger.info("context: {}", context.toString());

        // Log the raw input stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > -1 ) {
            baos.write(buffer, 0, len);
        }
        baos.flush();
        String rawEventPayload = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        logger.info("Raw event payload: {}", rawEventPayload);

        // Re-create the input stream for the handler
        InputStream newInputStream = new ByteArrayInputStream(baos.toByteArray());
        
        // Log the new input stream and output stream for completeness, though less critical now
        logger.info("inputStream (after logging): {}", newInputStream.toString());
        logger.info("outputStream: {}", outputStream.toString());

        handler.proxyStream(newInputStream, outputStream, context);
    }
}
 