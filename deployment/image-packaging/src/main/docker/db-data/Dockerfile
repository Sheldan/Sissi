FROM alpine:3.19.1
MAINTAINER Sheldan
ADD config/ /var/db-config/
ADD liquibase-artifacts /var/db-config/liquibase-zips

CMD ["/bin/sh", "/var/db-config/move.sh"]