
class LegacyQuoteAttachment:
    message_id = 0
    file_name = ''
    url = ''
    is_image: bool = False


class LegacyQuote:
    id = 0
    channel_id = 0
    author_id = 0
    adder_id = 0
    creation_time_stamp = None
    content = ''
    message_id = 0
    attachments = None
