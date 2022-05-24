import json
import os
import sys

import pyspark
from delta import *
from pyspark.sql import Column, DataFrame, functions


def source_delta_path(input_params):
    return f"s3a://{os.environ['PROFILES_BUCKET']}/sources/{input_params['project_id']}/{input_params['source_name']}-delta"


def func(batchDf: DataFrame, batchId: float):
    mergeDf = batchDf.groupBy(pk).agg(
        *localList).persist()
    delta_table \
        .alias("existing") \
        .merge(
            mergeDf.alias("incoming"),
            f"existing.{pk} = incoming.{pk}"
        ) \
        .whenMatchedUpdate(set=deltaUpdateExpr) \
        .whenNotMatchedInsertAll() \
        .execute()
    mergeDf.unpersist()


def initialize_spark():
    builder = pyspark.sql.SparkSession.builder.appName("MyApp") \
        .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension") \
        .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog") \
        .config("spark.hadoop.fs.s3a.access.key", os.environ["AWS_ACCESS_KEY_ID"]) \
        .config("spark.hadoop.fs.s3a.secret.key", os.environ["AWS_SECRET_KEY"]) \
        .config("spark.hadoop.fs.s3a.endpoint", os.environ['S3_ENDPOINT']) \
        .config("spark.hadoop.fs.s3a.path.style.access", "true")

    conf = pyspark.SparkConf()

    spark = configure_spark_with_delta_pip(
        builder).config(conf=conf).getOrCreate()
    return spark


def get_queries(staticDf: DataFrame):
    localList = list(map(lambda f: functions.last(f).alias(f), filter(
        lambda it: it != pk, staticDf.schema.fieldNames())))

    deltaUpdateExpr = {}

    for field in filter(lambda it: it != pk, staticDf.schema.fieldNames()):
        deltaUpdateExpr[field] = functions.col(f"incoming.{field}")
        """
        TODO: figure out why this does not work (merged table is a null)
            deltaUpdateExpr[field] = functions.when(
                functions.col(f"incoming.{field}").isNull(),
                functions.col(f"existing.{field}")).otherwise(Column(f"incoming.{field}"))
        """
    return localList, deltaUpdateExpr


if __name__ == '__main__':
    input_params = json.loads(sys.argv[-1])

    options = {
        "fs.s3a.access.key": input_params['publicKey'],
        "fs.s3a.secret.key": input_params['secretKey'],
        "fs.s3a.endpoint": input_params['s3Endpoint'],
    }
    spark = initialize_spark()

    staticDf = spark.read \
        .options(**options) \
        .parquet(input_params['uri'])

    streamDf = spark.readStream \
        .options(**options) \
        .option("maxFilesPerTrigger", input_params['maxFilesPerTrigger']) \
        .schema(staticDf.schema) \
        .format(input_params['type']) \
        .load(input_params['stream_read_dir'])

    pk = input_params["primary_key"]
    delta_path = source_delta_path(input_params=input_params)

    if not DeltaTable.isDeltaTable(spark, delta_path):
        staticDf.write \
                .format("delta") \
                .option("mergeSchema", "true") \
                .mode("overwrite") \
                .save(delta_path)

    # retrieve deltaTable
    delta_table = DeltaTable.forPath(spark, delta_path)

    localList, deltaUpdateExpr = get_queries(staticDf)

    streamDf \
        .writeStream \
        .outputMode("append") \
        .option("checkpointLocation", f"{delta_path}/checkpoint") \
        .foreachBatch(func) \
        .trigger(once=True) \
        .start() \
        .awaitTermination()
