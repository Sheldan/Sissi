ARG POSTGRES_VERSION
FROM postgres:${POSTGRES_VERSION}
MAINTAINER Sheldan
VOLUME /tmp
ADD sql/init.sql /docker-entrypoint-initdb.d/init.sql