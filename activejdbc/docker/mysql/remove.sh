docker container stop javalite-mysql
docker container rm javalite-mysql
docker image rm `docker image ls  | grep "8.0.27" | awk '{print $3}'`