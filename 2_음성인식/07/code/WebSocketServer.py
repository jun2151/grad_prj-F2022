
from flask import Flask, render_template
from flask_sock import Sock
import time 

app = Flask(__name__)
sock = Sock(app)

waitSec = 5

@app.route('/')
def index():
    return render_template('index.html')


@sock.route('/vrs')
def echo(sock):
    data = sock.receive()
    print("#message from client: "+data)
    while True:
        file = open('station.txt', "r")
        data = file.read()
        file.close()
        sock.send(data)
        print('###')
        time.sleep(waitSec)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)