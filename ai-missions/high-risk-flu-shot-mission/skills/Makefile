SHELL := /bin/bash

.PHONY:saves-types build deploy save-skill all
SKILLS := send_engage_sydneycare_chatbot_notification  send_nurseline_notification send_schedule_appointment_notification send_sydneycare_notification send_virtual_care_notification update_member_phone_number

save-type:
	@for skl in $(SKILLS); do (cd $$skl && $(MAKE) save-type) || exit 1; done

build:
	@for skl in $(SKILLS); do (cd $$skl && $(MAKE) build) || exit 1; done

deploy:
	@for skl in $(SKILLS); do (cd $$skl && $(MAKE) deploy) || exit 1; done

save-skill:
	@for skl in $(SKILLS); do (cd $$skl && $(MAKE) save-skill) || exit 1; done

all: save-type build deploy save-skill