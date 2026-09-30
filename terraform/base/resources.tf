provider "aws" {
  region     = "us-east-1"
}

terraform {
  # Use s3 to store terraform state
  backend "s3" {
    bucket  = "nypl-github-actions-builds-production"
    key     = "recap-harvester-poller-terraform-state"
    region  = "us-east-1"
  }
}

locals {
  tags = {
    Project = "Research Catalog"
    BusinessUnit = "LSP"
    Environment  = var.environment
  }
  log_group_name = "/ecs/recap-harvester-poller-${var.environment}"
}

variable "environment" {
  type = string
  default = "qa"
  description = "The name of the environment (qa, production). This controls the name of lambda and the env vars loaded."

  validation {
    condition     = contains(["qa", "production"], var.environment)
    error_message = "The environment must be 'qa' or 'production'."
  }
}

output "log_group_name" {
  value       = local.log_group_name
  description = "CloudWatch log group name for this environment."
}