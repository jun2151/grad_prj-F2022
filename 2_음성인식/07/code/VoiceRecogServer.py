
import threading

def server():
    
    import pyaudio
    
    while True:
        sampwidth = 2
        rate = 44100
        p = pyaudio.PyAudio()
        stream = p.open(format =
                p.get_format_from_width(sampwidth),
                channels = 1,
                rate = rate,
                output = True)
    
        #1. 지하철전동차 방송시스템에서 안내방송파일 수신 by socket 
        import socket
        print("#socket server start!")
        #!
        host = ''
        port = 3000
        size = 2048
        sock = socket.socket()
        #sock.setblocking(0)
        sock.bind((host, port))

        #sock.listen(5)
        sock.listen(1)
        conn, addr = sock.accept()
    
        bytes_ary = []
        while True:
            data = conn.recv(size)
            #print(data)
            #stream.write(data)

            bytes_ary.append(data)
            if (len(data) < size):
                bytes_ary.append(data)
                break

        conn.close()
        print("#socket server end !")
    
        file_name = "raudio.wav"
        file = open(file_name, "wb")
        file.write(b''.join(bytes_ary))
    
        #pyaudio close 
        stream.stop_stream()
        stream.close()
        p.terminate()

    
        #2. google stt api 호출 
        from google.cloud import speech
        import os
        import io
    
        RATE = 44100    
        #os.environ['GOOGLE_APPLICATION_CREDENTIALS']= 'google_secret_key.json'
        client = speech.SpeechClient()

        with io.open(file_name, "rb") as audio_file:
            content = audio_file.read()
            if (content == b''):    #음성파일 전송이 잘못된 경우 처리 
                print('#audio content error !')
                continue;
            audio = speech.RecognitionAudio(content=content)

        config = speech.RecognitionConfig(
            encoding=speech.RecognitionConfig.AudioEncoding.LINEAR16,
            sample_rate_hertz=RATE,
            language_code="ko-KR",
            #enable_automatic_punctuation=True,
            #audio_channel_count=2,
        )
    
        # Sends the request to google to transcribe the audio
        response = client.recognize(request={"config": config, "audio": audio})
        for result in response.results:
            print("Transcript: {}".format(result.alternatives[0].transcript))
            #ex) Transcript: 이번 역은 성균관대 성균관대 역입니다 내리실 문은 오른쪽입니다
        
        ts = result.alternatives[0].transcript

    
        #3. transcript 분석
        import re
    
        '''
        ts = '이번 역은 성균관대 성대입구 역입니다 내리실 문은 오른쪽입니다'
        print('#ts')
        print(ts)
        '''
    
        head_kw_1 = '이번 역은'
        tail_kw_1 = '역입니다'
    
        re_pattern = '(?<='+head_kw_1+')(.*?)(?='+tail_kw_1+')'
        #ex) (?<=이번 역은)(.*?)(?=역입니다)
        re_result = re.findall(re_pattern, ts)
        #성균관대 성균관대
    
        station_text = ''
        if (len(re_result) > 0):
            #print('#re_result[0]')
            #print(re_result[0]) # 성균관대 성균관대

            temp = re_result[0].strip().split(' ')
            #print('#temp')
            #print(temp) #['성균관대', '성균관대']
            if (len(temp) > 1):
                if (temp[0] == temp[1]):
                    station_text = temp[0]
                else:
                    station_text = temp[0] + ' ' + temp[1]
            else:
                station_text = temp[0]
        print('#station_text')
        print(station_text)
       
        #출입문 닫습니다 처리 추가
    
        #4. 역정보 file write
        file_name = "station.txt"
        if (station_text != ''):
            file = open(file_name, "w")
            file.write(station_text)
            print('#station_text2')
            print(station_text)
            file.close()

if __name__ == '__main__':
    server_thread = threading.Thread(target=server())
    server_thread.start()
    print('#end')
    
#추가항목 
#출입문 닫습니다 처리 추가
    
