@echo off

set SKILL_NAME=(send_engage_sydneycare_chatbot_notification  send_nurseline_notification send_schedule_appointment_notification send_sydneycare_notification send_virtual_care_notification update_member_phone_number)

GOTO :%1

:build
    for %%x in %SKILL_NAME% do ( cd "%%x" & cmd /c make build & cd ..)
    GOTO :EOF

:push
    for %%x in %SKILL_NAME% do ( cd "%%x" & cmd /c make push & cd ..)
    GOTO :EOF

:deploy
    for %%x in %SKILL_NAME% do ( cd "%%x" & cmd /c make deploy & cd ..)
    GOTO :EOF

:tests
    for %%x in %SKILL_NAME% do ( cd "%%x" & cmd /c make tests & cd ..)
    GOTO :EOF

:get
    for %%x in %SKILL_NAME% do ( cd "%%x" & cmd /c make get & cd ..)
    GOTO :EOF