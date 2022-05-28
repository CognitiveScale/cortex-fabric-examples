FROM python:latest
ADD . /app
WORKDIR /app
RUN pip install --upgrade -r requirements.txt \
    && apt-get purge unzip -y \
    && apt-get clean
ENV PYTHONPATH "${PYTHONPATH}:/app"
CMD ["python3", "predict/main.py"]
