FROM openjdk:17-alpine
MAINTAINER Sheldan
VOLUME /tmp
ADD config/* /config/
ADD wrapper/*.sh /
RUN chmod +x /start.sh
ADD bot/app.jar /app.jar
CMD ["/start.sh"]