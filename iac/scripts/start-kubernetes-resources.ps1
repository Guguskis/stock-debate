[string]$rootFolder='C:/Dev/cloud/stock-debate'

[bool]$startDeployments = 1
[bool]$startServices=1
[bool]$startIngress = 1

class KubernetesResource {
    [string]$module
    [string]$service
    [string]$deployment

    KubernetesResource([string]$module, [string]$service, [string]$deployment) {
        $this.module=$module
        $this.service=$service
        $this.deployment=$deployment
    }
}

$kubernetesResources = @(
    [KubernetesResource]::new('authentication-service', 'authentication-service', 'authentication-deployment');
    [KubernetesResource]::new('discussion-service', 'discussion-service', 'discussion-deployment');
    [KubernetesResource]::new('community-service', 'community-service', 'community-deployment');
    [KubernetesResource]::new('stock-service', 'stock-service', 'stock-deployment');
    [KubernetesResource]::new('stock-debate-api', 'stock-debate-api-service', 'stock-debate-api-deployment');
)

$ingressName = 'stock-debate-ingress'

Write-Output "----------------building docker images----------------------------------------------"
& minikube -p minikube docker-env | Invoke-Expression
$kubernetesResources | ForEach-Object {
docker build -t $_.module "$rootFolder/$($_.module)"
}
Write-Output "----------------docker images built   ----------------------------------------------"

If ($startServices) {

    Write-Output "----------------starting kubernetes services------------------------------------"
    $kubernetesResources | ForEach-Object {
        kubectl apply -f "$rootFolder/$($_.module)/$($_.service).yml"
    }
} else {
    Write-Output "----------------kubernetes services start was turned off------------------------"
}
If ($startDeployments) {
    Write-Output "----------------starting kubernetes deployments---------------------------------"
    $kubernetesResources | ForEach-Object {
        kubectl apply -f "$rootFolder/$($_.module)/$($_.deployment).yml"
    }
} else {
    Write-Output "----------------kubernetes deployments start was turned off---------------------"
}

If ($startIngress) {
    Write-Output "----------------starting kubernetes ingress-------------------------------------"
    kubectl apply -f "$rootFolder/iac/resources/$ingressName.yml"
} else {
    Write-Output "----------------kubernetes ingress start was turned off-------------------------"
}
