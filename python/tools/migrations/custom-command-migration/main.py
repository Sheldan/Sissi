import os
import sqlalchemy as db

from legacy_loader import load_all_commands
from command_importer import import_commands

server_id = os.getenv('SERVER_ID')

all_warnings = load_all_commands()
db_host = os.getenv('DB_HOST')
db_port = os.getenv('DB_PORT')
db_database = os.getenv('DB_NAME')
db_user = os.getenv('DB_USER')
db_password = os.getenv('DB_PASS')

engine = db.create_engine('postgresql://%s:%s@%s:%s/%s' % (db_user, db_password, db_host, db_port, db_database))

with engine.connect() as con:
    with con.begin():
        import_commands(server_id, all_warnings, con)
