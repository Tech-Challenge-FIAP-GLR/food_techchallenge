###### vpc/outputs.tf 
output "aws_public_subnet" {
  value = aws_subnet.public_fiaptechchallenge_subnet.*.id
}

output "vpc_id" {
  value = aws_vpc.fiaptechchallenge.id
}