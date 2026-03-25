JAVA_HOME ?= /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
ANDROID_HOME ?= /opt/homebrew/share/android-commandlinetools
ANDROID_SDK_ROOT ?= $(ANDROID_HOME)
GRADLE ?= gradle
KEYSTORE_PATH ?= armored-age-release.jks
KEY_ALIAS ?= armored-age
STORE_PASSWORD ?=
KEY_PASSWORD ?=

export JAVA_HOME
export ANDROID_HOME
export ANDROID_SDK_ROOT
export PATH := $(JAVA_HOME)/bin:$(ANDROID_HOME)/cmdline-tools/latest/bin:$(ANDROID_HOME)/platform-tools:$(PATH)

.PHONY: help sdk debug release lint test clean build-debug build-release build-local-debug build-local-release signing-secrets

help:
	@printf "Targets:\n"
	@printf "  make sdk      Install Android API 36 and Build Tools 36.0.0\n"
	@printf "  make debug    Build the debug APK\n"
	@printf "  make release  Build the release APK\n"
	@printf "  make build-debug         Alias for make debug\n"
	@printf "  make build-release       Alias for make release\n"
	@printf "  make build-local-debug   Build debug APK with repo macOS defaults\n"
	@printf "  make build-local-release Build release APK with repo macOS defaults\n"
	@printf "  make signing-secrets STORE_PASSWORD=... KEY_PASSWORD=... [KEYSTORE_PATH=armored-age-release.jks] [KEY_ALIAS=armored-age]\n"
	@printf "  make lint     Run Android lint\n"
	@printf "  make test     Run unit tests\n"
	@printf "  make clean    Remove Gradle build outputs\n"

sdk:
	sdkmanager "platforms;android-36" "build-tools;36.0.0"

debug:
	$(GRADLE) :app:assembleDebug

release:
	$(GRADLE) :app:assembleRelease

build-debug: debug

build-release: release

build-local-debug: debug

build-local-release: release

signing-secrets:
	@test -n "$(STORE_PASSWORD)" || (printf "STORE_PASSWORD is required\n" >&2; exit 1)
	@test -n "$(KEY_PASSWORD)" || (printf "KEY_PASSWORD is required\n" >&2; exit 1)
	@if [ ! -f "$(KEYSTORE_PATH)" ]; then \
		keytool -genkeypair \
			-storetype JKS \
			-keystore "$(KEYSTORE_PATH)" \
			-alias "$(KEY_ALIAS)" \
			-keyalg RSA \
			-keysize 2048 \
			-validity 10000 \
			-storepass "$(STORE_PASSWORD)" \
			-keypass "$(KEY_PASSWORD)" \
			-dname "CN=ArmoredAge,O=ArmoredAge,C=US"; \
	fi
	@printf "Add these GitHub Actions secrets:\n"
	@printf "ANDROID_KEYSTORE_BASE64="
	@base64 < "$(KEYSTORE_PATH)" | tr -d '\n'
	@printf "\nANDROID_KEYSTORE_PASSWORD=%s\n" "$(STORE_PASSWORD)"
	@printf "ANDROID_KEY_ALIAS=%s\n" "$(KEY_ALIAS)"
	@printf "ANDROID_KEY_PASSWORD=%s\n" "$(KEY_PASSWORD)"

lint:
	$(GRADLE) :app:lint

test:
	$(GRADLE) :app:testDebugUnitTest

clean:
	$(GRADLE) clean
