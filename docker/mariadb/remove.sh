docker container rm `docker container ls -a | grep mariadb | awk '{print $1}'`
docker image rm `docker image ls | grep mariadb  | awk '{print $3}'`

