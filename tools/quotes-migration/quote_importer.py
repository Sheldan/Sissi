from sqlalchemy.sql import text


def insert_quotes(server_id: int, quotes, con):
    for quote_id in quotes:
        quote = quotes[quote_id]
        statement = text("""INSERT INTO quote(id, server_id, author_user_in_server_id, adder_user_in_server_id, source_channel_id, message_id, text, created) 
                            VALUES(:id, :server_id, 
                            (select user_in_server_id from user_in_server where user_id = :author_id and server_id = :server_id), 
                            (select user_in_server_id from user_in_server where user_id = :adder_id and server_id = :server_id), 
                            :channel_id, :message_id, :text, :created)""")
        con.execute(statement, {'id': quote.id, 'server_id': server_id, 'author_id': quote.author_id, 'adder_id': quote.adder_id,
                                'channel_id': quote.channel_id, 'message_id': quote.message_id,
                                'text': quote.content, 'created': quote.creation_time_stamp})
        if quote.attachments:
            attachment_statement = text("""INSERT INTO quote_attachment(quote_id, server_id, url, is_image) 
                                    VALUES(:quote_id, :server_id, :url, :is_image)""")
            for attachment in quote.attachments:
                con.execute(attachment_statement, {'quote_id': quote_id, 'server_id': server_id, 'url': attachment.url, 'is_image': attachment.is_image})


def create_channels(server_id: int, quotes, con):
    channel_ids = {}
    for quote_id in quotes:
        quote = quotes[quote_id]
        if not does_channel_exist(quote.channel_id, con) and quote.channel_id not in channel_ids:
            channel_ids[quote.channel_id] = 1
    for channel_id in channel_ids:
        create_channel(channel_id, server_id, con)


def create_users(server_id: int, quotes, con):
    created_users = {}
    for quote_id in quotes:
        quote = quotes[quote_id]
        if not does_user_exist(quote.adder_id, con) and quote.adder_id not in created_users:
            create_user(quote.adder_id, con)
            create_user_in_server(quote.adder_id, server_id, con)
            created_users[quote.adder_id] = 1
        if not does_user_exist(quote.author_id, con) and quote.author_id not in created_users:
            create_user(quote.author_id, con)
            create_user_in_server(quote.author_id, server_id, con)
            created_users[quote.author_id] = 1


def does_user_exist(user_id, con):
    statement = text("""SELECT count(1) FROM auser where id = :id""")
    return con.execute(statement, {'id': user_id}).fetchone()[0] == 1


def does_channel_exist(channel_id, con):
    statement = text("""SELECT count(1) FROM channel where id = :id""")
    return con.execute(statement, {'id': channel_id}).fetchone()[0] == 1


def create_user(user_id, con):
    statement = text("""INSERT INTO auser(id) VALUES(:id)""")
    print(f'Creating user {user_id}')
    con.execute(statement, {'id': user_id})


def create_channel(channel_id, server_id, con):
    statement = text("""INSERT INTO channel(id, server_id, type, deleted) VALUES(:id, :server_id, 'TEXT', false)""")
    print(f'Creating channel {channel_id}')
    con.execute(statement, {'id': channel_id, 'server_id': server_id})


def create_user_in_server(user_id, server_id, con):
    statement = text("""INSERT INTO user_in_server(server_id, user_id) VALUES(:server_id, :user_id) returning user_in_server_id""")
    return con.execute(statement, {'user_id': user_id, 'server_id': server_id}).fetchone()[0]