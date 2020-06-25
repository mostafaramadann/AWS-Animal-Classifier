import os
import io
import boto3
import json
import mysql.connector as connector

db = connector.connect(host="animal-db.cjxukm1vzhl2.us-east-1.rds.amazonaws.com",user="admin",passwd="12526788",database="android")
cursor = db.cursor()
def lambda_handler(event, context):

	body = json.loads(event['body'])
	operation = int(body['operation'])
	name = body['name']
	password = body['password']
	status=False
	try:
		if operation == 1:
			cursor.execute("SELECT * FROM users WHERE username = '{0}' AND pass = '{1}'".format(name,password))
			records=cursor.fetchall()
			if len(records)==1:
				status = True
		if operation == 2:
			cursor.execute("INSERT INTO users (username,pass) VALUES (%s, %s)",(name,password))
			db.commit()
			status = True
	except mysql.connector.Error as error:
		status = False
	return {"statusCode":200,"body":json.dumps(status)}