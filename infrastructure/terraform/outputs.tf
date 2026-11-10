output "vpc_id" {
  description = "ID of the Campus Booking Platform VPC."
  value       = aws_vpc.main.id
}

output "vpc_cidr" {
  description = "CIDR block of the Campus Booking Platform VPC."
  value       = aws_vpc.main.cidr_block
}

output "public_subnet_ids" {
  description = "IDs of the public subnets."
  value       = aws_subnet.public[*].id
}

output "private_application_subnet_ids" {
  description = "IDs of the private application subnets."
  value       = aws_subnet.application[*].id
}

output "private_database_subnet_ids" {
  description = "IDs of the private database subnets."
  value       = aws_subnet.database[*].id
}

output "availability_zones" {
  description = "Availability Zones selected for the platform subnets."
  value       = slice(data.aws_availability_zones.available.names, 0, 2)
}

output "ecs_cluster_id" {
  description = "ID of the Campus Booking ECS cluster."
  value       = aws_ecs_cluster.main.id
}

output "ecs_cluster_name" {
  description = "Name of the Campus Booking ECS cluster."
  value       = aws_ecs_cluster.main.name
}

output "ecs_execution_role_arn" {
  description = "ARN of the ECS task execution IAM role."
  value       = aws_iam_role.ecs_execution.arn
}

output "ecs_task_definition_arn" {
  description = "ARN of the ECS task definition."
  value       = aws_ecs_task_definition.main.arn
}

output "ecs_task_definition_family" {
  description = "Family name of the ECS task definition."
  value       = aws_ecs_task_definition.main.family
}

output "ecs_service_name" {
  description = "Name of the ECS service."
  value       = aws_ecs_service.main.name
}

output "ecs_service_arn" {
  description = "ARN of the ECS service."
  value       = aws_ecs_service.main.id
}

output "ecs_desired_count" {
  description = "Desired number of ECS service tasks."
  value       = aws_ecs_service.main.desired_count
}

output "ecs_log_group_name" {
  description = "Name of the ECS CloudWatch log group."
  value       = aws_cloudwatch_log_group.ecs.name
}

output "rds_instance_identifier" {
  description = "Identifier of the RDS MySQL instance."
  value       = aws_db_instance.main.identifier
}

output "rds_endpoint" {
  description = "DNS address of the RDS MySQL instance."
  value       = aws_db_instance.main.address
}

output "rds_port" {
  description = "Port of the RDS MySQL instance."
  value       = aws_db_instance.main.port
}

output "rds_database_name" {
  description = "Initial database name on the RDS MySQL instance."
  value       = aws_db_instance.main.db_name
}

output "rds_security_group_id" {
  description = "ID of the RDS security group."
  value       = aws_security_group.rds.id
}

output "rds_db_subnet_group_name" {
  description = "Name of the RDS DB subnet group."
  value       = aws_db_subnet_group.main.name
}

output "rds_master_user_secret_arn" {
  description = "ARN of the AWS Secrets Manager secret containing the RDS master credentials."
  value       = try(aws_db_instance.main.master_user_secret[0].secret_arn, null)
}

output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer."
  value       = aws_lb.main.dns_name
}

output "alb_url" {
  description = "HTTP URL of the Application Load Balancer."
  value       = "http://${aws_lb.main.dns_name}"
}
