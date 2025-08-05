FROM alpine:3.22.1
MAINTAINER Sheldan
ADD config /var/template-config/
ADD template-artifacts /var/template-config/templates
ADD translation-artifacts /var/template-config/translations

CMD ["/bin/sh", "/var/template-config/move.sh"]