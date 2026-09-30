data "aws_sns_topic" "rc_alarms" {
  name = "research-catalog-team-alarms-production"
}

resource "aws_cloudwatch_log_metric_filter" "bib_processed" {
  log_group_name = module.base.log_group_name
  name           = "RecapHarvesterBibProcessed-${var.environment}"
  pattern        = "\"Processing bib\""
  region         = "us-east-1"

  metric_transformation {
    name      = "RecapHarvesterBibProcessed-${var.environment}"
    namespace = "LogMetrics"
    unit      = "None"
    value     = "1"
  }
}

# Alarm when RecapHarvesterBibProcessed-${var.environment} <= 0 for 1 day (no bibs processed)
resource "aws_cloudwatch_metric_alarm" "not_processing_bibs" {
  alarm_name          = "RecapHarvesterNotProcessingBibs-${var.environment}"
  alarm_description   = "ReCAP Harvester Poller has not processed any bibs in the last day."
  comparison_operator = "LessThanOrEqualToThreshold"
  evaluation_periods  = 1
  metric_name         = aws_cloudwatch_log_metric_filter.bib_processed.metric_transformation[0].name
  namespace           = aws_cloudwatch_log_metric_filter.bib_processed.metric_transformation[0].namespace
  period              = 86400
  statistic           = "Sum"
  threshold           = 0
  treat_missing_data  = "breaching"
  alarm_actions = [data.aws_sns_topic.rc_alarms.arn]
}