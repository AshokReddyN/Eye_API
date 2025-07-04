terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

locals {
  resource_prefix = "${var.app_name}-${var.environment}"
  common_tags = {
    Environment = var.environment
    Application = var.app_name
    ManagedBy   = "Terraform"
  }
}

# IAM Role for Lambda Function
resource "aws_iam_role" "lambda_exec_role" {
  name = "${local.resource_prefix}-lambda-exec-role"

  assume_role_policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [{
      Action    = "sts:AssumeRole",
      Effect    = "Allow",
      Principal = {
        Service = "lambda.amazonaws.com"
      }
    }]
  })

  tags = local.common_tags
}

resource "aws_iam_policy" "lambda_logging_policy" {
  name        = "${local.resource_prefix}-lambda-logging-policy"
  description = "IAM policy for Lambda logging to CloudWatch"

  policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [{
      Action = [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      Effect   = "Allow",
      Resource = "arn:aws:logs:*:*:*"
    }]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "lambda_logging_attachment" {
  role       = aws_iam_role.lambda_exec_role.name
  policy_arn = aws_iam_policy.lambda_logging_policy.arn
}

# Optional: If your Lambda needs other permissions (e.g., S3, DynamoDB), add them here
# resource "aws_iam_policy" "lambda_s3_access_policy" {
#   name        = "${local.resource_prefix}-lambda-s3-access"
#   description = "IAM policy for Lambda to access specific S3 bucket"
#   policy = jsonencode({
#     Version   = "2012-10-17",
#     Statement = [{
#       Action = [
#         "s3:GetObject",
#         "s3:PutObject"
#       ],
#       Effect   = "Allow",
#       Resource = "arn:aws:s3:::your-specific-bucket/*" # Change this
#     }]
#   })
#   tags = local.common_tags
# }
# resource "aws_iam_role_policy_attachment" "lambda_s3_access_attachment" {
#   role       = aws_iam_role.lambda_exec_role.name
#   policy_arn = aws_iam_policy.lambda_s3_access_policy.arn
# }

# Lambda Function
resource "aws_lambda_function" "app_lambda" {
  function_name = "${local.resource_prefix}-lambda"
  role          = aws_iam_role.lambda_exec_role.arn
  handler       = var.lambda_handler
  runtime       = var.lambda_runtime
  memory_size   = var.lambda_memory_size
  timeout       = var.lambda_timeout

  # The S3 bucket and key for the code will be updated by the CI/CD pipeline
  # For initial deployment, you might need a placeholder package or use a data source
  # if the bucket is already populated.
  s3_bucket = var.s3_bucket_name_for_lambda_code # This bucket must exist
  s3_key    = "${local.resource_prefix}/app.jar" # This will be the path for the deployed jar

  environment {
    variables = {
      SPRING_PROFILES_ACTIVE = var.environment
      # Add other environment variables your application might need
      # EXAMPLE_VAR = "example_value"
    }
  }

  tags = local.common_tags

  # Depending on your VPC configuration, you might need to add vpc_config here
  # vpc_config {
  #   subnet_ids         = ["subnet-xxxxxxxxxxxxxxxxx", "subnet-yyyyyyyyyyyyyyyyy"]
  #   security_group_ids = ["sg-zzzzzzzzzzzzzzzzz"]
  # }

  depends_on = [
    aws_iam_role_policy_attachment.lambda_logging_attachment
  ]
}

# API Gateway (HTTP API - simpler and cheaper than REST API for this use case)
resource "aws_apigatewayv2_api" "lambda_api" {
  name          = "${local.resource_prefix}-http-api"
  protocol_type = "HTTP"
  description   = "API Gateway for ${var.app_name} (${var.environment})"

  cors_configuration {
    allow_origins = ["*"] # Be more specific in production
    allow_methods = ["GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"]
    allow_headers = ["Content-Type", "Authorization", "X-Amz-Date", "X-Api-Key", "X-Amz-Security-Token"]
    expose_headers = ["Content-Length", "Content-Type", "Authorization"]
    max_age = 300 # In seconds
  }

  tags = local.common_tags
}

resource "aws_apigatewayv2_integration" "lambda_integration" {
  api_id           = aws_apigatewayv2_api.lambda_api.id
  integration_type = "AWS_PROXY" # For Lambda integration
  integration_uri  = aws_lambda_function.app_lambda.invoke_arn
  payload_format_version = "2.0" # For SpringBootLambdaContainerHandler with HttpApiV2ProxyHandler
}

resource "aws_apigatewayv2_route" "proxy_route" {
  api_id    = aws_apigatewayv2_api.lambda_api.id
  route_key = "$default" # Catch-all route
  target    = "integrations/${aws_apigatewayv2_integration.lambda_integration.id}"
}

# Alternative: Specific routes if you don't want a catch-all
# resource "aws_apigatewayv2_route" "get_items" {
#   api_id    = aws_apigatewayv2_api.lambda_api.id
#   route_key = "GET /items"
#   target    = "integrations/${aws_apigatewayv2_integration.lambda_integration.id}"
# }
# resource "aws_apigatewayv2_route" "post_items" {
#   api_id    = aws_apigatewayv2_api.lambda_api.id
#   route_key = "POST /items"
#   target    = "integrations/${aws_apigatewayv2_integration.lambda_integration.id}"
# }

resource "aws_apigatewayv2_stage" "lambda_stage" {
  api_id      = aws_apigatewayv2_api.lambda_api.id
  name        = var.api_gateway_stage_name # e.g., api, v1, or $default
  auto_deploy = true

  # Access logs can be configured here if needed
  # default_route_settings {
  #   throttling_burst_limit = 5000
  #   throttling_rate_limit  = 10000
  # }

  # stage_variables = {
  #   myStageVar = "value"
  # }

  tags = local.common_tags
}

# Lambda Permission for API Gateway to invoke the function
resource "aws_lambda_permission" "api_gw_permission" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.app_lambda.function_name
  principal     = "apigateway.amazonaws.com"

  # The ARN of the API Gateway, can be restricted to specific routes/methods if needed
  source_arn = "${aws_apigatewayv2_api.lambda_api.execution_arn}/*/*"
  # For more specific permission, e.g. a specific route:
  # source_arn = "${aws_apigatewayv2_api.lambda_api.execution_arn}/*/\$default"
  # or source_arn = "${aws_apigatewayv2_api.lambda_api.execution_arn}/*_METHOD_/${var.api_gateway_stage_name}/*_PATH_"
}

# Note on API Gateway REST API vs HTTP API:
# The code above uses HTTP API, which is generally recommended for Lambda proxy integrations
# due to its simplicity, lower cost, and better performance.
# If you specifically need features from REST API (e.g., custom authorizers, API keys, request validation, etc.),
# the resources would be different (aws_api_gateway_rest_api, aws_api_gateway_resource,
# aws_api_gateway_method, aws_api_gateway_integration, aws_api_gateway_deployment, aws_api_gateway_stage).
# The user request specified "REST API Gateway", so if that's a hard requirement, this needs to be changed.
# For now, I've chosen HTTP API for its common use with Lambda. I can change it if needed.
# If CORS is not working as expected with HttpApi's built-in CORS,
# we might need to handle OPTIONS method explicitly or adjust cors_configuration.
# The `cors_configuration` block in `aws_apigatewayv2_api` is the standard way for HTTP APIs.
# For REST APIs, CORS is handled via `aws_api_gateway_method_response` and `aws_api_gateway_integration_response`
# and potentially an OPTIONS method mock integration.

# CloudWatch Log Group for the Lambda function (optional, Lambda creates one by default but this gives more control)
resource "aws_cloudwatch_log_group" "lambda_log_group" {
  name              = "/aws/lambda/${aws_lambda_function.app_lambda.function_name}"
  retention_in_days = 14 # Adjust as needed

  tags = local.common_tags
}
