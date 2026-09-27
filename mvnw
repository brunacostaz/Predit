#!/bin/sh
set -e
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MAVEN_VERSION=3.9.9
CACHE_DIR="${HOME}/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}-bin"
MAVEN_HOME="${CACHE_DIR}/apache-maven-${MAVEN_VERSION}"
ZIP_FILE="${CACHE_DIR}/apache-maven-${MAVEN_VERSION}-bin.zip"
if [ ! -x "${MAVEN_HOME}/bin/mvn" ]; then
  mkdir -p "${CACHE_DIR}"
  curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.zip" -o "${ZIP_FILE}"
  unzip -q -o "${ZIP_FILE}" -d "${CACHE_DIR}"
fi
exec "${MAVEN_HOME}/bin/mvn" -f "${BASE_DIR}/pom.xml" "$@"
