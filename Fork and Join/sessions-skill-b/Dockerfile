FROM python:3.7.4-buster

WORKDIR /app

COPY sessions-skill-b sessions-skill-b
COPY sessions.py sessions.py
RUN pip install -r sessions-skill-b/requirements.txt
ENV PYTHONPATH "${PYTHONPATH}:/app"
CMD ["uvicorn", "sessions-skill-b.src.__main__:app", "--host", "0.0.0.0", "--port", "5000"]

