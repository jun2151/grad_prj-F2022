# wav file to google speech api 

from google.cloud import speech
import os
import io

RATE = 44100

#setting Google credential
#os.environ['GOOGLE_APPLICATION_CREDENTIALS']= 'google_secret_key.json'
# create client instance 
client = speech.SpeechClient()

#the path of your audio file
file_name = "test.wav"

with io.open(file_name, "rb") as audio_file:
    content = audio_file.read()
    audio = speech.RecognitionAudio(content=content)

config = speech.RecognitionConfig(
    encoding=speech.RecognitionConfig.AudioEncoding.LINEAR16,
    sample_rate_hertz=RATE,
    language_code="ko-KR",
)

# Sends the request to google to transcribe the audio
response = client.recognize(request={"config": config, "audio": audio})
# Reads the response
for result in response.results:
    print("Transcript: {}".format(result.alternatives[0].transcript))
	