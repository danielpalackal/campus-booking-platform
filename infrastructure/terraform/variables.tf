variable "aws_region" {
  description = "AWS region where the Campus Booking Platform will be deployed."
  type        = string
  default     = "ap-southeast-2"
}

variable "project_name" {
  description = "Name used to identify Campus Booking Platform infrastructure resources."
  type        = string
  default     = "campus-booking"
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
  default     = "dev"
}

variable "container_image" {
  description = "Container image URI used by the ECS Fargate task definition."
  type        = string
  default     = "025452941139.dkr.ecr.ap-southeast-2.amazonaws.com/campus-booking-platform:latest"
}

variable "db_name" {
  description = "Initial MySQL database name for the Campus Booking Platform."
  type        = string
  default     = "campus_booking"
}

variable "db_master_username" {
  description = "Master username for the RDS MySQL instance; the password is managed by AWS Secrets Manager."
  type        = string
  default     = "campus_booking_admin"
}
