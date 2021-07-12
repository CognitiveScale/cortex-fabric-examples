FROM python:3.9.5-slim
ADD . /app
WORKDIR /app
RUN pip install -r requirements.txt
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5000", "--workers", "2"]
