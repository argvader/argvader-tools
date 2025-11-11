## Journey Team Management Tool ##
Linking with external data on the horizon -- and other sources one day soon (crossing fingers)

## Dirac Configuration ##

* Must start chrome with remote debugging ( currently aliased canary to start chrome canary in debug mode port 9222)
* Dirac starts in alternate window after clicking dirac icon ( need to get key short )

## Build ##

Install: Clojure 1.10 or better installed

Start local Server
```
> clj -R1.10.1 -Adev
```

Optimized Build for Production
```
> clj -R1.10.1 -Abuild
```

Docker Build and Run

```
docker build -t team-tree:latest .
docker run --name test-tree -d -p 8080:80 team-tree
```

## Deploy ##

Push Release into ECR
```
> clj -R1.10.1 -Arelease
```
