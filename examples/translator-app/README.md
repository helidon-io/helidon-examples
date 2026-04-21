# Translator Example Application

This application demonstrates a pseudo application composed of two microservices
 implemented with Helidon SE.

## Start Jaeger

With Docker:
```shell
docker run --name jaeger -d -p 16686:16686 -p 4317:4317 cr.jaegertracing.io/jaegertracing/jaeger:2.17.0
```

With Kubernetes:
```shell
kubectl apply \
 -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/ingress-nginx-3.15.2/deploy/static/provider/cloud/deploy.yaml \
 -f ../k8s/jaeger.yaml
```

## Build and run

With Docker:
```shell
docker build -t helidon-examples-translator-backend backend/
docker build -t helidon-examples-translator-frontend frontend/
docker run --rm -d -p 9080:9080 \
    --link jaeger \
    --name helidon-examples-translator-backend \
     helidon-examples-translator-backend:latest
docker run --rm -d -p 8080:8080 \
    --link jaeger \
    --link helidon-examples-translator-backend \
    --name helidon-examples-translator-frontend \
     helidon-examples-translator-frontend:latest
```

```shell
mvn package
java -jar backend/target/helidon-examples-translator-backend.jar &
java -jar frontend/target/helidon-examples-translator-frontend.jar
```

Try the endpoint:
```shell
curl "http://localhost:8080?q=cloud&lang=czech"
curl "http://localhost:8080?q=cloud&lang=french"
curl "http://localhost:8080?q=cloud&lang=italian"
```

Then check out the traces at http://localhost:16686.

## Run with Kubernetes (docker for desktop)

```shell
docker build -t helidon-examples-translator-backend backend/
docker build -t helidon-examples-translator-frontend frontend/
kubectl apply -f backend/app.yaml -f frontend/app.yaml
```

Forward the Jaeger UI port in a separate terminal:
```shell
kubectl port-forward service/jaeger 16686:16686
```

Try the endpoint:
```shell
curl "http://localhost/translator?q=cloud&lang=czech"
curl "http://localhost/translator?q=cloud&lang=french"
curl "http://localhost/translator?q=cloud&lang=italian"
```

Then check out the traces at http://localhost:16686.

Stop the docker containers:
```shell
docker stop jaeger \
    helidon-examples-translator-backend \
    helidon-examples-translator-frontend
```

Delete the Kubernetes resources:
```shell
kubectl delete -f backend/app.yaml -f frontend/app.yaml -f ../k8s/jaeger.yaml
```
