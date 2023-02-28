FROM python:3.9-slim

RUN apt-get update \
    && apt-get upgrade -y
ADD . /app
WORKDIR /app
RUN pip install --upgrade -r /app/requirements.txt \
    && apt-get clean

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5000", "--workers", "2"]