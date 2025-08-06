#!/bin/bash

# Kubernetes deployment script for Mortgage Calculator MCP Server
# Usage: ./deploy.sh [apply|delete|status]

set -e

NAMESPACE="mcp-server"
APP_NAME="mortgage-calculator-mcp"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        error "kubectl is not installed or not in PATH"
        exit 1
    fi
    
    # Check if kubernetes cluster is accessible
    if ! kubectl cluster-info &> /dev/null; then
        error "Cannot connect to Kubernetes cluster"
        exit 1
    fi
    
    # Check if Docker image exists (for local development)
    if ! docker images | grep -q "mortgage-calculator-mcp"; then
        warn "Docker image 'mortgage-calculator-mcp:latest' not found"
        warn "Please build the image first with: docker build -t mortgage-calculator-mcp:latest ."
    fi
    
    log "Prerequisites check completed"
}

apply_manifests() {
    log "Applying Kubernetes manifests..."
    
    # Apply in order to handle dependencies
    kubectl apply -f namespace.yaml
    kubectl apply -f rbac.yaml
    kubectl apply -f configmap.yaml
    kubectl apply -f deployment.yaml
    kubectl apply -f service.yaml
    kubectl apply -f hpa.yaml
    
    # Optional: Apply ingress (uncomment if you have ingress controller)
    # kubectl apply -f ingress.yaml
    
    log "Waiting for deployment to be ready..."
    kubectl wait --for=condition=available --timeout=300s deployment/${APP_NAME} -n ${NAMESPACE}
    
    log "Deployment completed successfully!"
    show_status
}

delete_manifests() {
    log "Deleting Kubernetes manifests..."
    
    # Delete in reverse order
    kubectl delete -f hpa.yaml --ignore-not-found=true
    kubectl delete -f ingress.yaml --ignore-not-found=true
    kubectl delete -f service.yaml --ignore-not-found=true
    kubectl delete -f deployment.yaml --ignore-not-found=true
    kubectl delete -f configmap.yaml --ignore-not-found=true
    kubectl delete -f rbac.yaml --ignore-not-found=true
    kubectl delete -f namespace.yaml --ignore-not-found=true
    
    log "All resources deleted successfully!"
}

show_status() {
    log "Current deployment status:"
    
    echo
    log "Namespace:"
    kubectl get namespace ${NAMESPACE} 2>/dev/null || echo "Namespace not found"
    
    echo
    log "Pods:"
    kubectl get pods -n ${NAMESPACE} -o wide 2>/dev/null || echo "No pods found"
    
    echo
    log "Services:"
    kubectl get services -n ${NAMESPACE} 2>/dev/null || echo "No services found"
    
    echo
    log "HPA Status:"
    kubectl get hpa -n ${NAMESPACE} 2>/dev/null || echo "No HPA found"
    
    echo
    log "Service URLs:"
    # Get service URL for port-forward
    echo "To access the MCP server locally:"
    echo "kubectl port-forward -n ${NAMESPACE} svc/${APP_NAME}-service 8080:8080"
    echo "Then access: http://localhost:8080"
    
    echo
    echo "To access management endpoints:"
    echo "kubectl port-forward -n ${NAMESPACE} svc/${APP_NAME}-service 8081:8081"
    echo "Health check: http://localhost:8081/actuator/health"
    echo "Metrics: http://localhost:8081/actuator/metrics"
}

build_and_load_image() {
    log "Building Docker image..."
    docker build -t mortgage-calculator-mcp:latest ..
    
    # Load image into kind cluster if using kind
    if kubectl config current-context | grep -q "kind"; then
        log "Detected kind cluster, loading image..."
        kind load docker-image mortgage-calculator-mcp:latest
    fi
    
    # Load image into minikube if using minikube
    if kubectl config current-context | grep -q "minikube"; then
        log "Detected minikube cluster, loading image..."
        minikube image load mortgage-calculator-mcp:latest
    fi
    
    log "Image build and load completed"
}

# Main script logic
case "${1:-apply}" in
    "apply")
        check_prerequisites
        apply_manifests
        ;;
    "delete")
        delete_manifests
        ;;
    "status")
        show_status
        ;;
    "build")
        build_and_load_image
        ;;
    "full-deploy")
        check_prerequisites
        build_and_load_image
        apply_manifests
        ;;
    *)
        echo "Usage: $0 [apply|delete|status|build|full-deploy]"
        echo ""
        echo "Commands:"
        echo "  apply       - Apply all Kubernetes manifests"
        echo "  delete      - Delete all resources"
        echo "  status      - Show current deployment status"
        echo "  build       - Build and load Docker image"
        echo "  full-deploy - Build image and deploy everything"
        echo ""
        echo "Examples:"
        echo "  $0 full-deploy  # Build image and deploy everything"
        echo "  $0 status       # Check deployment status"
        echo "  $0 delete       # Clean up all resources"
        exit 1
        ;;
esac