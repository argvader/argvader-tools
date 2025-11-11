## Contentful Translation Service ##
Building on the node scripts for translation this is a service - to


```
> java -cp target/service.jar clojure.main -m translation.core
```



Docker Build and Run

```
docker build -t translation-api:latest .
docker run --name translation-api -d -it -p 8888:8888 translation-api
```

## Deploy ##

Push Release into ECR
```
> clj -R1.10.1 -Auberdeps
> clj -R1.10.1 -Apush-image
> clj -R1.10.1 -Arelease
```
