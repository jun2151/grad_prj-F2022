#참조 url: https://davey.tistory.com/entry/%ED%8C%8C%EC%9D%B4%EC%8D%AC-%EC%9D%8C%EC%84%B1-%ED%85%8D%EC%8A%A4%ED%8A%B8-%EB%B3%80%ED%99%98-%EB%82%B4-%EB%A7%90-%EB%8C%80%EB%8B%B5-AI-%EB%A1%9C%EB%B4%87-%EB%A7%8C%EB%93%A4%EA%B8%B0-gTTs-SpeechRecognition-%EB%9D%BC%EC%9D%B4%EB%B8%8C%EB%9F%AC%EB%A6%AC
# 마이크로 입력받아서 text로 변환

import speech_recognition as sr

def voice_recog():
    r = sr.Recognizer()
    with sr.Microphone() as source:
        audio = r.listen(source)
        said = ""
        
        try:
            said = r.recognize_google(audio_data=audio, language="ko-KR")
            print(said)
        except Exception as e:
            print("exception: " + str(e))
    return said

text = voice_recog()
print('#end')
    
        
