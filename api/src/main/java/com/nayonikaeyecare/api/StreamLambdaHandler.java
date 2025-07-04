package com.nayonikaeyecare.api;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
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
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            // For applications that take longer than 10 seconds to start,
            // increase the INIT_TIMEOUT environment variable.
            // This can be set in the AWS Lambda console or in the SAM template.
            // Default is 10 seconds.
            // String initTimeout = System.getenv("INIT_TIMEOUT");
            // long timeout = (initTimeout != null) ? Long.parseLong(initTimeout) : 10_000L;

            logger.info("Initializing Spring Boot application for Lambda");
            // For Spring Boot 2.x applications
            // handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(ApiApplication.class);

            // For Spring Boot 3.x applications, use the following:
            handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(ApiApplication.class);


            // If you are using Spring Boot 3.x with a different base path, you might need:
            // handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(ApiApplication.class, "/your-base-path");


            // If you want to launch the Spring Boot application asynchronously, you can do this:
            // handler.onStartup(servletContext -> {
            //     // Perform any ServletContext initialization if needed
            // });
            // handler.initialize();


            // If you need to register filters or servlets, you can do it here
            // For example, to register a filter:
            // handler.onStartup(servletContext -> {
            //     FilterRegistration.Dynamic registration = servletContext.addFilter("myFilter", MyFilter.class);
            //     registration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
            // });

            // To warm up the application, you can send a sample request
            // This is optional and depends on your application's needs
            // logger.info("Warming up the application...");
            // AwsProxyRequest warmupRequest = new AwsProxyRequest();
            // warmupRequest.setHttpMethod("GET");
            // warmupRequest.setPath("/ping"); // Replace with an actual endpoint in your application
            // handler.proxy(warmupRequest, null); // Context can be null for warmup
            // logger.info("Warmup complete.");

        } catch (ContainerInitializationException e) {
            // If you fail here. We re-throw the exception to force another cold start
            logger.error("Could not initialize Spring Boot application", e);
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    public StreamLambdaHandler() {
        // We enable the timer for debugging purposes.
        // TODO: Consider removing or making configurable for production
        Timer.enable();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }

    // Simple Timer class for debugging purposes
    // TODO: Consider removing or making this more robust if kept for production
    private static class Timer {
        private static long startTime;
        private static boolean enabled = false;

        public static void enable() {
            enabled = true;
        }

        public static void start() {
            if (enabled) {
                startTime = System.currentTimeMillis();
            }
        }

        public static void stop(String message) {
            if (enabled) {
                long endTime = System.currentTimeMillis();
                logger.info(message + ": " + (endTime - startTime) + "ms");
            }
        }
    }
}
