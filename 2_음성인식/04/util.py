#1. sample_rate 변환 
'''
import librosa, soundfile 
from scipy.io import wavfile

input_wav = "test.wav"
sample_rate, data = wavfile.read(input_wav)
print('#sample_rate')
print(sample_rate)

origin_sr = sample_rate
resample_sr = 44100

y, sr = librosa.load(input_wav, sr=origin_sr)
resample = librosa.resample(y, sr, resample_sr)
print("original wav sr: {}, original wav shape: {}, resample wav sr: {}, resmaple shape: {}".format(origin_sr, y.shape, resample_sr, resample.shape))

soundfile.write("out.wav", resample, resample_sr, format='WAV')
'''

#2. 음성데이터 자르기 
'''
import librosa, soundfile

audio_file = "test.wav"
y, sr = librosa.load(audio_file)
sec = 5
ny = y[:sr*sec]
soundfile.write("out.wav", ny, sr, format='WAV')
'''

#3. stereo to mono channel 변환 
# ffmpeg 설치필요
'''
from pydub import AudioSegment
sound = AudioSegment.from_wav("test.wav")
sound = sound.set_channels(1)
sound.export("out.wav", format="wav")
'''

#============================
#4. 마이크로 입력받은 음성을 파일로 저장 
'''
import pyaudio
import wave

CHUNK = 1024
FORMAT = pyaudio.paInt16
CHANNELS = 1
RATE = 44100
RECORD_SECONDS = 5
WAVE_OUTPUT_FILENAME = "out.wav"

p = pyaudio.PyAudio()
stream = p.open(format=FORMAT,
                channels=CHANNELS,
                rate=RATE,
                input=True,
                frames_per_buffer=CHUNK)

print("* recording")
frames = []
for i in range(0, int(RATE / CHUNK * RECORD_SECONDS)):
    data = stream.read(CHUNK)
    frames.append(data)
print("* done recording")

stream.stop_stream()
stream.close()
p.terminate()

wf = wave.open(WAVE_OUTPUT_FILENAME, 'wb')
wf.setnchannels(CHANNELS)
wf.setsampwidth(p.get_sample_size(FORMAT))
wf.setframerate(RATE)
wf.writeframes(b''.join(frames))
wf.close()
'''

#5. wav file을 읽어서 play sound 실행 
'''
import pyaudio
import wave

chunk = 1024

# open up a wave
wf = wave.open('test.wav', 'rb')
swidth = wf.getsampwidth()
rate = wf.getframerate() 
channels = wf.getnchannels()
print('#wave file info')
print(channels) #1
print(rate) #44100
print(swidth) #2
   
# open pyaudio stream
p = pyaudio.PyAudio()
stream = p.open(format =
    p.get_format_from_width(wf.getsampwidth()),
    channels = channels,
    rate = rate,
    output = True)
    #input = True)
                
# read wav data
data = wf.readframes(chunk)
while len(data) == chunk*swidth*channels:
    # write data out to the audio stream
    stream.write(data)
    data = wf.readframes(chunk)
if data:
    stream.write(data)
stream.close()
p.terminate()
'''

#6. wav file을 읽어서 chunk 단위로 음성데이터 확인
'''
from scipy.io import wavfile

samplerate, data = wavfile.read('test.wav')
print('#samplerate, data')
print(samplerate)
print(len(data))
print(data)

chunk =  int(44100 / 10)
for i in range(0, int(len(data)/chunk)):
  sind = i* chunk 
  eind = i* chunk + chunk
  temp = data[sind : eind]
  print(temp)
'''

#7. wav file을 읽어서 play sound 실행함 
'''
from scipy.io import wavfile
import pyaudio

samplerate, data = wavfile.read('test.wav')
print('#samplerate')
print(samplerate)

p = pyaudio.PyAudio()
stream = p.open(
  format=pyaudio.paInt16,
  channels=1,
  rate=samplerate,
  output=True,
)
        
chunk =  int(1024)
for i in range(0, int(len(data)/chunk)):
    sind = i* chunk
    eind = i* chunk + chunk
    temp = data[sind : eind]
    stream.write(temp)

stream.close()
'''
