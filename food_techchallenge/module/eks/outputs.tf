output "endpoint" {
  value = aws_eks_cluster.fiaptechchallenge.endpoint
}

output "kubeconfig-certificate-authority-data" {
  value = aws_eks_cluster.fiaptechchallenge.certificate_authority[0].data
}
output "cluster_id" {
  value = aws_eks_cluster.fiaptechchallenge.id
}
output "cluster_endpoint" {
  value = aws_eks_cluster.fiaptechchallenge.endpoint
}
output "cluster_name" {
  value = aws_eks_cluster.fiaptechchallenge.name
}