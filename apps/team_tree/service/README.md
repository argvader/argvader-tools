## Journey Team Management Services ##
GraphQl service for team data and so much more!!

```
> java -cp target/service.jar clojure.main -m team-tree.core
```



Docker Build and Run

```
docker build -t team-tree:latest .
docker run --name team-tree -d -it -p 8888:8888 team-tree
```

## Deploy ##

Push Release into ECR
```
> clj -R1.10.1 -Auberdeps
> clj -R1.10.1 -Apush-image
> clj -R1.10.1 -Arelease
```
