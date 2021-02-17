
docker rm mariadb
docker run --hostname mariadb --name mariadb --env-file=docker/mariadb/env.mariadb --volume=$PWD/docker/data/mariadb:/var/lib/mysql --volume=$PWD/docker/data/mariadb:/etc/mysql/conf.d --volume=$PWD/docker/data/mariadb/log:/var/log/mysql --volume=/tmp:/tmp -p 127.0.0.1:3307:3306 mariadb:latest