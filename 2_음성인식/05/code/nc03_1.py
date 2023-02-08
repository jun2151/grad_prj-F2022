# url: https://www.kaggle.com/code/awalahmed/noisecancel4000/notebook
# In [12] 부분 코드 테스트 
# 결과: 일부 효과 있음


from scipy.io import wavfile
import noisereduce as nr

# load data
rate, data = wavfile.read("test.wav")
# perform noise reduction
reduced_noise = nr.reduce_noise(y=data, sr=rate)
print(reduced_noise)
wavfile.write("out.wav", rate, reduced_noise)
print(data)
