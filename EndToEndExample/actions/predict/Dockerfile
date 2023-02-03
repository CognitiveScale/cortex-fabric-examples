FROM python:3.10-slim
ADD . /app
WORKDIR /app
RUN apt-get update \
    && apt-get upgrade -y \
    && pip install --upgrade -r requirements.txt \
    && apt-get clean

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5000", "--workers", "2"]
