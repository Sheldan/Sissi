ARG ABSTRACTO_REGISTRY_PREFIX
ARG ABSTRACTO_VERSION=latest
FROM ${ABSTRACTO_REGISTRY_PREFIX}abstracto_deployment:${ABSTRACTO_VERSION:-latest}
MAINTAINER Sheldan
ADD template-artifacts /templates
ADD translation-artifacts /translations
ADD liquibase-artifacts /liquibase-zips
ADD config /
