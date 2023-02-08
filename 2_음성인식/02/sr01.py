import speech_recognition as sr

recognizer = sr.Recognizer()
recognizer.energy_threshold = 300

## wav 파일 읽어오기
audio = sr.AudioFile("test.wav")

with audio as source:
    recognizer.adjust_for_ambient_noise(source)
    audio = recognizer.record(source)

text = recognizer.recognize_google(audio_data=audio, language="ko-KR")
print(text)
print('#end')