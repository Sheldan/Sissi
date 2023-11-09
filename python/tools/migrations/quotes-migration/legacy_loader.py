import sqlite3
from sqlite3 import Error
import datetime

from dto import LegacyQuote, LegacyQuoteAttachment


def create_connection(file: str):
    conn = None
    try:
        conn = sqlite3.connect(file)
    except Error as e:
        print(e)
    return conn


def load_all_quotes_with_attachments(conn):
    cur = conn.cursor()
    cur.execute("SELECT q.id, q.chan_id, q.author_id, "
                "q.adder_id, q.jump_url, q.'timestamp', q.content,"
                "a.msg_id, a.filename, a.url, a.is_image  "
                "FROM quotes q left outer join attachments a ON q.jump_url like '%' || a.msg_id")

    rows = cur.fetchall()

    quotes = {}
    for row in rows:
        quote_id = row[0]
        if quote_id not in quotes:
            quotes[quote_id] = LegacyQuote()
        current_quote = quotes[quote_id]
        current_quote.id = quote_id
        current_quote.channel_id = row[1]
        current_quote.author_id = row[2]
        current_quote.adder_id = row[3]
        if len(row[5]) > 25:
            current_quote.creation_time_stamp = datetime.datetime.strptime(row[5], '%Y-%m-%d %H:%M:%S.%f')
        elif len(row[5]) != 19:
            current_quote.creation_time_stamp = datetime.datetime.strptime(row[5], '%Y-%m-%d %H:%M:%S.%f')
        else:
            current_quote.creation_time_stamp = datetime.datetime.strptime(row[5], '%Y-%m-%d %H:%M:%S')
        current_quote.content = row[6]
        current_quote.message_id = row[4][row[4].rindex('/')+1:]

        if row[7] is not None:
            if current_quote.attachments is None:
                current_quote.attachments = []
            attachment = LegacyQuoteAttachment()
            attachment.message_id = row[7]
            attachment.file_name = row[8]
            attachment.url = row[9]
            attachment.is_image = row[10] == 1
            current_quote.attachments.append(attachment)
    return quotes
