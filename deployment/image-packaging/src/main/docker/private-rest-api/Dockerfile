ARG REGISTRY_PREFIX
ARG SISSI_REGISTRY_PREFIX
ARG VERSION
ARG ABSTRACTO_VERSION
FROM ${REGISTRY_PREFIX}abstracto-rest-api-image-gen:${ABSTRACTO_VERSION:-latest} AS image-gen-api

FROM ${SISSI_REGISTRY_PREFIX}sissi-image-gen-api:${VERSION:-latest} AS sissi-image-gen-api

FROM ${REGISTRY_PREFIX}abstracto-rest-api:${ABSTRACTO_VERSION:-latest} AS running-image
COPY --from=image-gen-api /python /python
COPY --from=sissi-image-gen-api /python /python