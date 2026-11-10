# Networking

data "aws_availability_zones" "available" {
  state = "available"
}

locals {
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

resource "aws_vpc" "main" {
  cidr_block           = "10.20.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = merge(local.common_tags, {
    Name = "campus-booking-dev-vpc"
  })
}

resource "aws_subnet" "public" {
  count = 2

  vpc_id                  = aws_vpc.main.id
  cidr_block              = ["10.20.1.0/24", "10.20.2.0/24"][count.index]
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${var.environment}-public-${count.index == 0 ? "a" : "b"}"
    Tier = "Public"
  })
}

resource "aws_subnet" "application" {
  count = 2

  vpc_id            = aws_vpc.main.id
  cidr_block        = ["10.20.11.0/24", "10.20.12.0/24"][count.index]
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${var.environment}-app-${count.index == 0 ? "a" : "b"}"
    Tier = "PrivateApplication"
  })
}

resource "aws_subnet" "database" {
  count = 2

  vpc_id            = aws_vpc.main.id
  cidr_block        = ["10.20.21.0/24", "10.20.22.0/24"][count.index]
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${var.environment}-db-${count.index == 0 ? "a" : "b"}"
    Tier = "PrivateDatabase"
  })
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${var.environment}-igw"
  })
}

# Route tables

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${var.environment}-public-rt"
  })
}

resource "aws_route_table" "application" {
  vpc_id = aws_vpc.main.id

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${var.environment}-app-rt"
  })
}

resource "aws_route_table" "database" {
  vpc_id = aws_vpc.main.id

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${var.environment}-db-rt"
  })
}

# Route table associations

resource "aws_route_table_association" "public" {
  count = 2

  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "application" {
  count = 2

  subnet_id      = aws_subnet.application[count.index].id
  route_table_id = aws_route_table.application.id
}

resource "aws_route_table_association" "database" {
  count = 2

  subnet_id      = aws_subnet.database[count.index].id
  route_table_id = aws_route_table.database.id
}

# Security Groups
# Future security groups for the load balancer, ECS tasks, and RDS.

# RDS
# Future Amazon RDS for MySQL resources and supporting configuration.

# ECR
# Future Amazon Elastic Container Registry repository resources.

# ECS
# Future ECS cluster, task definition, service, and IAM resources.

# Application Load Balancer
# Future ALB, target group, listener, and routing resources.

# CloudWatch
# Future log groups, alarms, and monitoring resources.
