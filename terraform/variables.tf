variable "aws_region" {
  description = "AWS region for deployment"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Deployment environment (e.g., dev, qa, stg, prod)"
  type        = string
  validation {
    condition     = contains(["dev", "qa", "stg", "prod"], var.environment)
    error_message = "Environment must be one of: dev, qa, stg, prod."
  }
}

variable "lambda_memory_size" {
  description = "Memory size for the Lambda function"
  type        = number
  default     = 1024
}

variable "lambda_timeout" {
  description = "Timeout for the Lambda function in seconds"
  type        = number
  default     = 30
}

variable "app_name" {
  description = "Application name, used for naming resources"
  type        = string
  default     = "nayonika-api"
}

variable "lambda_handler" {
  description = "The fully qualified name of the Lambda handler class"
  type        = string
  default     = "com.nayonikaeyecare.api.StreamLambdaHandler"
}

variable "lambda_runtime" {
  description = "Lambda runtime"
  type        = string
  default     = "java17" // Corresponds to Java 24 as per Spring Boot version, ensure this is compatible
}

variable "s3_bucket_name_for_lambda_code" {
    description = "Name of the S3 bucket where Lambda code will be stored. This bucket must exist."
    type        = string
    # default     = "your-lambda-code-bucket-name" # User should provide this
}

variable "api_gateway_stage_name" {
  description = "Stage name for API Gateway"
  type        = string
  default     = "api"
}
