import os
import io
import boto3
import json
import numpy as np
import base64

bucketname='animalclassifier-input'
s3 = boto3.resource('s3')

ENDPOINT_NAME = os.environ['ENDPOINT_NAME']
runtime= boto3.client('runtime.sagemaker')


def lambda_handler(event, context):

    path = json.loads(event['body'])
    path = path['name']
    obj = s3.Object(bucketname, path)
    body = obj.get()['Body'].read()
    payload = bytearray(body)
    
    response = runtime.invoke_endpoint(EndpointName=ENDPOINT_NAME,
                                       ContentType='application/x-image',
                                       Body=payload)
    #print(response)
    result = json.loads(response['Body'].read())
    index = np.argmax(result)
    object_categories = ['airplane', 'automobile', 'bird', 'cat', 'deer', 'dog', 'frog', 'horse', 'ship', 'truck']
    predicted_label = object_categories[index]

    return {"statusCode":200,"body":json.dumps(predicted_label)}
