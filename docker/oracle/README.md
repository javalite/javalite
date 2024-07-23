Instructions are taken from here:
https://blogs.oracle.com/oraclemagazine/post/deliver-oracle-database-18c-express-edition-in-containers


mkdir oracle
git clone https://github.com/oracle/docker-images.git
cd docker-images/OracleDatabase/SingleInstance/dockerfiles
./buildDockerImage.sh -v 18.4.0 -x
