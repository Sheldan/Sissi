from flask import request, render_template
from PIL import Image, ImageDraw, ImageFont
import requests
import os
import json
import logging
import uuid
from __main__ import app
from utils import serve_pil_image
from datetime import timezone, datetime
import pytz


sissi_host = os.getenv('SISSI_HOST')
sissi_port = os.getenv('SISSI_PORT')
latest_donations_url = f'http://{sissi_host}:{sissi_port}/debra/latestDonations'
highest_donations_url = f'http://{sissi_host}:{sissi_port}/debra/highestDonations'
campaign_info_url = f'http://{sissi_host}:{sissi_port}/debra/campaignInfo'
endless_stream_info_url = f'http://{sissi_host}:{sissi_port}/stream/endlessStream'


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
    d1.text((0, 0), f"{campaign_info['collected']}/{campaign_info['target']}€", fill=parameters.color, font=font)
    return serve_pil_image(img)


@app.route('/debra/image/endlessStream/end')
def endless_stream_image():
    stream_id = int(request.args.get('streamId', type=int))
    endless_stream_info = json.loads(requests.get(f'{endless_stream_info_url}/{stream_id}').text)
    logging.info(f'rendering endless stream end image')
    parameters = parse_image_parameters()
    if not parameters.validated:
        return parameters.validation_message, 400
    img = Image.new('RGBA', (parameters.canvas_width, parameters.canvas_height), (255, 0, 0, 0))
    d1 = ImageDraw.Draw(img)
    font = ImageFont.truetype(f'{parameters.font_name}.ttf', parameters.font_size)
    end_time = datetime.strptime(endless_stream_info['endDate'], "%Y-%m-%dT%H:%M:%S%z")
    tz = pytz.timezone('Europe/Vienna')
    end_time_formatted = end_time.astimezone(tz).strftime('%d.%m %H:%M')
    d1.text((0, 0), f"{end_time_formatted}", fill=parameters.color, font=font)
    return serve_pil_image(img)


@app.route('/debra/image/endlessStream/end/html')
def endless_stream_html():
    refresh_interval = int(request.args.get('refreshInterval', 30, type=int))
    random_bit = str(uuid.uuid4())
    parameters_query = request.query_string.decode()
    return render_template('image_refresh_wrapper.html', imagePath=f'/debra/image/endlessStream/end?{parameters_query}&{random_bit}', refreshInterval=refresh_interval)


@app.route('/debra/image/endlessStream/remaining')
def endless_stream_remaining():
    stream_id = int(request.args.get('streamId', type=int))
    endless_stream_info = json.loads(requests.get(f'{endless_stream_info_url}/{stream_id}').text)
    logging.info(f'rendering endless stream remaining image')
    parameters = parse_image_parameters()
    if not parameters.validated:
        return parameters.validation_message, 400
    img = Image.new('RGBA', (parameters.canvas_width, parameters.canvas_height), (255, 0, 0, 0))
    d1 = ImageDraw.Draw(img)
    font = ImageFont.truetype(f'{parameters.font_name}.ttf', parameters.font_size)
    end_time = datetime.strptime(endless_stream_info['endDate'], "%Y-%m-%dT%H:%M:%S%z").replace(tzinfo=pytz.utc)
    current_time = datetime.now(timezone.utc)
    remaining_time = end_time - current_time
    total_seconds = remaining_time.total_seconds()
    remaining_time_formatted = f'{int(total_seconds // 3600):02d}:{int((total_seconds % 3600) // 60):02d}:{int(total_seconds % 60):02d}'
    d1.text((0, 0), f"{remaining_time_formatted}", fill=parameters.color, font=font)
    return serve_pil_image(img)


@app.route('/debra/image/endlessStream/remaining/html')
def endless_stream_remaining_html():
    refresh_interval = int(request.args.get('refreshInterval', 30, type=int))
    random_bit = str(uuid.uuid4())
    parameters_query = request.query_string.decode()
    return render_template('image_refresh_wrapper.html', imagePath=f'/debra/image/endlessStream/remaining?{parameters_query}&{random_bit}', refreshInterval=refresh_interval)


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


@app.route('/debra/image/highestDonations/html')
def highest_donations_image_html_wrapper():
    refresh_interval = int(request.args.get('refreshInterval', 30, type=int))
    random_bit = str(uuid.uuid4())
    parameters_query = request.query_string.decode()
    return render_template('image_refresh_wrapper.html', imagePath=f'/debra/image/highestDonations?{parameters_query}&{random_bit}', refreshInterval=refresh_interval)


@app.route('/debra/image/latestDonations/html')
def latest_donations_image_html_wrapper():
    refresh_interval = int(request.args.get('refreshInterval', 30, type=int))
    random_bit = str(uuid.uuid4())
    parameters_query = request.query_string.decode()
    return render_template('image_refresh_wrapper.html', imagePath=f'/debra/image/latestDonations?{parameters_query}&{random_bit}', refreshInterval=refresh_interval)


@app.route('/debra/image/info/html')
def total_donations_image_html_wrapper():
    refresh_interval = int(request.args.get('refreshInterval', 30, type=int))
    random_bit = str(uuid.uuid4())
    parameters_query = request.query_string.decode()
    return render_template('image_refresh_wrapper.html', imagePath=f'/debra/image/info?{parameters_query}&{random_bit}', refreshInterval=refresh_interval)


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
    font = request.args.get('font', DonationImageGenerationConstants.font_default, type=str)
    font_size = int(request.args.get('fontSize', DonationImageGenerationConstants.font_size_default, type=int))
    canvas_width = int(request.args.get('canvasWidth', DonationImageGenerationConstants.canvas_width_default, type=int))
    canvas_height = int(request.args.get('canvasHeight', DonationImageGenerationConstants.canvas_height_default, type=int))
    donation_count = int(request.args.get('donationCount', DonationImageGenerationConstants.donation_count_default, type=int))
    r = int(request.args.get('r', DonationImageGenerationConstants.r_default, type=int))
    g = int(request.args.get('g', DonationImageGenerationConstants.g_default, type=int))
    b = int(request.args.get('b', DonationImageGenerationConstants.b_default, type=int))
    parameters = DonationImageGenerationParameters(font, font_size, canvas_width, canvas_height, donation_count, r, g, b)
    parameters.validate()
    return parameters


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
