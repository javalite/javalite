
export MYSQL_DATABASE=activejdbc
docker run --name  javalite-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=p@ssw0rd  -e MYSQL_DATABASE=activejdbc -d mysql:8.0.27

