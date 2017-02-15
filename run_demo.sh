#!/bin/bash

cd src

docker rm -f pingpongserver
docker rm -f pingpongclient

docker build -t pingpongserver .
docker build -t pingpongclient .

docker network rm my-network
docker network create -d bridge my-network

docker run -d --net=my-network -P --name pingpongserver pingpongserver \
    /bin/bash -c "javac *.java && java ActualServer"
echo "running server"
SERVER_IP=$(docker inspect --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' pingpongserver)

echo $SERVER_IP

docker run -d --net=my-network -P --name pingpongclient pingpongclient \
    /bin/bash -c "javac *.java && java PingPongClient ${SERVER_IP}"
echo "running client"
CLIENT_IP=$(docker inspect --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' pingpongclient)


docker logs -f pingpongclient

