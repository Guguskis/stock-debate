[string]$rootFolder='C:/Dev/serious/stock-debate'

[bool]$startDeployments=0
[bool]$startServices=1
[bool]$startIngress=0

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
    [KubernetesResource]::new('authentication-service', 'authentication-service'    , 'authentication-deployment');
    [KubernetesResource]::new('discussion-service'    , 'discussion-service'        , 'discussion-deployment');
    [KubernetesResource]::new('community-service'     , 'community-service'         , 'community-deployment');
    [KubernetesResource]::new('stock-service'         , 'stock-service'             , 'stock-deployment');
    [KubernetesResource]::new('stock-debate-api'      , 'stock-debate-api-service'  , 'stock-debate-api-deployment');
)
$ingressName = 'stock-debate-ingress'

$kubernetesResources | ForEach-Object {
    If($startDeployments) {
        kubectl apply -f "$rootFolder/$($_.deployment).yml"
    }
    If($startServices) {
        kubectl apply -f "$rootFolder/$($_.module)/$($_.service).yml"
    }
}

If($startIngress) {
    kubectl apply -f "$rootFolder/$ingressName.yml"
}
