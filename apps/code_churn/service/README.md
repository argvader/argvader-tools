## Engineer and code analysis Services ##
This may be non front end services for timed batch and data analysis for
other applications. Fun stuff hopefully!!


```
> java -cp target/service.jar clojure.main -m code-churn.core
```



Docker Build and Run

```
docker build -t code-churn-api:latest .
docker run --name code-churn-api -d -it -p 8888:8888 code-churn-api
```

## Deploy ##

Push Release into ECR
```
> clj -R1.10.1 -Auberdeps
> clj -R1.10.1 -Apush-image
> clj -R1.10.1 -Arelease
```
