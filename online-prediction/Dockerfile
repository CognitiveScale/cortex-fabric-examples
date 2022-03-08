FROM python:3.9.10-buster
ADD . /app
WORKDIR /app
RUN pip install -r requirements.txt
ENV PYTHONPATH "${PYTHONPATH}:/app"
CMD ["python3", "predict/main.py"]
