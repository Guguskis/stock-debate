[string]$rootFolder='C:/Dev/serious/stock-debate'

[bool]$stopDeployments=0
[bool]$stopServices=1
[bool]$stopIngress=0

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
    If($stopDeployments) {
        kubectl delete -f "$rootFolder/$($_.deployment).yml"
    }
    If($stopServices) {
        kubectl delete -f "$rootFolder/$($_.module)/$($_.service).yml"
    }
}

If($stopIngress) {
    kubectl delete -f "$rootFolder/$ingressName.yml"
}
