# Setup for local docker-compose deployment

* Install docker [here](https://docs.docker.com/get-docker/)
* Install docker-compose [here](https://docs.docker.com/compose/install/).
* Execute `docker-compose build` in `image-packaging`.
* Fill out values in `.env` for token and database password. There are some default values, but token is required to be changed.
* Execute `fill-prometheus-file-sh` in order to populate the prometheus config with the correct password (https://github.com/prometheus/prometheus/issues/2357)
* Execute `docker-compose up` in this directory and wait for completion.
* Per default pgAdmin is available on `localhost:5050` with the configured user and password. It will contain a configuration in the servers list.

