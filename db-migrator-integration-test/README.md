## Integration test

for [JavaLite DB-Migrator](https://github.com/javalite/activejdbc/tree/master/db-migrator)


## Cassandra docker instructions

Starting a container for the first time: 

        docker run  --name=javalite-cassandra   --publish 127.0.0.1:9043:9042 cassandra:2.1

Note,  the external port is mapped to 9043 so as not to conflict with a local instance 

After this, the container will be created automatically. You can check this: 
        
        docker container ls -a

Running in the future:

        docker start javalite_cassandra

Login to bash:

        docker exec -it javalite_cassandra bash