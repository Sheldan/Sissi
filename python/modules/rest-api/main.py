from flask import Flask, send_file, request
from io import BytesIO
from PIL import Image, ImageDraw, ImageFont
import requests
import os
import json
import logging

FORMAT = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
logging.basicConfig(encoding='utf-8', level=logging.INFO, format=FORMAT)
app = Flask(__name__)

sissi_host = os.getenv('SISSI_HOST')
sissi_port = os.getenv('SISSI_PORT')
latest_donations_url = f'http://{sissi_host}:{sissi_port}/debra/latestDonations'
highest_donations_url = f'http://{sissi_host}:{sissi_port}/debra/highestDonations'
campaign_info_url = f'http://{sissi_host}:{sissi_port}/debra/campaignInfo'


class ValidationException(Exception):
    def __init__(self, provided_value, message):
        self.provided_value = provided_value
        self.message = message
        super().__init__(f'{self.message}: provided value: {provided_value}')


class DonationImageGenerationConstants:
    allowed_fonts = ['Andale_Mono', 'Courier_New', 'Impact', 'Trebuchet_MS_Italic',
                     'Arial', 'Courier_New_Bold', 'Times_New_Roman', 'Verdana',
                     'Arial_Black', 'Courier_New_Bold_Italic', 'Times_New_Roman_Bold', 'Verdana_Bold',
                     'Arial_Bold', 'Courier_New_Italic', 'Times_New_Roman_Bold_Italic', 'Verdana_Bold_Italic',
                     'Arial_Bold_Italic', 'Georgia', 'Times_New_Roman_Italic', 'Verdana_Italic',
                     'Arial_Italic', 'Georgia_Bold', 'Trebuchet_MS',
                     'Comic_Sans_MS', 'Georgia_Bold_Italic', 'Trebuchet_MS_Bold',
                     'Comic_Sans_MS_Bold', 'Georgia_Italic', 'Trebuchet_MS_Bold_Italic']
    font_range = (2, 150)
    canvas_width_range = (1, 2500)
    canvas_height_range = (1, 2500)
    donation_count_range = (1, 25)
    color_range = (0, 255)
    font_default = 'Arial'
    font_size_default = 40
    canvas_width_default = 1024
    canvas_height_default = 300
    donation_count_default = 5
    r_default = 0
    g_default = 0
    b_default = 0


class DonationImageGenerationParameters:
    validation_message = ''
    validation_value = ''
    validated = None

    def __init__(self, font_name, font_size, canvas_width, canvas_height, donation_count, r, g, b):
        self.font_name = font_name
        self.font_size = font_size
        self.canvas_width = canvas_width
        self.canvas_height = canvas_height
        self.donation_count = donation_count
        self.color = (r, g, b)

    def validate(self):
        self.validated = True
        if self.font_name not in DonationImageGenerationConstants.allowed_fonts:
            self.validated = False
            self.validation_message = f'Font must be one of ' + ','.join(DonationImageGenerationConstants.allowed_fonts)
            self.validation_value = self.font_name

        if self.font_size > DonationImageGenerationConstants.font_range[1] or self.font_size < DonationImageGenerationConstants.font_range[0]:
            self.validated = False
            self.validation_message = f'Font size must be between {DonationImageGenerationConstants.font_range[0]} and {DonationImageGenerationConstants.font_range[1]}'
            self.validation_value = self.font_size

        if self.canvas_height > DonationImageGenerationConstants.canvas_height_range[1] or self.canvas_height < DonationImageGenerationConstants.canvas_height_range[0]:
            self.validated = False
            self.validation_message = f'Canvas height must be between {DonationImageGenerationConstants.canvas_height_range[0]} and {DonationImageGenerationConstants.canvas_height_range[1]}'
            self.validation_value = self.canvas_height

        if self.canvas_width > DonationImageGenerationConstants.canvas_width_range[1] or self.canvas_width < DonationImageGenerationConstants.canvas_width_range[0]:
            self.validated = False
            self.validation_message = f'Canvas width must be between {DonationImageGenerationConstants.canvas_width_range[0]} and {DonationImageGenerationConstants.color_range[1]}'
            self.validation_value = self.canvas_width

        if self.donation_count > DonationImageGenerationConstants.donation_count_range[1] or self.donation_count < DonationImageGenerationConstants.donation_count_range[0]:
            self.validated = False
            self.validation_message = f'Donation count must be between {DonationImageGenerationConstants.donation_count_range[0]} and {DonationImageGenerationConstants.donation_count_range[1]}'
            self.validation_value = self.donation_count

        if self.color[0] > DonationImageGenerationConstants.color_range[1] or self.color[0] < DonationImageGenerationConstants.color_range[0]:
            self.validated = False
            self.validation_message = f'Red must be between {DonationImageGenerationConstants.color_range[0]} and {DonationImageGenerationConstants.color_range[1]} inclusively'
            self.validation_value = self.color[0]

        if self.color[1] > DonationImageGenerationConstants.color_range[1] or self.color[1] < DonationImageGenerationConstants.color_range[0]:
            self.validated = False
            self.validation_message = f'Green must be between {DonationImageGenerationConstants.color_range[0]} and {DonationImageGenerationConstants.color_range[1]} inclusively'
            self.validation_value = self.color[1]

        if self.color[2] > DonationImageGenerationConstants.color_range[1] or self.color[2] < DonationImageGenerationConstants.color_range[0]:
            self.validated = False
            self.validation_message = f'Blue must be between {DonationImageGenerationConstants.color_range[0]} and {DonationImageGenerationConstants.color_range[1]} inclusively'
            self.validation_value = self.color[2]



