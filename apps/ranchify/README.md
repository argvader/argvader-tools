### Ranchify Bot ###
Used to create service for /ranchify slack GetCommand

/ranchify "text"

# Notes #
Serverless offline still not working with dynamic code reloading.
Need to export AWS_PROFILE setting

Currently:

clj -M1.10.1:watch for the code hot reload and in other window

sls offline ( this has to be restarted after every change :( )
