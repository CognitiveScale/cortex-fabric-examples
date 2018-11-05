SCRIPT_DIR=$(shell pwd)

local.detect:
	python test.py detect

build:
	python build.py

detect:
	cortex actions invoke --params-file "$(SCRIPT_DIR)/test/test_detect.json" c12e/lang-detect
