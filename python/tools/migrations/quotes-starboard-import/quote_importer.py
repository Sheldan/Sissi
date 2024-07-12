from sqlalchemy.sql import text

def import_quotes(posts, con):
    for post in posts:
        if 'content' not in post:
            print(f"Skipping {post['message_id']} because no content, did it fail?")
        print(f"Inserting {post['message_id']}")
        statement = text("""INSERT INTO quote(author_user_in_server_id, adder_user_in_server_id, source_channel_id, 
                                                        server_id, message_id, text, created) 
                                    VALUES(:author_id, :adder_id, :channel_id, :server_id, :message_id, :content, :created) returning id""")
        quote_id = con.execute(statement, {'author_id': post['author_id'], 'adder_id': post['adder_id'], 'channel_id': post['channel_id'], 'server_id': post['server_id'],
                                       'message_id': post['message_id'], 'content': post['content'], 'created': post['created']}).fetchone()[0]
        print(f'Created quote {quote_id}')
        for attachment in post['attachments']:
            statement = text("""INSERT INTO quote_attachment(quote_id, server_id, url, is_image) 
                                    VALUES(:quote_id, :server_id, :url, :is_image)""")
            con.execute(statement, {'quote_id': quote_id, 'server_id': post['server_id'], 'url': attachment['url'], 'is_image': attachment['is_image']})
        post['quote_id'] = quote_id

# the insert trigger always updated created, we have to re-do it (will be changed, but not for now)
def fix_quote_created(posts, con):
    for post in posts:
        if 'quote_id' in post:
            statement = text("""update quote set created = :created where id = :quote_id""")
            con.execute(statement, {'created': post['created'], 'quote_id': post['quote_id']})
