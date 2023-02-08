#scipy lowpass filer 적용
#https://eclipse360.tistory.com/114


from scipy.io import wavfile
from scipy import signal 
import numpy as np 
from matplotlib import pyplot as plt 
import os 

wav = "test.wav"

sample_rate, data = wavfile.read(wav)

time = np.linspace(0, len(data)/sample_rate, len(data))

#Amplitude - Time[s]
plt.figure(figsize=(20,10))
plt.plot(time, data)
plt.xlabel("Amplitude")
plt.ylabel("Time[s]")
plt.title("Amplitude - Time[s]")
plt.show()


fft = np.fft.fft(data)
magnitude = np.abs(fft)

f = np.linspace(0, sample_rate, len(magnitude))
left_spectrum = magnitude[: int(len(magnitude)/2)]
left_f = f[: int(len(magnitude)/2)]

#Frequency vs Magnitude
plt.figure(figsize=(20,10))
plt.plot(left_f, left_spectrum)
plt.xlabel("Frequency")
plt.ylabel("Magnitude")
plt.title("Power spectrum")
plt.show()

b = signal.firwin(101, cutoff=1000, fs=sample_rate, pass_zero='lowpass')
data1 = signal.lfilter(b, [1.0], data)
waf_lpf = "out.wav"
wavfile.write(waf_lpf, sample_rate, data1.astype(np.int16))



