# RDS Database Tier

resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-${var.environment}-db-subnet-group"
  subnet_ids = aws_subnet.database[*].id

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${var.environment}-db-subnet-group"
  })
}

resource "aws_security_group" "rds" {
  name        = "${var.project_name}-${var.environment}-rds-sg"
  description = "Security group for Campus Booking RDS MySQL."
  vpc_id      = aws_vpc.main.id

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${var.environment}-rds-sg"
  })
}

resource "aws_vpc_security_group_ingress_rule" "rds_mysql_from_ecs" {
  security_group_id            = aws_security_group.rds.id
  referenced_security_group_id = aws_security_group.ecs.id
  from_port                    = 3306
  to_port                      = 3306
  ip_protocol                  = "tcp"
  description                  = "Allow MySQL traffic from ECS application tasks."
}

resource "aws_vpc_security_group_egress_rule" "rds_all" {
  security_group_id = aws_security_group.rds.id
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
  description       = "Allow RDS outbound traffic."
}

resource "aws_db_instance" "main" {
  identifier                  = "${var.project_name}-${var.environment}-mysql"
  engine                      = "mysql"
  instance_class              = "db.t3.micro"
  allocated_storage           = 20
  storage_type                = "gp3"
  storage_encrypted           = true
  db_name                     = var.db_name
  username                    = var.db_master_username
  manage_master_user_password = true
  port                        = 3306
  publicly_accessible         = false
  multi_az                    = false
  backup_retention_period     = 1
  deletion_protection         = false
  skip_final_snapshot         = true

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${var.environment}-mysql"
  })
}
