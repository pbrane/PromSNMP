.DEFAULT_GOAL := promsnmp

SHELL               := /bin/bash -o nounset -o pipefail -o errexit
VERSION             ?= $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
GIT_BRANCH          := $(shell git branch --show-current)
GIT_SHORT_HASH      := $(shell git rev-parse --short HEAD)
OCI_TAG             := local/promsnmp:$(VERSION)
DATE                := $(shell date -u +"%Y-%m-%dT%H:%M:%SZ") # Date format RFC3339
JAVA_MAJOR_VERSION  := 21

.PHONY help:
help:
	@echo ""
	@echo "Build PromSNMP from source"
	@echo "Goals:"
	@echo "  help:         Show this help with explaining the build goals"
	@echo "  promsnmp      Compile, assemble and run test suite"
	@echo "  clean:        Clean the build artifacts"
	@echo ""

.PHONY deps-build:
deps-build:
	command -v java
	command -v javac
	command -v mvn
	@java --version
	@echo "Check Java version $(JAVA_MAJOR_VERSION)"
	@java --version | grep '21\.[[:digit:]]*\.[[:digit:]]*' >/dev/null

.PHONY deps-oci:
deps-oci:
	command -v docker

.PHONY promsnmp:
promsnmp: deps-build
	mvn --batch-mode --update-snapshots verify

.PHONY oci:
oci: deps-oci promsnmp
	docker build -t $(OCI_TAG) \
      --build-arg="VERSION=$(VERSION)" \
      --build-arg="GIT_SHORT_HASH"=$(GIT_SHORT_HASH) \
      --build-arg="DATE=$(DATE)" \
      .

.PHONY clean:
clean: deps-build
	mvn clean
