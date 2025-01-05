FROM confluentinc/cp-kafka:7.4.0

EXPOSE 9092 2181

ENV KAFKA_BROKER_ID=1
ENV KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
ENV KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092
ENV KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
ENV KAFKA_LOG_DIRS=/kafka-logs

CMD ["bash", "-c", "zookeeper-server-start /etc/kafka/zookeeper.properties & kafka-server-start /etc/kafka/server.properties"]