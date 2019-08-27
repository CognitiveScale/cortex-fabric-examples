CORTEX_BASE_URL ?= https://api.cortex-dev.insights.ai
CORTEX_TENANT_ID ?= cortex-examples-tenant
CORTEX_ADMIN_USERNAME ?= cortex-examples-username
CORTEX_ADMIN_PASSWORD ?= cortex-examples-password

.PHONY: install dev.test dev.network.test test dist

install:
	pip install -r requirements-dev.txt

dev.test:
	pytest --cache-clear  \
		--capture=no \
		--ignore cortex_docs_examples/notebooks/python3/examples/deployment \
		--nbval-lax cortex_docs_examples/notebooks/python3/examples

dev.network.test:
	pytest \
		--admin_password=${CORTEX_ADMIN_PASSWORD} \
		--admin_username=${CORTEX_ADMIN_USERNAME} \
		--base_url=${CORTEX_BASE_URL} \
		--cache-clear  \
		--capture=no \
		--current-env \
		--invitation_code=${INVITATION_CODE} \
		--nbval-lax \
		--tenant_id=${CORTEX_TENANT_ID} \
		tests/setup.py cortex_docs_examples/notebooks/python3/examples notebooks

test:
	tox -r

dist:
	mkdir -p dist && \
	cd cortex_docs_examples && \
	zip -r ../dist/notebooks.zip . && \
	cd -