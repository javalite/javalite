
docker rm mariadb
docker run  \
--name mariadb \
--env-file=env.mariadb \
--volume=$PWD/data:/var/lib/mysql \
--volume=$PWD/data:/etc/mysql/conf.d \
--volume=$PWD/data:/var/log/mysql \
--volume=/tmp:/tmp -p 127.0.0.1:3307:3306 mariadb:latest