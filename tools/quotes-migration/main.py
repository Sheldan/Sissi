from legacy_loader import create_connection, load_all_quotes_with_attachments
from quote_importer import insert_quotes, create_users, create_channels
import sqlalchemy as db
import os

server_id = os.getenv('SERVER_ID')
conn = create_connection('new_quotes.db')

all_quotes = load_all_quotes_with_attachments(conn)
db_host = os.getenv('DB_HOST')
db_port = os.getenv('DB_PORT')
db_database = os.getenv('DB_NAME')
db_user = os.getenv('DB_USER')
db_password = os.getenv('DB_PASS')
engine = db.create_engine('postgresql://%s:%s@%s:%s/%s' % (db_user, db_password, db_host, db_port, db_database))

with engine.connect() as con:
    with con.begin():
        create_users(server_id, all_quotes, con)
        create_channels(server_id, all_quotes, con)
    with con.begin():
        insert_quotes(server_id, all_quotes, con)
