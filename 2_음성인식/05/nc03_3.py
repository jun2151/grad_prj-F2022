# url: https://www.kaggle.com/code/awalahmed/noisecancel4000/notebook
# In [14] 부분 코드 테스트 
# 결과: 소음 제거 효과 없음

from librosa.core import resample, to_mono
import librosa
import wavio
import scipy as sp
from scipy import signal

def reduce_noise_median(y, sr):
    y = sp.signal.medfilt(y,3)
    return (y)

def trim_silence(y):
    y_trimmed, index = librosa.effects.trim(y, top_db=20, frame_length=2, hop_length=500)
    trimmed_length = librosa.get_duration(y) - librosa.get_duration(y_trimmed)
    return y_trimmed, trimmed_length

def butter_highpass(cutoff, fs, order=3):
    nyq = 0.5 * fs
    normal_cutoff = cutoff / nyq
    #b, a = signal.butter(order, normal_cutoff, btype='high', analog=False)
    b, a = signal.butter(order, normal_cutoff, btype='low', analog=False)
    return b, a
def butter_highpass_filter(data, fs, cutoff = 10, order=3):
    b, a = butter_highpass(cutoff, fs, order=order)
    y = signal.filtfilt(b, a, data)
    return y

#url: https://hadaney.tistory.com/7
def butter_bandpass(lowcut, highcut, fs, order=5):
    nyq = 0.5 * fs
    low = lowcut / nyq
    high = highcut / nyq
    
    b, a = signal.butter(order, [low,high], btype='band')
    return b, a
def butter_bandpass_filter(data, lowcut, highcut, fs, order=5):
    b, a = butter_bandpass(lowcut, highcut, fs, order=order)
    y = signal.filtfilt(b, a, data)
    return y


y, sr = librosa.load('test.wav')

try:
    channel = y.shape[1]
    print('#channel')
    print(channel)    
    if channel == 2:
        y = to_mono(y.T)
    elif channel == 1:
        y = to_mono(y.reshape(-1))
except IndexError:
    y = to_mono(y.reshape(-1))
    pass
except Exception as exc:
    raise exc

    
wav = reduce_noise_median(y, sr)
#wav = butter_highpass_filter(y, sr)
#wav = butter_bandpass_filter(y, 5000, 10000, sr, 3)
#wav, time_trimmed = trim_silence(wav)


import soundfile 
soundfile.write("out.wav", wav, sr, format='WAV')
