#Remove container:
docker rm `docker container ls -a | grep javalite-cassandra | awk '{print $1}'`
docker image  rm `docker image ls  | grep cassandra | awk '{print $3}'`