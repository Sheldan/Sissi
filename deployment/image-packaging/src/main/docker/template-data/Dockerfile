FROM alpine:3.18.0
MAINTAINER Sheldan
ADD config /var/template-config/
ADD template-artifacts /var/template-config/templates
ADD translation-artifacts /var/template-config/translations

CMD ["/bin/sh", "/var/template-config/move.sh"]