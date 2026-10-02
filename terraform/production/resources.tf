provider "aws" {
  region = "us-east-1"
}

variable "environment" {
  type    = string
  default = "production"
}

module "base" {
  source      = "../base"
  environment = var.environment
}
