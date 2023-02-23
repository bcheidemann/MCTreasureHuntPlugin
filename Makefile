attach:
	./scripts/attach.sh

build: install
	./scripts/build.sh

copy-build-output:
	./scripts/copy-build-output.sh

dev: build copy-build-output
	./scripts/dev.sh

install:
	./scripts/install.sh

# By running setup-server, you agree to the Minecraft EULA (https://aka.ms/MinecraftEULA)
setup-server:
	./scripts/setup-server.sh

start-server:
	./scripts/start-server.sh

watch:
	./scripts/watch.sh
