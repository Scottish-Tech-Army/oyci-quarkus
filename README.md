# oyci_quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/oyci_quarkus-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Docker

Build and run the container image:

```shell script
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t oyci-quarkus:latest .
docker run -i --rm -p 8080:8080 oyci-quarkus:latest
```

## Helm Chart (Kubernetes Deployment)

The project includes a production-ready Helm chart in `helm/oyci-quarkus/`.

### Prerequisites

- Kubernetes cluster (1.24+)
- Helm 3.x installed
- Container image pushed to a registry

### Quick Start

```shell script
# Install to dev
helm install oyci-api helm/oyci-quarkus \
  -f helm/oyci-quarkus/values-dev.yaml \
  -n oyci-dev --create-namespace \
  --set image.tag=latest

# Upgrade with a new image tag
helm upgrade oyci-api helm/oyci-quarkus \
  -f helm/oyci-quarkus/values-dev.yaml \
  -n oyci-dev \
  --set image.tag=1.0.1

# Install to staging
helm install oyci-api helm/oyci-quarkus \
  -f helm/oyci-quarkus/values-staging.yaml \
  -n oyci-staging --create-namespace

# Install to production
helm install oyci-api helm/oyci-quarkus \
  -f helm/oyci-quarkus/values-prod.yaml \
  -n oyci-prod --create-namespace
```

### Environment-Specific Values

| File | Description |
|------|-------------|
| `values.yaml` | Base defaults |
| `values-dev.yaml` | Dev overrides (1 replica, debug logging, schema auto-update) |
| `values-staging.yaml` | Staging overrides (2 replicas, HPA, TLS, validate schema) |
| `values-prod.yaml` | Production overrides (3 replicas, HPA, PDB, canary-ready) |

### Key Configuration

Override database credentials at install time (or use external secrets):

```shell script
helm install oyci-api helm/oyci-quarkus \
  --set dbCredentials.username=myuser \
  --set dbCredentials.password=mysecret \
  --set datasource.jdbcUrl="jdbc:postgresql://my-db-host:5432/oyci"
```

### Health Endpoints

The Helm chart configures Kubernetes probes against these Quarkus SmallRye Health endpoints:

| Probe | Endpoint |
|-------|----------|
| Liveness | `/q/health/live` |
| Readiness | `/q/health/ready` |
| Startup | `/q/health/started` |

## Harness CI/CD Pipelines

The project includes Harness pipeline-as-code YAML files in `.harness/`.

### Structure

```
.harness/
├── pipelines/
│   ├── ci-build.yaml        # CI: build, test, push Docker image
│   └── cd-deploy.yaml       # CD: deploy to Dev → Staging → Prod
├── services/
│   └── oyci-quarkus.yaml # Harness service definition
├── environments/
│   ├── dev.yaml
│   ├── staging.yaml
│   └── production.yaml
└── inputsets/
    ├── dev-inputset.yaml
    ├── staging-inputset.yaml
    └── prod-inputset.yaml
```

### Harness Prerequisites

Before using the pipelines, configure the following **Harness Connectors**:

1. **GitHub Connector** — for source code access
2. **Docker Registry Connector** — for pushing/pulling container images (e.g. GHCR, ECR, Docker Hub)
3. **Kubernetes Cluster Connector** — one per target environment (dev, staging, prod)

### CI Pipeline (`ci-build.yaml`)

Triggers on push to `main` or pull request. Steps:
1. Maven build (`./mvnw package -DskipTests`)
2. Maven test with JUnit report collection
3. Build & push Docker image tagged with build sequence ID, `latest`, and commit SHA

### CD Pipeline (`cd-deploy.yaml`)

Deploys across three stages with approval gates:
1. **Dev** — auto-deploys via Helm
2. **Staging** — requires 1 manual approval, then Helm deploy
3. **Production** — requires 2 manual approvals, canary deploy (25%), verification, then full rollout

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- SmallRye Health ([guide](https://quarkus.io/guides/smallrye-health)): Monitor service health with Kubernetes-compatible health checks.
- Helm Charts ([guide](https://helm.sh/docs/)): Package manager for Kubernetes.
- Harness CI/CD ([guide](https://developer.harness.io/docs/platform/pipelines/pipeline-as-code/)): Pipeline as Code with Harness.

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
