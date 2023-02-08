#https://www.kaggle.com/code/mauriciofigueiredo/methods-for-sound-noise-reduction
#결과: 소음 일부제거됨 

from scipy import signal
import random
import librosa

#1. highpass filter 적용, low frequency data 제거

def f_high(y,sr):
    b,a = signal.butter(5, 2200/(sr/2), btype='highpass')
    yf = signal.lfilter(b,a,y)
    return yf

y1,sr = librosa.load('test.wav')
yf1 = f_high(y1, sr)

import soundfile 
soundfile.write("out.wav", yf1, sr, format='WAV')




