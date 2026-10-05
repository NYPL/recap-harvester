provider "aws" {
  region = "us-east-1"
}

variable "environment" {
  type    = string
  default = "qa"
}

module "base" {
  source      = "../base"
  environment = var.environment
}