@app.route('/')
def hello():
    return 'Hello, World?'


@app.route('/debra/latestDonations')
def latest_donations():
    donation_stats = requests.get(latest_donations_url)
    logging.info(f'returning latest donations')
    return donation_stats.text


@app.route('/debra/highestDonations')
def highest_donations():
    donation_stats = requests.get(highest_donations_url)
    logging.info(f'returning highest donations')
    return donation_stats.text


@app.route('/debra/campaignInfo')
def campaign_info_route():
    donation_stats = requests.get(campaign_info_url)
    logging.info(f'returning campaign info')
    return donation_stats.text


@app.route('/debra/image/help')
def show_image_generation_help():

    def make_param(parameters_obj, name, default, values):
        parameters_obj[name] = {
            'default': default,
            'values': values
        }
    parameters = {}
    make_param(parameters, 'font', DonationImageGenerationConstants.font_default, DonationImageGenerationConstants.allowed_fonts)
    make_param(parameters, 'fontSize', DonationImageGenerationConstants.font_size_default, DonationImageGenerationConstants.font_range)
    make_param(parameters, 'canvasWidth', DonationImageGenerationConstants.canvas_width_default, DonationImageGenerationConstants.canvas_width_range)
    make_param(parameters, 'canvasHeight', DonationImageGenerationConstants.canvas_height_default, DonationImageGenerationConstants.canvas_height_range)
    make_param(parameters, 'donationCount', DonationImageGenerationConstants.donation_count_default, DonationImageGenerationConstants.donation_count_range)
    make_param(parameters, 'r', DonationImageGenerationConstants.r_default, DonationImageGenerationConstants.color_range)
    make_param(parameters, 'g', DonationImageGenerationConstants.g_default, DonationImageGenerationConstants.color_range)
    make_param(parameters, 'b', DonationImageGenerationConstants.b_default, DonationImageGenerationConstants.color_range)
    info_object = {
        'parameters': parameters
    }
    return json.dumps(info_object)


@app.route('/debra/image/info')
def total_donations_image():
    campaign_info = json.loads(requests.get(campaign_info_url).text)
    logging.info(f'rendering campaign info')
    parameters = parse_image_parameters()
    if not parameters.validated:
        return parameters.validation_message, 400
    img = Image.new('RGBA', (parameters.canvas_width, parameters.canvas_height), (255, 0, 0, 0))
    d1 = ImageDraw.Draw(img)
    font = ImageFont.truetype(f'{parameters.font_name}.ttf', parameters.font_size)
    d1.text((0, 0), f"Aktuell {campaign_info['collected']}/{campaign_info['target']}€", fill=parameters.color, font=font)
    return serve_pil_image(img)


@app.route('/debra/image/latestDonations')
def latest_donation_image():
    donation_stats = json.loads(requests.get(latest_donations_url).text)
    logging.info(f'rendering latest donations')
    parameters = parse_image_parameters()
    if not parameters.validated:
        return parameters.validation_message, 400
    return rendering_donation_image(donation_stats, parameters)


@app.route('/debra/image/highestDonations')
def highest_donation_image():
    donation_stats = json.loads(requests.get(highest_donations_url).text)
    logging.info(f'rendering highest donations')
    parameters = parse_image_parameters()
    if not parameters.validated:
        return parameters.validation_message, 400
    return rendering_donation_image(donation_stats, parameters)


def rendering_donation_image(donation_stats, parameters):
    img = Image.new('RGBA', (parameters.canvas_width, parameters.canvas_height), (255, 0, 0, 0))
    d1 = ImageDraw.Draw(img)
    font = ImageFont.truetype(f'{parameters.font_name}.ttf', parameters.font_size)
    donations_to_draw = donation_stats['donations'][:parameters.donation_count]
    height = parameters.font_size
    it = 0
    for donation in donations_to_draw:
        name = donation['firstName'] if not donation['anonymous'] else 'anonym'
        d1.text((0, height * it), f"{donation['donationAmount']}€ von {name}", fill=parameters.color, font=font)
    it += 1
    return serve_pil_image(img)


def parse_image_parameters() -> DonationImageGenerationParameters:
    font = request.args.get('font', DonationImageGenerationConstants.font_default)
    font_size = int(request.args.get('fontSize', DonationImageGenerationConstants.font_size_default))
    canvas_width = int(request.args.get('canvasWidth', DonationImageGenerationConstants.canvas_width_default))
    canvas_height = int(request.args.get('canvasHeight', DonationImageGenerationConstants.canvas_height_default))
    donation_count = int(request.args.get('donationCount', DonationImageGenerationConstants.donation_count_default))
    r = int(request.args.get('r', DonationImageGenerationConstants.r_default))
    g = int(request.args.get('g', DonationImageGenerationConstants.g_default))
    b = int(request.args.get('b', DonationImageGenerationConstants.b_default))
    parameters = DonationImageGenerationParameters(font, font_size, canvas_width, canvas_height, donation_count, r, g, b)
    parameters.validate()
    return parameters


def serve_pil_image(pil_img):
    img_io = BytesIO()
    pil_img.save(img_io, 'PNG')
    img_io.seek(0)
    return send_file(img_io, mimetype='image/png')


if __name__ == "__main__":
    from waitress import serve

    serve(app, host="0.0.0.0", port=8080)
