FROM python:3.11.0a5-slim-bullseye
ADD . /app
WORKDIR /app
RUN pip install -r requirements.txt
ENV PYTHONPATH "${PYTHONPATH}:/app"
CMD ["python3", "predict/main.py"]
