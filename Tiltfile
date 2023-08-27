allow_k8s_contexts('k8s-cluster')

load('ext://restart_process', 'docker_build_with_restart')
registry = 'harbor.sheldan.dev/sissi/'

local_resource(
  'sissi-java-compile',
  'mvn install && ' +
  'rm -rf application/executable/target/jar-staging && ' +
  'unzip -o application/executable/target/sissi-exec.jar -d application/executable/target/jar-staging && ' +
  'rsync --delete --inplace --checksum -r application/executable/target/jar-staging/ application/executable/target/jar && ' +
  'mkdir application/executable/target/jar/snapshots && ' +
  'rsync --delete --inplace --checksum -r application/executable/target/jar/BOOT-INF/lib/*-SNAPSHOT.jar application/executable/target/jar/snapshots && ' +
  'rm -f application/executable/target/jar/BOOT-INF/lib/*-SNAPSHOT.jar ',
  deps=['pom.xml'])

docker_build_with_restart(
  registry + 'sissi',
  './application/executable/target/jar',
  entrypoint=['java', '-noverify', '-cp', '.:./lib/*', 'dev.sheldan.sissi.executable.Application'],
  dockerfile='./application/executable/Dockerfile',
  live_update=[
    sync('./application/executable/target/jar/BOOT-INF/lib', '/app/lib'),
    sync('./application/executable/target/jar/META-INF', '/app/META-INF'),
    sync('./application/executable/target/jar/BOOT-INF/classes', '/app'),
    sync('./application/executable/target/jar/snapshots', '/app/lib')
  ],
)

docker_build(registry + 'sissi-db-data', 'deployment/image-packaging/src/main/docker/db-data/')
docker_build(registry + 'sissi-template-data', 'deployment/image-packaging/src/main/docker/template-data/')


k8s_yaml(helm('deployment/helm/sissi', values=
['./../Sissi-environments/values/local/values.yaml',
'secrets://./../Sissi-environments/values/local/values.secrets.yaml']
))