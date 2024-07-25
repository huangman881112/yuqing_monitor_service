FROM java:8
EXPOSE 7788
ADD public_opinion_monitor.jar app.jar
ADD conf /conf
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.config.location=/conf/"]