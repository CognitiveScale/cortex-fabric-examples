SHELL := /bin/bash

.PHONY:build push deploy tests get all
SKILLS := send_engage_sydneycare_chatbot_notification  send_nurseline_notification send_schedule_appointment_notification send_sydneycare_notification send_virtual_care_notification update_member_phone_number


build:
	@for skl in $(SKILLS); do (cd $$skl && $(MAKE) build) || exit 1; done

push:
	@for skl in $(SKILLS); do (cd $$skl && $(MAKE) push) || exit 1; done

deploy:
	@for skl in $(SKILLS); do (cd $$skl && $(MAKE) deploy) || exit 1; done

tests:
	@for skl in $(SKILLS); do (cd $$skl && $(MAKE) tests) || exit 1; done

get:
	@for skl in $(SKILLS); do (cd $$skl && $(MAKE) get) || exit 1; done

all: build push deploy get tests