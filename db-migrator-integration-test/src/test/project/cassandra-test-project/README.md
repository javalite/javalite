


## Run Docker  

Expose port 9043 locally if you want to avoid a conflict with another Cassandra instance:

    docker run  --name=javalite-cassandra-2.1   --publish 127.0.0.1:9043:9042 cassandra:2.1

after this, the container will be created automatically

## Running in the future:

    docker start javalite-cassandra-2.1

## Login
    
    docker exec -it javalite-cassandra-2.1 bash
        
