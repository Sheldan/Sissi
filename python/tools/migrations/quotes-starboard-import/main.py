import sqlalchemy as db
import os
from starboard_loader import load_all_starboard_posts
from post_loader import enrich_posts
from quote_importer import import_quotes, fix_quote_created

db_host = os.getenv('DB_HOST')
db_port = os.getenv('DB_PORT')
db_database = os.getenv('DB_NAME')
db_user = os.getenv('DB_USER')
db_password = os.getenv('DB_PASS')

engine = db.create_engine('postgresql://%s:%s@%s:%s/%s' % (db_user, db_password, db_host, db_port, db_database))

with engine.connect() as con:
    posts = load_all_starboard_posts(con)
    sub_posts = chunks = [posts[x:x+100] for x in range(0, len(posts), 100)]
    print(f'Loaded {len(posts)} into {len(sub_posts)} partitions')
    counter = 0
    for sub_post in sub_posts:
        print(f'Partition size {len(sub_post)}')
        enriched_posts = enrich_posts(sub_post)
        print(f'Enriched posts')
        import_quotes(enriched_posts, con)
        print(f'Done storing quotes')
        con.commit()
        fix_quote_created(enriched_posts, con)
        con.commit()
        counter += 1
        print(f'Done. {counter}')