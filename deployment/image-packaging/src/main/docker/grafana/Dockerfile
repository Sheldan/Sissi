ARG GRAFANA_VERSION
FROM grafana/grafana:${GRAFANA_VERSION}
MAINTAINER Sheldan
ADD ./provisioning /etc/grafana/provisioning
ADD ./config.ini /etc/grafana/config.ini
ADD ./dashboards /var/lib/grafana/dashboards