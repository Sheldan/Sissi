FROM amazoncorretto:17.0.8-alpine3.18
MAINTAINER Sheldan
VOLUME /tmp
ADD config/* /config/
ADD wrapper/*.sh /
RUN chmod +x /start.sh
ADD bot/app.jar /app.jar
CMD ["/start.sh"]