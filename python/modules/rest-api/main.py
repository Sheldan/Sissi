import logging
import os

from flask import Flask

FORMAT = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
logging.basicConfig(encoding='utf-8', level=logging.INFO, format=FORMAT)
template_dir = os.path.abspath('res/templates')
app = Flask(__name__, template_folder=template_dir)

# loads the api end points
import debra
import image_gen

@app.route('/')
def hello():
    return 'Hello, World?'


if __name__ == "__main__":
    from waitress import serve

    serve(app, host="0.0.0.0", port=8080)
