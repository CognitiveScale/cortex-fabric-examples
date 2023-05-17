FROM python:3.9.10-bullseye
ADD ./requirements.txt /app/requirements.txt
WORKDIR /app
RUN pip install --upgrade -r requirements.txt \
    && apt-get purge unzip -y \
    && apt-get clean
ADD . .
ENV PYTHONPATH "${PYTHONPATH}:/app"
CMD ["python3", "predict/main.py"]
