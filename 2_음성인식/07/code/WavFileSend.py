
import socket
import sys 

def sendWaveFile(args):
    #socket 
    ip = '127.0.0.1'
    port = 3000
    size = 2048

    ip = args[1]
    file_name = args[2]

    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((ip,port))
    
    file = open(file_name, "rb")
    #print("###: "+file_name)
    
    data = file.read(size)
    while data:
        #print('###')
        sock.sendall(data)
        data = file.read(size)
        
    file.close()    
        
if __name__ == '__main__':
    sendWaveFile(sys.argv)
    #ex) python WavFileSend.py 127.0.0.1 성균관대.wav
    
