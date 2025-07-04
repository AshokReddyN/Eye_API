output "lambda_function_name" {
  description = "The name of the Lambda function"
  value       = aws_lambda_function.app_lambda.function_name
}

output "lambda_function_arn" {
  description = "The ARN of the Lambda function"
  value       = aws_lambda_function.app_lambda.arn
}

output "api_gateway_invoke_url" {
  description = "The invoke URL for the API Gateway"
  value       = aws_apigatewayv2_stage.lambda_stage.invoke_url
}

output "api_gateway_id" {
  description = "The ID of the API Gateway"
  value       = aws_apigatewayv2_api.lambda_api.id
}

output "lambda_iam_role_name" {
  description = "The name of the IAM role created for the Lambda function"
  value       = aws_iam_role.lambda_exec_role.name
}
