minikube start --vm = true

Start-Process powershell.exe -ArgumentList "minikube mount C:/Dev/h2databases:/h2databases"

minikube addons enable ingress