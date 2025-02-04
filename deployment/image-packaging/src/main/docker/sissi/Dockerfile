# https://careers.wolt.com/en/blog/tech/how-to-reduce-jvm-docker-image-size
FROM amazoncorretto:17.0.14-alpine3.20 AS corretto-jdk

RUN apk add --no-cache binutils

RUN $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /customjre

FROM alpine:3.21

ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

COPY --from=corretto-jdk /customjre $JAVA_HOME

ARG APPLICATION_USER=appuser

RUN adduser --no-create-home -u 1000 -D $APPLICATION_USER

RUN mkdir /app && chown -R $APPLICATION_USER /app

COPY --chown=1000:1000 config/* /config/
COPY --chown=1000:1000 wrapper/*.sh /
RUN chmod +x /start.sh
COPY --chown=1000:1000 bot/app.jar /app.jar
CMD ["/start.sh"]