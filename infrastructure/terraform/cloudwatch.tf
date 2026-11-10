# CloudWatch Logs

resource "aws_cloudwatch_log_group" "ecs" {
  name              = "/ecs/campus-booking-dev"
  retention_in_days = 7

  tags = merge(local.common_tags, {
    Name = "/ecs/campus-booking-dev"
  })
}
